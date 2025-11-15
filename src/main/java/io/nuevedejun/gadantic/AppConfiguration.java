package io.nuevedejun.gadantic;

import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.ShuffleMutator;
import io.jenetics.TournamentSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.Engine;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Application configuration that produces CDI beans for the genetic algorithm engine.
 */
@ApplicationScoped
public class AppConfiguration {

  private final GeneticProperties properties;

  AppConfiguration(final GeneticProperties properties) {
    this.properties = properties;
  }

  @Produces
  TournamentSelector<IntegerGene, Double> selector() {
    return new TournamentSelector<>(properties.tournamentSize());
  }

  @Produces
  Engine<IntegerGene, Double> engine(
      final PlotPhenotype plotPhenotype,
      final Constraint<IntegerGene, Double> constraint,
      final TournamentSelector<IntegerGene, Double> selector) {
    Log.info("Creating evolution engine");
    return Engine
        .builder(plotPhenotype::fitness, constraint.constrain(plotPhenotype.encoding()))
        .constraint(constraint)
        .populationSize(properties.populationSize())
        .offspringFraction(properties.offspringFraction())
        .survivorsSelector(selector)
        .offspringSelector(new RouletteWheelSelector<>())
        .alterers(
            new ShuffleMutator<>(properties.shuffleProbability()),
            new UniformCrossover<>(
                properties.crossoverProbability(),
                properties.swapProbability()),
            new Mutator<>(properties.mutationProbability()),
            new ReplacementMutator(properties.replacementProbability()))
        .build();
  }
}
