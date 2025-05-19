package io.nuevedejun;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.nuevedejun.jenetics.PlotConstraint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Gadantic {
  public static void main(final String[] args) {
    new Gadantic().run();
  }

  void run() {
    final var plot = new PlotPhenotype();
    final var genotype = plot.genotype();
    final var constraint = new PlotConstraint();

    final var engine = Engine
        .builder(plot::fitness, constraint.constrain(genotype))
        .constraint(constraint)
        .build();

    final Genotype<IntegerGene> result = engine.stream()
        .limit(100)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Hello World:\n{}", result);
  }
}
