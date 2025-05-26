package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import lombok.extern.slf4j.XSlf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public interface PlotPrinter {

  static Standard standard(final PlotDecoder decoder, final long delayMillis) {
    return new Standard(decoder, delayMillis);
  }

  void accept(EvolutionResult<IntegerGene, Double> result);

  void print(Phenotype<IntegerGene, Double> individual);

  @XSlf4j
  class Standard implements PlotPrinter, AutoCloseable {

    private final PlotDecoder decoder;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
        Thread.ofVirtual().name("plot-printer-", 0).factory());

    private final AtomicReference<Phenotype<IntegerGene, Double>> ref = new AtomicReference<>();
    private final AtomicLong best = new AtomicLong(Double.doubleToRawLongBits(0));

    private Standard(final PlotDecoder decoder, final long delayMillis) {
      this.decoder = decoder;
      executor.scheduleAtFixedRate(this::command, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void command() {
      final Phenotype<IntegerGene, Double> individual = ref.getAndSet(null);
      if (individual != null) {
        print(individual);
      }
    }

    @Override
    public void accept(final EvolutionResult<IntegerGene, Double> result) {
      final var phenotype = result.bestPhenotype();
      if (phenotype.fitness() != null) {
        best.updateAndGet(prev -> {
          final double actual = Double.longBitsToDouble(prev);
          if (phenotype.fitness() > actual) {
            ref.set(phenotype); // <- intentional side effect
            return Double.doubleToRawLongBits(phenotype.fitness());
          } else {
            return prev;
          }
        });
      }
    }

    @Override
    public void print(final Phenotype<IntegerGene, Double> individual) {
      ref.set(null);

      final Genotype<IntegerGene> genotype = individual.genotype();
      final Plot plot = decoder.decode(genotype);

      final StringBuilder sb = new StringBuilder(plot.tableString()).append('\n')
          .append(String.join(" | ", Plot.LEGEND)).append('\n');

      appendPercent(sb, plot.water(), "Water").append(" | ");
      appendPercent(sb, plot.weed(), "Weed").append(" | ");
      appendPercent(sb, plot.quality(), "Quality").append(" | ");
      appendPercent(sb, plot.harvest(), "Harvest").append(" | ");
      sb.append("Unique crops: (").append(plot.distinct()).append('|')
          .append(percent(plot.distinct(), Crop.len())).append("%)").append('\n');

      individual.fitnessOptional().ifPresent(fitness ->
          sb.append("Fitness: ").append(String.format("%2.5f", fitness)).append(" | "));
      sb.append("Generation: ").append(individual.generation()).append('\n');

      sb.append("Garden Planner: ").append(plot.layoutUrl());

      log.info("Best individual found\n{}", sb);
    }

    private StringBuilder appendPercent(final StringBuilder sb, final int count,
        final String text) {
      return sb.append(text).append(": (").append(count).append('|').append(percent(count, 81))
          .append("%)");
    }

    private int percent(final int count, final int total) {
      return 100 * count / total;
    }

    @Override
    public void close() {
      executor.close();
    }
  }
}
