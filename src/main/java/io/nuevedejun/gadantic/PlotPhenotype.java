package io.nuevedejun.gadantic;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

  record TiledCrop(int x, int y, Crop crop, int perk, int area, int kind, CropDecoder decoder) {

    TiledCrop(final Coordinate coordinate, final Crop crop,
        final int perk, final int area, final int kind, final CropDecoder decoder) {
      this(coordinate.x(), coordinate.y(), crop, perk, area, kind, decoder);
    }

    static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype) {
      return tiles(genotype, false);
    }

    static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled) {
      final var elements = new ArrayList<>(COORDINATES);
      if (shuffled) {
        Collections.shuffle(elements);
      }

      final var perkChromosome = genotype.get(0);
      final var areaChromosome = genotype.get(1);
      final var kindChromosome = genotype.get(2);

      return () -> new Iterator<>() {
        final Iterator<Coordinate> inner = elements.iterator();

        @Override
        public boolean hasNext() {
          return inner.hasNext();
        }

        @Override
        public TiledCrop next() {
          final var coordinate = inner.next();

          final int perk = perkChromosome.get(9 * coordinate.y() + coordinate.x()).allele();
          final int area = areaChromosome.get(9 * coordinate.y() + coordinate.x()).allele();
          final int kind = kindChromosome.get(9 * coordinate.y() + coordinate.x()).allele();

          final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
          final Crop crop = cropDecoder.get(area, kind);

          return new TiledCrop(coordinate, crop, perk, area, kind, cropDecoder);
        }
      };
    }
  }

  @RequiredArgsConstructor
  enum Border {
    UPPER_LEFT('\u250C'), UPPER_RIGHT('\u2510'), LOWER_RIGHT('\u2518'), LOWER_LEFT('\u2514'),
    LEFT_T('\u251C'), UPPER_T('\u252C'), RIGHT_T('\u2524'), LOWER_T('\u2534'),
    CROSS('\u253C'), DASH('\u2500'), PIPE('\u2502'),
    BLANK(' ');

    static final Map<Character, Border> MAP = Map.copyOf(Stream.of(Border.values())
        .collect(Collectors.toMap(b -> b.character, Function.identity())));

    static Border from(final char c) {
      log.entry((int) c);
      return log.exit(MAP.get(c));
    }

    final char character;
  }

  record Coordinate(int x, int y) {
  }

  static final List<Coordinate> COORDINATES = IntStream.range(0, 9).boxed()
      .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
      .toList();

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

  final double waterCf;
  final double weedCf;
  final double qualityCf;
  final double harvestCf;
  final double distinctCf;

  Genotype<IntegerGene> genotype() {
    return Genotype.of(
        /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
        /* area */ IntegerChromosome.of(0, 6, 9 * 9),
        /* kind */ IntegerChromosome.of(0, 6, 9 * 9));
  }

  double fitness(final Genotype<IntegerGene> genotype) {
    final Plot plot = Plot.decode(genotype);
    return waterCf * plot.water / 81.0
        + weedCf * plot.weed / 81.0
        + qualityCf * plot.quality / 81.0
        + harvestCf * plot.harvest / 81.0
        + distinctCf * plot.distinct / Crop.values().length;
  }
}
