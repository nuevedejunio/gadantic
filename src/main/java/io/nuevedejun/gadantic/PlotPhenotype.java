package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.XSlf4j;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

public interface PlotPhenotype {

  Genotype<IntegerGene> encoding();

  double fitness(Genotype<IntegerGene> genotype);

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  enum Crop {
    // single-tile crops
    TOMATOES(1, WATER), POTATOES(1, WATER), CABBAGE(1, WATER), RICE(1, HARVEST), WHEAT(1, HARVEST),
    CORN(1, HARVEST), CARROTS(1, WEED), ONIONS(1, WEED), BOK_CHOY(1, WEED), COTTON(1, QUALITY),
    // 4-tile crops
    BLUEBERRIES(2, HARVEST), BEANS(2, HARVEST), PEPPERS(2, QUALITY), PUMPKINS(2, QUALITY),
    // 9-tile crops
    APPLES(3, HARVEST);

    private final int size;
    private final Perk perk;

    private static final Crop[] CROPS = values();

    public static Crop at(final int ordinal) {return CROPS[ordinal];}

    public static int len() {return CROPS.length;}
  }


  enum Perk {WATER, WEED, QUALITY, HARVEST}


  record FitnessCoefficients(
      double water, double weed, double quality, double harvest,
      double distinct, double efficiency) {
  }

  static Impl create(final PlotDecoder plotDecoder,
      final FitnessCoefficients coefficients) {
    return new Impl(plotDecoder, coefficients);
  }

  @XSlf4j
  class Impl implements PlotPhenotype {
    private final PlotDecoder plotDecoder;
    private final FitnessCoefficients coefficients;
    private final double normalize;

    private Impl(final PlotDecoder plotDecoder, final FitnessCoefficients coefficients) {
      this.plotDecoder = plotDecoder;
      this.coefficients = coefficients;
      this.normalize = coefficients.water()
          + coefficients.weed()
          + coefficients.quality()
          + coefficients.harvest()
          + coefficients.distinct()
          + coefficients.efficiency();
    }

    @Override
    public Genotype<IntegerGene> encoding() {
      return Genotype.of(IntegerChromosome.of(0, Crop.len(), 9 * 9));
    }

    @Override
    public double fitness(final Genotype<IntegerGene> genotype) {
      final Plot plot = plotDecoder.decode(genotype);
      return (coefficients.water() * plot.water() / 81
          + coefficients.weed() * plot.weed() / 81
          + coefficients.quality() * plot.quality() / 81
          + coefficients.harvest() * plot.harvest() / 81
          + coefficients.distinct() * plot.distinct() / Crop.len()
          + coefficients.efficiency() * plot.efficiency()) / normalize;
    }
  }
}
