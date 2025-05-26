package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;

import java.util.Arrays;

class PlotCodec {
  static Phenotype<IntegerGene, Double> encode(final int[] genes) {
    final var chromosome = IntegerChromosome.of(Arrays.stream(genes)
        .mapToObj(i -> IntegerGene.of(i, 0, 15)).toList());
    return Phenotype.of(Genotype.of(chromosome), 1);
  }

  static int[] decode(final Phenotype<IntegerGene, Double> phenotype) {
    return phenotype.genotype().chromosome().as(IntegerChromosome.class).toArray();
  }
}
