package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.MonteCarloSelector;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionDurations;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvolutionPersistenceTest {

  @TempDir
  Path temp;

  @Test
  void testPersistence() {
    final var factory = Genotype.of(IntegerChromosome.of(0, 15, 81));
    final var population = factory.instances().limit(50)
        .map(g -> Phenotype.of(g, 1, 0.0))
        .collect(ISeq.toISeq());
    final var evolution = EvolutionResult.of(
        Optimize.MAXIMUM, population, 1, EvolutionDurations.ZERO, 0, 0, 0);

    final EvolutionStart<IntegerGene, Double> result;
    try (final EvolutionPersistence.File persistence = EvolutionPersistence.file(
        temp.resolve("gadantic.sav"),
        new MonteCarloSelector<>(), 10)) {
      persistence.write(evolution);
      result = persistence.read();
    }

    final boolean match = result.population().stream()
        .allMatch(ph -> contains(population, ph));
    assertTrue(match);
  }

  private boolean contains(
      final ISeq<Phenotype<IntegerGene, Double>> population,
      final Phenotype<IntegerGene, Double> phenotype) {
    final List<Integer> genes = geneList(phenotype);
    return population.stream().anyMatch(ph ->
        genes.equals(geneList(ph)) && phenotype.generation() == ph.generation());
  }

  private List<Integer> geneList(final Phenotype<IntegerGene, Double> phenotype) {
    return phenotype.genotype().chromosome().as(IntegerChromosome.class).intStream()
        .boxed().toList();
  }

  @Test
  void testPersistenceNoFilePresent() {
    final EvolutionStart<IntegerGene, Double> result;
    try (final EvolutionPersistence.File persistence = EvolutionPersistence.file(
        temp.resolve("gadantic.sav"),
        new MonteCarloSelector<>(), 10)) {
      result = persistence.read();
    }

    assertEquals(0, result.population().size());
  }

  @Test
  void testPersistenceFileBadFormat() throws IOException {
    final Path source = temp.resolve("gadantic.sav");
    Files.createFile(source);

    final EvolutionStart<IntegerGene, Double> result;
    try (final EvolutionPersistence.File persistence = EvolutionPersistence.file(source,
        new MonteCarloSelector<>(), 10)) {
      result = persistence.read();
    }

    assertEquals(0, result.population().size());
  }
}
