package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.quarkus.logging.Log;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.nuevedejun.gadantic.Gadantic.LOG_FQCN;
import static io.nuevedejun.gadantic.Iterables.arr;

public interface PlotPrinter {

  void accept(EvolutionResult<IntegerGene, Double> result);

  void print(Phenotype<IntegerGene, Double> individual);

  @ApplicationScoped
  class Impl implements PlotPrinter, AutoCloseable {

    private final PlotDecoder decoder;
    private final ScheduledExecutorService executor;

    private final AtomicReference<Phenotype<IntegerGene, Double>> ref = new AtomicReference<>();
    private final AtomicLong best = new AtomicLong(Double.doubleToRawLongBits(0));

    Impl(final PlotDecoder decoder,
        @ConfigProperty(name = "log-delay-millis", defaultValue = "1000") final long delayMillis) {
      this.decoder = decoder;

      executor = Executors.newSingleThreadScheduledExecutor(
          Thread.ofVirtual().name("plot-printer-", 0).factory());
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
      sb.append("Unique crops: (").append(plot.unique()).append('|')
          .append(percent(plot.unique(), Crop.len())).append("%)").append('\n');

      sb.append("Buff efficiency: ").append(String.format("%2.5f", plot.efficiency()))
          .append(" | ");
      individual.fitnessOptional().ifPresent(fitness ->
          sb.append("Fitness: ").append(String.format("%2.5f", fitness)).append(" | "));
      sb.append("Generation: ").append(individual.generation()).append('\n');

      sb.append("Garden Planner: ").append(plot.layoutUrl());

      Log.info(LOG_FQCN, "Best individual found\n{0}", arr(sb), null);
    }

    private StringBuilder appendPercent(final StringBuilder sb, final int count,
        final String text) {
      return sb.append(text).append(": (").append(count).append('|').append(percent(count, 81))
          .append("%)");
    }

    private int percent(final int count, final int total) {
      return 100 * count / total;
    }

    @PreDestroy
    @Override
    public void close() {
      executor.close();
    }
  }
}
