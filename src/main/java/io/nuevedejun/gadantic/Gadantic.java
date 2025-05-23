package io.nuevedejun.gadantic;

import java.util.function.Function;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
class Gadantic {

  public static void main(final String[] args) {
    try {
      new Gadantic().run();
    } catch (Exception e) {
      log.error("The application threw an uncaught exception", e);
      System.exit(1);
    }
  }

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
    final var plot = new PlotPhenotype(
        property("gadantic.fitness.water", Double::parseDouble, 1.0),
        property("gadantic.fitness.weed", Double::parseDouble, 1.0),
        property("gadantic.fitness.quality", Double::parseDouble, 0.0),
        property("gadantic.fitness.harvest", Double::parseDouble, 0.5),
        property("gadantic.fitness.distinct", Double::parseDouble, 1.0));

    final var genotype = plot.genotype();
    final var constraint = new PlotConstraint();

    final var engine = Engine
        .builder(plot::fitness, constraint.constrain(genotype))
        .constraint(constraint)
        .build();

    log.info("Starting evolution...");
    final int generations = property("gadantic.generations", Integer::parseInt, 100);
    final Genotype<IntegerGene> result = engine.stream()
        .limit(generations)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Plot:\n{}", Plot.decode(result).str());
  }
}
