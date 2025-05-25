package io.nuevedejun.gadantic;

import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.ShuffleMutator;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.engine.EvolutionStream;
import io.nuevedejun.gadantic.PlotPhenotype.FitnessCoefficients;
import lombok.extern.slf4j.XSlf4j;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@XSlf4j
public class Gadantic {
  public static void main(final String[] args) {
    final Thread main = Thread.currentThread();
    final UncaughtExceptionHandler ueh = (thread, throwable) -> {
      log.error("Uncaught exception; thread: {}", thread, throwable);
      System.exit(1);
    };

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
    log.info("Initializing application");
    final var properties = Properties.system();
    final var iterableFactory = PlotIterableFactory.standard();
    final var plotDecoder = PlotDecoder.standard(iterableFactory);

    final var coefficients = new FitnessCoefficients(
        properties.waterWeight(1.0),
        properties.weedWeight(1.0),
        properties.qualityWeight(1.0),
        properties.harvestWeight(1.0),
        properties.distinctWeight(1.0));
    final var plotPhenotype = PlotPhenotype.standard(plotDecoder, coefficients);
    final var constraint = PlotConstraint.create(iterableFactory);

    final var encoding = plotPhenotype.encoding();

    log.info("Creating evolution engine");
    final var engine = Engine
        .builder(plotPhenotype::fitness, constraint.constrain(encoding))
        .constraint(constraint)
        .populationSize(properties.population(256))
        .survivorsSize(51)
        .survivorsSelector(new StochasticUniversalSelector<>())
        .offspringSelector(new RouletteWheelSelector<>())
        .alterers(
            new ShuffleMutator<>(0.11),
            new UniformCrossover<>(0.11, 0.11),
            new Mutator<>(0.03))
        .build();

    try (final var persistence = EvolutionPersistence.file(
        Path.of(properties.saveFile("gadantic.sav")),
        engine.survivorsSelector(), engine.survivorsSize());
        final var printer = PlotPrinter.standard(plotDecoder, properties.printDelayMillis(1000))) {

      log.info("Loading result of previous execution");
      final EvolutionStart<IntegerGene, Double> start = persistence.read();

      log.info("Initiating evolution");
      final EvolutionStream<IntegerGene, Double> stream = engine.stream(start);
      final EvolutionResult<IntegerGene, Double> result = limitStream(stream, properties)
          .peek(printer::accept) //NOSONAR java:S3864 peek is ok for this use case
          .collect(EvolutionResult.toBestEvolutionResult());

      log.info("Evolution terminated. Saving result");
      persistence.write(result);
      log.debug("Finished saving evolution result");

      printer.print(result.bestPhenotype());
    }
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
      log.info("Initiating controlled application shutdown");
      stopped.set(true);
      final long shutdownMillis = Properties.system().shutdownMillis(500);
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
