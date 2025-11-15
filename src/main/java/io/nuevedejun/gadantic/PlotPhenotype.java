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

/**
 * Represents the phenotype of a garden plot layout in the genetic algorithm.
 * Provides encoding and fitness evaluation for crop arrangements.
 */
public interface PlotPhenotype {

  /**
   * Returns the genotype encoding for the plot.
   *
   * @return genotype with 81 integer genes representing crop types
   */
  Genotype<IntegerGene> encoding();

  /**
   * Calculates the fitness value for a given genotype.
   *
   * @param genotype the genotype to evaluate
   * @return normalized fitness value [0, 1]
   */
  double fitness(Genotype<IntegerGene> genotype);

  /**
   * Enumeration of all crop types with their size and perk.
   */
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

    /**
     * Returns the crop at the specified ordinal position.
     *
     * @param ordinal the ordinal position
     * @return the crop at that position
     */
    public static Crop at(final int ordinal) {return CROPS[ordinal];}

    /**
     * Returns the total number of crop types.
     *
     * @return the number of crops
     */
    public static int len() {return CROPS.length;}
  }


  /**
   * Enumeration of perk types that crops provide to neighbors.
   */
  enum Perk {WATER, WEED, QUALITY, HARVEST}


  /**
   * Configuration interface for fitness function coefficients.
   */
  @ConfigMapping(prefix = "fitness")
  interface FitnessCoefficients {
    /**
     * Coefficient for water retention perk coverage.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double waterRetention();

    /**
     * Coefficient for weed prevention perk coverage.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double weedPrevention();

    /**
     * Coefficient for quality boost perk coverage.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double qualityBoost();

    /**
     * Coefficient for harvest increase perk coverage.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double harvestIncrease();

    /**
     * Coefficient for unique crop diversity.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double uniqueCrops();

    /**
     * Coefficient for buff application efficiency.
     *
     * @return the coefficient value
     */
    @WithDefault("1.0")
    double buffEfficiency();

    /**
     * Coefficient for horizontal symmetry (left-right mirror).
     *
     * @return the coefficient value
     */
    @WithDefault("0.0")
    double horizontalSymmetry();

    /**
     * Coefficient for vertical symmetry (top-bottom mirror).
     *
     * @return the coefficient value
     */
    @WithDefault("0.0")
    double verticalSymmetry();

    /**
     * Coefficient for 180° rotational symmetry.
     *
     * @return the coefficient value
     */
    @WithDefault("0.0")
    double rotationalSymmetry();
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
          + coefficients.buffEfficiency()
          + coefficients.horizontalSymmetry()
          + coefficients.verticalSymmetry()
          + coefficients.rotationalSymmetry();
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
          + coefficients.buffEfficiency() * plot.efficiency()
          + coefficients.horizontalSymmetry() * plot.horizontalSymmetry()
          + coefficients.verticalSymmetry() * plot.verticalSymmetry()
          + coefficients.rotationalSymmetry() * plot.rotationalSymmetry()) / normalize;
    }
  }
}
