package io.nuevedejun.gadantic;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.stream.IntStream;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.CropDecoder;

public class PrioritizedIterableFactory implements GenotypeIterableFactory {
  final LinkedHashSet<Coordinate> coordinates = IntStream.range(0, 9).boxed()
      .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
      .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

  @Override
  public Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled) {
    final var perkChromosome = genotype.get(0);
    final var areaChromosome = genotype.get(1);
    final var kindChromosome = genotype.get(2);

    return () -> new Iterator<>() {
      final Iterator<Coordinate> inner = coordinates.iterator();

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

  /**
   * Add the provided coordinates to the beginning of the iterator.
   * 
   * @param priority set of coordinates to move to the beginning of the iteration.
   *                 Iteration will occur in the same order that coordinates
   *                 appear in the provided set.
   * @throws IllegalArgumentException if any coordinate is out of the original set
   *                                  of coordinates
   */
  void prioritize(SequencedSet<Coordinate> priority) {
    for (var coordinate : priority.reversed()) {
      if (!coordinates.contains(coordinate)) {
        throw new IllegalArgumentException(coordinate + " is not in the allowed coordinates");
      }
      coordinates.addFirst(coordinate);
    }
  }
}
