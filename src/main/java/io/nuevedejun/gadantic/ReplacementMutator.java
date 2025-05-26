package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;

import java.util.random.RandomGenerator;

public class ReplacementMutator extends Mutator<IntegerGene, Double> {
  public ReplacementMutator(final double probability) {
    super(probability);
  }

  @Override
  protected MutatorResult<Genotype<IntegerGene>> mutate(
      final Genotype<IntegerGene> genotype, final double p, final RandomGenerator random) {
    final var replacement = genotype.newInstance();
    return new MutatorResult<>(replacement, replacement.length());
  }
}
