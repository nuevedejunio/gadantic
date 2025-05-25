package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import lombok.AccessLevel;
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
  }

  /**
   * Decodes the crop type based on the type of perk, area, and kind.
   * <p>
   * These values do not map directly with crop characteristics, but instead where chosen to
   * guarantee a normal distribution of the probability of each crop type. When new crops are
   * added, the ranges of those parameters must be adjusted.
   */
  interface CropDecoder {
    Crop get(int area, int kind);
  }


  enum Perk implements CropDecoder {
    WATER {
      @Override
      public Crop get(final int area, final int kind) {
        return switch (kind) {
          case 0, 1 -> Crop.TOMATOES;
          case 2, 3 -> Crop.POTATOES;
          default /* 4, 5 */ -> Crop.CABBAGE;
        };
      }
    },
    WEED {
      @Override
      public Crop get(final int area, final int kind) {
        return switch (kind) {
          case 0, 1 -> Crop.CARROTS;
          case 2, 3 -> Crop.ONIONS;
          default /* 4, 5 */ -> Crop.BOK_CHOY;
        };
      }
    },
    QUALITY {
      @Override
      public Crop get(final int area, final int kind) {
        if (area < 2) {
          return Crop.COTTON;
        } else if (kind < 3) {
          return Crop.PEPPERS;
        } else {
          return Crop.PUMPKINS;
        }
      }
    },
    HARVEST {
      @Override
      public Crop get(final int area, final int kind) {
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
    };

    public static Perk of(final int perk) {
      return switch (perk) {
        case 0 -> WATER;
        case 1 -> WEED;
        case 2 -> QUALITY;
        default /* 3, 4 */ -> HARVEST;
      };
    }
  }


  record FitnessCoefficients(
      double water, double weed, double quality, double harvest,
      double distinct) {
  }

  static Standard standard(final PlotDecoder plotDecoder,
      final FitnessCoefficients coefficients) {
    return new Standard(plotDecoder, coefficients);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @XSlf4j
  class Standard implements PlotPhenotype {
    private final PlotDecoder plotDecoder;
    private final FitnessCoefficients coefficients;

    @Override
    public Genotype<IntegerGene> encoding() {
      return Genotype.of(
          /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
          /* area */ IntegerChromosome.of(0, 6, 9 * 9),
          /* kind */ IntegerChromosome.of(0, 6, 9 * 9));
    }

    @Override
    public double fitness(final Genotype<IntegerGene> genotype) {
      final Plot plot = plotDecoder.decode(genotype);
      return coefficients.water() * plot.water() / 81.0
          + coefficients.weed() * plot.weed() / 81.0
          + coefficients.quality() * plot.quality() / 81.0
          + coefficients.harvest() * plot.harvest() / 81.0
          + coefficients.distinct() * plot.distinct() / Crop.values().length;
    }
  }
}
