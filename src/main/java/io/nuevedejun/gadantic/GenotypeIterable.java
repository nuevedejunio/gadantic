package io.nuevedejun.gadantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.CropDecoder;

interface GenotypeIterableFactory {
  record Coordinate(int x, int y) {
  }

  record TiledCrop(int x, int y, Crop crop, int perk, int area, int kind, CropDecoder decoder) {
    TiledCrop(final Coordinate coordinate, final Crop crop,
        final int perk, final int area, final int kind, final CropDecoder decoder) {
      this(coordinate.x(), coordinate.y(), crop, perk, area, kind, decoder);
    }
  }

  class Default implements GenotypeIterableFactory {
    final List<Coordinate> coordinates = IntStream.range(0, 9).boxed()
        .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
        .toList();

    @Override
    public Iterable<TiledCrop> tiles(Genotype<IntegerGene> genotype, boolean shuffled) {
      final var elements = new ArrayList<>(coordinates);
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

          final int index = 9 * coordinate.y() + coordinate.x();
          final int perk = perkChromosome.get(index).allele();
          final int area = areaChromosome.get(index).allele();
          final int kind = kindChromosome.get(index).allele();

          final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
          final Crop crop = cropDecoder.get(area, kind);

          return new TiledCrop(coordinate, crop, perk, area, kind, cropDecoder);
        }
      };
    }
  }

  default Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype) {
    return tiles(genotype, false);
  }

  Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled);
}
