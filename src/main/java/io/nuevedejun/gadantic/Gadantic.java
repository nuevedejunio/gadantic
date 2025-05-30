package io.nuevedejun.gadantic;

import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.engine.EvolutionStream;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.nuevedejun.gadantic.Iterables.arr;

@QuarkusMain
public class Gadantic implements QuarkusApplication {
  public static final String LOG_FQCN = Log.class.getName();

  private final GeneticProperties properties;
  private final int shutdownMillis;
  private final Engine<IntegerGene, Double> engine;
  private final EvolutionPersistence persistence;
  private final PlotPrinter printer;

  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final AtomicReference<Thread> mainThread = new AtomicReference<>(Thread.currentThread());

  Gadantic(final GeneticProperties properties,
      @ConfigProperty(name = "shutdown-wait-millis", defaultValue = "500") final int shutdownMillis,
      final Engine<IntegerGene, Double> engine, final EvolutionPersistence persistence,
      final PlotPrinter printer) {
    this.properties = properties;
    this.shutdownMillis = shutdownMillis;
    this.engine = engine;
    this.persistence = persistence;
    this.printer = printer;
  }

  @Override
  public int run(final String[] args) {
    Log.info("Initializing application");
    mainThread.set(Thread.currentThread());

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
    return 0;
  }

  private Stream<EvolutionResult<IntegerGene, Double>> limitStream(
      final EvolutionStream<IntegerGene, Double> stream) {
    final var ongoing = stream.limit(r -> !stopped.get());
    final long generations = properties.generations();
    if (generations >= 0) {
      return ongoing.limit(generations);
    } else {
      return ongoing;
    }
  }

  void onStop(@Observes final ShutdownEvent ignored) {
    try {
      Log.info("Initiating controlled application shutdown");
      stopped.set(true);
      if (!mainThread.get().join(Duration.ofMillis(shutdownMillis))) {
        Log.warn(LOG_FQCN, "The main thread did not complete within {0} ms. Data may be lost",
            arr(shutdownMillis), null);
      }
    } catch (final InterruptedException e) {
      Log.warn("Shutdown was interrupted while waiting for the main thread to complete.");
      Thread.currentThread().interrupt();
    }
  }
}
