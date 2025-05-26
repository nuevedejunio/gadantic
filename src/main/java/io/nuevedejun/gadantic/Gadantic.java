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

  private final Properties properties = Properties.file();
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private void run() {
    log.info("Initializing application");
    final var plotDecoder = PlotDecoder.create();

    final var coefficients = new FitnessCoefficients(
        properties.getDouble("weights.water-retention"),
        properties.getDouble("weights.weed-prevention"),
        properties.getDouble("weights.quality-boost"),
        properties.getDouble("weights.harvest-increase"),
        properties.getDouble("weights.unique-crops"),
        properties.getDouble("weights.buff-efficiency"));
    final var plotPhenotype = PlotPhenotype.create(plotDecoder, coefficients);

    final var iterableShuffler = Iterables.Shuffler.create();
    final var constraint = PlotConstraint.create(iterableShuffler);

    final var encoding = plotPhenotype.encoding();

    log.info("Creating evolution engine");
    final var engine = Engine
        .builder(plotPhenotype::fitness, constraint.constrain(encoding))
        .constraint(constraint)
        .populationSize(properties.getInt("ga.population-size"))
        .offspringFraction(properties.getDouble("ga.offspring-fraction"))
        .survivorsSelector(new StochasticUniversalSelector<>())
        .offspringSelector(new RouletteWheelSelector<>())
        .alterers(
            new ShuffleMutator<>(properties.getDouble("ga.shuffle-probability")),
            new UniformCrossover<>(
                properties.getDouble("ga.crossover-probability"),
                properties.getDouble("ga.swap-probability")),
            new Mutator<>(properties.getDouble("ga.mutation-probability")),
            new ReplacementMutator(properties.getDouble("ga.replacement-probability")))
        .build();

    try (final var persistence = EvolutionPersistence.file(
        Path.of(properties.getString("save-file")),
        engine.survivorsSelector(), engine.survivorsSize());
        final var printer = PlotPrinter.standard(plotDecoder,
            properties.getInt("print-delay-millis"))) {

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
    final long generations = properties.getLong("ga.generations");
    if (generations >= 0) {
      return ongoing.limit(generations);
    } else {
      return ongoing;
    }
  }

  private void shutdown(final Thread main) {
    try {
      log.info("Initiating controlled application shutdown");
      stopped.set(true);
      final long shutdownMillis = properties.getInt("shutdown-wait-millis");
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
