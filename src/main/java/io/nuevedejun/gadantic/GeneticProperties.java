package io.nuevedejun.gadantic;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "ga")
public interface GeneticProperties {

  @WithDefault("-1")
  int generations();

  @WithDefault("50")
  int populationSize();

  @WithDefault("5")
  int tournamentSize();

  @WithDefault("0.6")
  double offspringFraction();

  @WithDefault("0.2")
  double shuffleProbability();

  @WithDefault("0.2")
  double crossoverProbability();

  @WithDefault("0.2")
  double swapProbability();

  @WithDefault("0.01")
  double mutationProbability();

  @WithDefault("0.01")
  double replacementProbability();
}
