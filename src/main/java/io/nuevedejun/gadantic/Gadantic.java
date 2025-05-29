package io.nuevedejun.gadantic;

import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.ShuffleMutator;
import io.jenetics.TournamentSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.engine.EvolutionStream;
import io.nuevedejun.gadantic.PlotPhenotype.FitnessCoefficients;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.nuevedejun.gadantic.Iterables.arr;

public class Gadantic implements QuarkusApplication {
  public static final String LOG_FQCN = Log.class.getName();

  private static final Properties PROPERTIES = Properties.file();

  private static final AtomicBoolean STOPPED = new AtomicBoolean(false);
  private static final AtomicReference<Thread> MAIN_THREAD =
      new AtomicReference<>(Thread.currentThread());

  @Override
  public int run(final String[] args) {
    Log.info("Initializing application");
    MAIN_THREAD.set(Thread.currentThread());

    final var plotDecoder = PlotDecoder.create();

    final var coefficients = new FitnessCoefficients(
        PROPERTIES.getDouble("weights.water-retention"),
        PROPERTIES.getDouble("weights.weed-prevention"),
        PROPERTIES.getDouble("weights.quality-boost"),
        PROPERTIES.getDouble("weights.harvest-increase"),
        PROPERTIES.getDouble("weights.unique-crops"),
        PROPERTIES.getDouble("weights.buff-efficiency"));
    final var plotPhenotype = PlotPhenotype.create(plotDecoder, coefficients);

    final var iterableShuffler = Iterables.Shuffler.create();
    final var constraint = PlotConstraint.create(iterableShuffler);

    final var encoding = plotPhenotype.encoding();

    Log.info("Creating evolution engine");
    final var engine = Engine
        .builder(plotPhenotype::fitness, constraint.constrain(encoding))
        .constraint(constraint)
        .populationSize(PROPERTIES.getInt("ga.population-size"))
        .offspringFraction(PROPERTIES.getDouble("ga.offspring-fraction"))
        .survivorsSelector(new TournamentSelector<>(PROPERTIES.getInt("ga.tournament-size")))
        .offspringSelector(new RouletteWheelSelector<>())
        .alterers(
            new ShuffleMutator<>(PROPERTIES.getDouble("ga.shuffle-probability")),
            new UniformCrossover<>(
                PROPERTIES.getDouble("ga.crossover-probability"),
                PROPERTIES.getDouble("ga.swap-probability")),
            new Mutator<>(PROPERTIES.getDouble("ga.mutation-probability")),
            new ReplacementMutator(PROPERTIES.getDouble("ga.replacement-probability")))
        .build();

    try (final var persistence = EvolutionPersistence.file(
        Path.of(PROPERTIES.getString("save-file")),
        engine.survivorsSelector(), engine.survivorsSize());
        final var printer = PlotLogger.standard(plotDecoder,
            PROPERTIES.getInt("print-delay-millis"))) {

      Log.info("Loading result of previous execution");
      final EvolutionStart<IntegerGene, Double> start = persistence.read();

      Log.info("Initiating evolution");
      final EvolutionStream<IntegerGene, Double> stream = engine.stream(start);
      final EvolutionResult<IntegerGene, Double> result = limitStream(stream)
          .peek(printer::accept) //NOSONAR java:S3864 peek is ok for this use case
          .collect(EvolutionResult.toBestEvolutionResult());

      Log.info("Evolution terminated. Saving result");
      persistence.write(result);
      Log.debug("Finished saving evolution result");

      printer.print(result.bestPhenotype());
    }
    return 0;
  }

  private Stream<EvolutionResult<IntegerGene, Double>> limitStream(
      final EvolutionStream<IntegerGene, Double> stream) {
    final var ongoing = stream.limit(r -> !STOPPED.get());
    final long generations = Gadantic.PROPERTIES.getLong("ga.generations");
    if (generations >= 0) {
      return ongoing.limit(generations);
    } else {
      return ongoing;
    }
  }

  public static void shutdown() {
    try {
      Log.info("Initiating controlled application shutdown");
      STOPPED.set(true);
      final long shutdownMillis = PROPERTIES.getInt("shutdown-wait-millis");
      if (!MAIN_THREAD.get().join(Duration.ofMillis(shutdownMillis))) {
        Log.warn(LOG_FQCN, "The main thread did not complete within {0} ms. Data may be lost",
            arr(shutdownMillis), null);
      }
    } catch (final InterruptedException e) {
      Log.warn("Shutdown was interrupted while waiting for the main thread to complete.");
      Thread.currentThread().interrupt();
    }
  }
}
