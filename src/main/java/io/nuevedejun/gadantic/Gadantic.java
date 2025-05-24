package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStream;
import io.nuevedejun.gadantic.PlotPhenotype.FitnessCoefficients;
import lombok.extern.slf4j.XSlf4j;

import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@XSlf4j
public class Gadantic {
  public static void main(final String[] args) {
    final Thread main = Thread.currentThread();
    final UncaughtExceptionHandler ueh = (thread, throwable) ->
        log.error("Uncaught exception; thread: {}", thread, throwable);

    final var gadantic = new Gadantic();
    Runtime.getRuntime().addShutdownHook(Thread.ofPlatform()
        .name("shutdown-hook")
        .uncaughtExceptionHandler(ueh)
        .unstarted(() -> gadantic.shutdown(main)));

    main.setUncaughtExceptionHandler(ueh);
    gadantic.run();
  }

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private void run() {
    final var properties = Properties.system();

    final var iterableFactory = PlotIterableFactory.standard();
    final var plotDecoder = PlotDecoder.standard(iterableFactory);

    final var coefficients = new FitnessCoefficients(
        properties.waterWeight(1.0),
        properties.weedWeight(1.0),
        properties.qualityWeight(1.0),
        properties.harvestWeight(1.0),
        properties.distinctWeight(1.0));
    final var plotPhenotype = new PlotPhenotype(plotDecoder, coefficients);

    final var constraint = new PlotConstraint(iterableFactory);

    final var genotype = plotPhenotype.genotype();
    final var engine = Engine
        .builder(plotPhenotype::fitness, constraint.constrain(genotype))
        .constraint(constraint)
        .populationSize(properties.population(256))
        .build();

    log.info("Initiating evolution");
    final EvolutionStream<IntegerGene, Double> stream = engine.stream();
    final Genotype<IntegerGene> result = limitStream(stream, properties)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Plot:\n{}", plotDecoder.decode(result).str());
  }

  private Stream<EvolutionResult<IntegerGene, Double>> limitStream(
      final EvolutionStream<IntegerGene, Double> stream, final Properties properties) {
    final var ongoing = stream.limit(_ -> !stopped.get());
    final OptionalLong generations = properties.generations();
    if (generations.isPresent()) {
      return ongoing.limit(generations.getAsLong());
    } else {
      return ongoing;
    }
  }

  private void shutdown(final Thread main) {
    try {
      stopped.set(true);
      final long shutdownMillis = Properties.system().shutdownMillis(100);
      if (!main.join(Duration.ofMillis(shutdownMillis))) {
        log.warn("The main thread did not complete within {} ms. Data may be lost.",
            shutdownMillis);
      }
    } catch (final InterruptedException e) {
      log.warn("Shutdown was interrupted while waiting for the main thread to complete.");
      Thread.currentThread().interrupt();
    }
  }
}
