package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.Selector;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.util.ISeq;
import lombok.extern.slf4j.XSlf4j;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.io.FuryInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface EvolutionPersistence {

  static File file(final Path file,
      final Selector<IntegerGene, Double> selector, final int count) {
    return new File(file, selector, count);
  }

  EvolutionStart<IntegerGene, Double> read();

  void write(EvolutionResult<IntegerGene, Double> individuals);

  @XSlf4j
  class File implements EvolutionPersistence, AutoCloseable {

    record Population(List<Individual> individuals, long generation) {
      public static Population empty() {
        return new Population(List.of(), 1);
      }
    }


    record Individual(List<Integer> perks, List<Integer> areas, List<Integer> kinds,
        long generation) {
    }


    private final Path file;
    private final Selector<IntegerGene, Double> selector;
    private final int count;
    private final Fury fury;

    // guarantee sequential access to the file
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
        Thread.ofVirtual().name("evolution-persistence-", 0).factory());

    private File(final Path file, final Selector<IntegerGene, Double> selector, final int count) {
      this.file = file;
      this.selector = selector;
      this.count = count;
      this.fury = Fury.builder().withLanguage(Language.JAVA)
          .requireClassRegistration(true)
          .build();
      fury.register(Population.class);
      fury.register(Individual.class);
    }

    @Override
    public EvolutionStart<IntegerGene, Double> read() {
      final Population population = CompletableFuture.supplyAsync(() -> {
        try (final var in = new FuryInputStream(Files.newInputStream(file))) {
          return fury.deserializeJavaObject(in, Population.class);
        } catch (final IOException | IndexOutOfBoundsException e) {
          log.warn("An exception prevented reading file {}. "
              + "Evolution will start from scratch.", file, e);
          return Population.empty();
        }
      }, executor).join();

      if (population == null) {
        log.warn("Deserialized format is null. Serialization may have been corrupted,"
            + " or format changed since last execution. Evolution will start from scratch.");
      }
      final Population actual = population != null ? population : Population.empty();
      final var collect = actual.individuals.stream()
          .map(this::toPhenotype)
          .collect(ISeq.toISeq());
      return EvolutionStart.of(collect, actual.generation());
    }

    @Override
    public void write(final EvolutionResult<IntegerGene, Double> evolutionResult) {
      final List<Individual> individuals =
          selector.select(evolutionResult.population(), count, Optimize.MAXIMUM).stream()
              .map(this::toIndividual).toList();
      final var format = new Population(individuals, evolutionResult.generation());

      CompletableFuture.runAsync(() -> {
        try (final var out = new BufferedOutputStream(Files.newOutputStream(file))) {
          fury.serializeJavaObject(out, format);
        } catch (final IOException ioe) {
          log.warn("An exception prevented writing to file {}. State was not saved.", file, ioe);
        }
      }, executor).join();
    }

    private Individual toIndividual(final Phenotype<IntegerGene, Double> phenotype) {
      final Genotype<IntegerGene> genotype = phenotype.genotype();
      return new Individual(
          genotype.get(0).as(IntegerChromosome.class).intStream().boxed().toList(),
          genotype.get(1).as(IntegerChromosome.class).intStream().boxed().toList(),
          genotype.get(2).as(IntegerChromosome.class).intStream().boxed().toList(),
          phenotype.generation());
    }

    private Phenotype<IntegerGene, Double> toPhenotype(final Individual individual) {
      final var genotype = Genotype.of(
          IntegerChromosome.of(individual.perks().stream()
              .map(it -> IntegerGene.of(it, 0, 5)).toList()),
          IntegerChromosome.of(individual.areas().stream()
              .map(it -> IntegerGene.of(it, 0, 6)).toList()),
          IntegerChromosome.of(individual.kinds().stream()
              .map(it -> IntegerGene.of(it, 0, 6)).toList()));
      return Phenotype.of(genotype, individual.generation());
    }

    @Override
    public void close() {
      executor.close();
    }
  }
}
