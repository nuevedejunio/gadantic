package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.nuevedejun.gadantic.PlotPhenotype.FitnessCoefficients;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
public class Gadantic {

  public static void main(final String[] args) {
    try {
      new Gadantic().run();
    } catch (final Exception e) {
      log.error("The application threw an uncaught exception", e);
      System.exit(1);
    }
  }

  public void run() {
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
        .build();

    log.info("Starting evolution...");
    final Genotype<IntegerGene> result = engine.stream()
        .limit(100)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Plot:\n{}", plotDecoder.decode(result).str());
  }
}
