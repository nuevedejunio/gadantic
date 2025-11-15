package io.nuevedejun.gadantic;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for genetic algorithm parameters.
 */
@ConfigMapping(prefix = "ga")
public interface GeneticProperties {

  /**
   * Maximum number of generations to evolve (-1 for unlimited).
   *
   * @return the generation limit
   */
  @WithDefault("-1")
  int generations();

  /**
   * Size of the population in each generation.
   *
   * @return the population size
   */
  @WithDefault("50")
  int populationSize();

  /**
   * Number of individuals competing in tournament selection.
   *
   * @return the tournament size
   */
  @WithDefault("5")
  int tournamentSize();

  /**
   * Fraction of population replaced by offspring each generation.
   *
   * @return the offspring fraction
   */
  @WithDefault("0.6")
  double offspringFraction();

  /**
   * Probability of applying shuffle mutation.
   *
   * @return the shuffle probability
   */
  @WithDefault("0.2")
  double shuffleProbability();

  /**
   * Probability of applying crossover.
   *
   * @return the crossover probability
   */
  @WithDefault("0.2")
  double crossoverProbability();

  /**
   * Probability of swapping genes during crossover.
   *
   * @return the swap probability
   */
  @WithDefault("0.2")
  double swapProbability();

  /**
   * Probability of applying point mutation.
   *
   * @return the mutation probability
   */
  @WithDefault("0.01")
  double mutationProbability();

  /**
   * Probability of replacing an individual entirely.
   *
   * @return the replacement probability
   */
  @WithDefault("0.01")
  double replacementProbability();
}
