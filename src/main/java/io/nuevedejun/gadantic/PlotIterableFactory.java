package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.Perk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

interface PlotIterableFactory {
  record Coordinate(int x, int y) {
  }


  record TiledCrop(int x, int y, Crop crop, int perk, int area, int kind, Perk decoder) {
    TiledCrop(final Coordinate coordinate, final Crop crop,
        final int perk, final int area, final int kind, final Perk decoder) {
      this(coordinate.x(), coordinate.y(), crop, perk, area, kind, decoder);
    }
  }

  default Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype) {
    return tiles(genotype, false);
  }

  Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled);

  static PlotIterableFactory standard() {
    return new Standard();
  }

  class Standard implements PlotIterableFactory {
    private static final List<Coordinate> COORDINATES = IntStream.range(0, 9).boxed()
        .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
        .toList();

    private Standard() {}

    @Override
    public Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled) {
      final var elements = new ArrayList<>(COORDINATES);
      if (shuffled) {
        Collections.shuffle(elements);
      }

      final var perkChromosome = genotype.get(0);
      final var areaChromosome = genotype.get(1);
      final var kindChromosome = genotype.get(2);

      return () -> new Iterator<>() {
        private final Iterator<Coordinate> inner = elements.iterator();

        @Override
        public boolean hasNext() {
          return inner.hasNext();
        }

        @Override
        public TiledCrop next() {
          final var coordinate = inner.next();

          final int index = 9 * coordinate.y() + coordinate.x();
          final int perk = perkChromosome.get(index).allele();
          final int area = areaChromosome.get(index).allele();
          final int kind = kindChromosome.get(index).allele();

          final Perk cropDecoder = Perk.of(perk);
          final Crop crop = cropDecoder.get(area, kind);

          return new TiledCrop(coordinate, crop, perk, area, kind, cropDecoder);
        }
      };
    }
  }
}
