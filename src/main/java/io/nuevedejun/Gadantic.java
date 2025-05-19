package io.nuevedejun;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
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
    final var genotype = Genotype.of(
        /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
        /* area */ IntegerChromosome.of(0, 6, 9 * 9),
        /* kind */ IntegerChromosome.of(0, 6, 9 * 9));

    final var constraint = new PlotConstraint();

    final var engine = Engine
        .builder(this::fitness, constraint.constrain(genotype))
        .constraint(constraint)
        .build();

    final Genotype<IntegerGene> result = engine.stream()
        .limit(100)
        .collect(EvolutionResult.toBestGenotype());

    log.info("Hello World:\n{}", result);
  }

  double fitness(final Genotype<IntegerGene> genotype) {
    return 0;
  }
}
