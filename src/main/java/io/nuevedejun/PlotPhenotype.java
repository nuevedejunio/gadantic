package io.nuevedejun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public class PlotPhenotype {

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  public enum Crop {
    // single-tile crops
    TOMATOES(1), POTATOES(1), CABBAGE(1), RICE(1), WHEAT(1),
    CORN(1), CARROTS(1), ONIONS(1), BOK_CHOY(1), COTTON(1),
    // 4-tile crops
    BLUEBERRIES(2), BEANS(2), PEPPERS(2), PUMPKINS(2),
    // 9-tile crops
    APPLES(3);

    private final int size;
  }

  @FunctionalInterface
  public interface CropDecoder {
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

  public record TiledCrop(int x, int y, Crop crop, int perk, int area, int kind, CropDecoder decoder) {

    public TiledCrop(final Coordinate coordinate, final Crop crop,
        final int perk, final int area, final int kind, final CropDecoder decoder) {
      this(coordinate.x(), coordinate.y(), crop, perk, area, kind, decoder);
    }

    public static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype) {
      return tiles(genotype, false);
    }

    public static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled) {
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

          final int perk = perkChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
          final int area = areaChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
          final int kind = kindChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();

          final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
          final Crop crop = cropDecoder.get(area, kind);

          return new TiledCrop(coordinate, crop, perk, area, kind, cropDecoder);
        }
      };
    }
  }

  record Coordinate(int x, int y) {
  }

  private static final List<Coordinate> COORDINATES = IntStream.range(0, 9).boxed()
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

  Genotype<IntegerGene> genotype() {
    return Genotype.of(
        /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
        /* area */ IntegerChromosome.of(0, 6, 9 * 9),
        /* kind */ IntegerChromosome.of(0, 6, 9 * 9));
  }

  double fitness(final Genotype<IntegerGene> genotype) {
    return 0;
  }
}
