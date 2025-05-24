package io.nuevedejun.gadantic;

import java.util.function.Function;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.nuevedejun.gadantic.PlotPhenotype.FitnessCoefficients;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
class Gadantic {

  public static void main(final String[] args) {
    try {
      new Gadantic().run();
    } catch (final Exception e) {
      log.error("The application threw an uncaught exception", e);
      System.exit(1);
    }
  }

  /**
   * Reads a system property and applies a mapping function to it.
   * 
   * @param name         the name of the property
   * @param mapper       the mapping function
   * @param defaultValue the value to use if the property does not exist
   * @return the property value after applying the mapping function, if the
   *         property is present. The default value otherwise.
   */
  <T> T property(final String name, final Function<String, T> mapper, final T defaultValue) {
    final String got = System.getProperty(name);
    if (got == null) {
      log.info("Property {}: {} (default)", name, defaultValue);
      return defaultValue;
    } else {
      log.info("Property {}: {}", name, got);
      return mapper.apply(got);
    }
  }

  void run() {
    final var iterableFactory = new GenotypeIterableFactory.Default();
    final var plotDecoder = new PlotDecoder.Default(iterableFactory);

    final var coefficients = new FitnessCoefficients(
        property("gadantic.fitness.water", Double::parseDouble, 1.0),
        property("gadantic.fitness.weed", Double::parseDouble, 1.0),
        property("gadantic.fitness.quality", Double::parseDouble, 0.0),
        property("gadantic.fitness.harvest", Double::parseDouble, 0.5),
        property("gadantic.fitness.distinct", Double::parseDouble, 1.0));
    final var plotPhenotype = new PlotPhenotype(plotDecoder, coefficients);

    final var constraint = new PlotConstraint(iterableFactory);

    final var genotype = plotPhenotype.genotype();
    final var engine = Engine
        .builder(plotPhenotype::fitness, constraint.constrain(genotype))
        .constraint(constraint)
        .build();

    final int generations = property("gadantic.generations", Integer::parseInt, 100);
    log.info("Starting evolution...");
    final Genotype<IntegerGene> result = engine.stream()
        .limit(generations)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Plot:\n{}", plotDecoder.decode(result).str());
  }
}
