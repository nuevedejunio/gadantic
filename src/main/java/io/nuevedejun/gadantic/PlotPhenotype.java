package io.nuevedejun.gadantic;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

@RequiredArgsConstructor
@XSlf4j
class PlotPhenotype {

  @RequiredArgsConstructor
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
  }

  enum Perk {
    WATER, WEED, QUALITY, HARVEST
  }

  /**
   * Decodes the crop type based on the type of perk, area, and kind.
   * <p>
   * These values do not map directly with crop characteristics, but instead where
   * chosen to guarantee a normal distribution of the probability of each crop
   * type. When new crops are added, the ranges of those parameters must be
   * adjusted.
   */
  @FunctionalInterface
  interface CropDecoder {
    static CropDecoder ofPerk(final int perk) {
      return switch (perk) {
        case 0 -> PlotPhenotype::decodeWater;
        case 1 -> PlotPhenotype::decodeWeed;
        case 2 -> PlotPhenotype::decodeQuality;
        default /* 3, 4 */ -> PlotPhenotype::decodeHarvest;
      };
    }

    Crop get(int area, int kind);
  }

  record FitnessCoefficients(double water, double weed, double quality, double harvest, double distinct) {
  }

  static Crop decodeWater(final int area, final int kind) {
    return switch (kind) {
      case 0, 1 -> Crop.TOMATOES;
      case 2, 3 -> Crop.POTATOES;
      default /* 4, 5 */ -> Crop.CABBAGE;
    };
  }

  static Crop decodeWeed(final int area, final int kind) {
    return switch (kind) {
      case 0, 1 -> Crop.CARROTS;
      case 2, 3 -> Crop.ONIONS;
      default /* 4, 5 */ -> Crop.BOK_CHOY;
    };
  }

  static Crop decodeQuality(final int area, final int kind) {
    if (area < 2) {
      return Crop.COTTON;
    } else if (kind < 3) {
      return Crop.PEPPERS;
    } else {
      return Crop.PUMPKINS;
    }
  }

  static Crop decodeHarvest(final int area, final int kind) {
    return switch (area) {
      case 0, 1, 2 -> switch (kind) {
        case 0, 1 -> Crop.RICE;
        case 2, 3 -> Crop.WHEAT;
        default /* 4, 5 */ -> Crop.CORN;
      };
      case 3, 4 -> kind < 3 ? Crop.BLUEBERRIES : Crop.BEANS;
      default /* 5 */ -> Crop.APPLES;
    };
  }

  final PlotDecoder plotDecoder;
  final FitnessCoefficients coefficients;

  Genotype<IntegerGene> genotype() {
    return Genotype.of(
        /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
        /* area */ IntegerChromosome.of(0, 6, 9 * 9),
        /* kind */ IntegerChromosome.of(0, 6, 9 * 9));
  }

  double fitness(final Genotype<IntegerGene> genotype) {
    final Plot plot = plotDecoder.decode(genotype);
    return coefficients.water() * plot.water / 81.0
        + coefficients.weed() * plot.weed / 81.0
        + coefficients.quality() * plot.quality / 81.0
        + coefficients.harvest() * plot.harvest / 81.0
        + coefficients.distinct() * plot.distinct / Crop.values().length;
  }
}
