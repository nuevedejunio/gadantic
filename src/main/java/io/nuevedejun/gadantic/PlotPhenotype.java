package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.enterprise.context.ApplicationScoped;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

public interface PlotPhenotype {

  Genotype<IntegerGene> encoding();

  double fitness(Genotype<IntegerGene> genotype);

  enum Crop {
    // single-tile crops
    TOMATOES(1, WATER), POTATOES(1, WATER), CABBAGE(1, WATER), RICE(1, HARVEST), WHEAT(1, HARVEST),
    CORN(1, HARVEST), CARROTS(1, WEED), ONIONS(1, WEED), BOK_CHOY(1, WEED), COTTON(1, QUALITY),
    // 4-tile crops
    BLUEBERRIES(2, HARVEST), BEANS(2, HARVEST), PEPPERS(2, QUALITY), PUMPKINS(2, QUALITY),
    // 9-tile crops
    APPLES(3, HARVEST);

    final int size;
    final Perk perk;

    Crop(final int size, final Perk perk) {
      this.size = size;
      this.perk = perk;
    }

    private static final Crop[] CROPS = values();

    public static Crop at(final int ordinal) {return CROPS[ordinal];}

    public static int len() {return CROPS.length;}
  }


  enum Perk {WATER, WEED, QUALITY, HARVEST}


  @ConfigMapping(prefix = "fitness")
  interface FitnessCoefficients {
    @WithDefault("1.0")
    double waterRetention();

    @WithDefault("1.0")
    double weedPrevention();

    @WithDefault("1.0")
    double qualityBoost();

    @WithDefault("1.0")
    double harvestIncrease();

    @WithDefault("1.0")
    double uniqueCrops();

    @WithDefault("1.0")
    double buffEfficiency();
  }


  @ApplicationScoped
  class Impl implements PlotPhenotype {
    private final PlotDecoder plotDecoder;
    private final FitnessCoefficients coefficients;
    private final double normalize;

    Impl(final PlotDecoder plotDecoder, final FitnessCoefficients coefficients) {
      this.plotDecoder = plotDecoder;
      this.coefficients = coefficients;
      this.normalize = coefficients.waterRetention()
          + coefficients.weedPrevention()
          + coefficients.qualityBoost()
          + coefficients.harvestIncrease()
          + coefficients.uniqueCrops()
          + coefficients.buffEfficiency();
    }

    @Override
    public Genotype<IntegerGene> encoding() {
      return Genotype.of(IntegerChromosome.of(0, Crop.len(), 9 * 9));
    }

    @Override
    public double fitness(final Genotype<IntegerGene> genotype) {
      final Plot plot = plotDecoder.decode(genotype);
      return (coefficients.waterRetention() * plot.water() / 81
          + coefficients.weedPrevention() * plot.weed() / 81
          + coefficients.qualityBoost() * plot.quality() / 81
          + coefficients.harvestIncrease() * plot.harvest() / 81
          + coefficients.uniqueCrops() * plot.unique() / Crop.len()
          + coefficients.buffEfficiency() * plot.efficiency()) / normalize;
    }
  }
}
