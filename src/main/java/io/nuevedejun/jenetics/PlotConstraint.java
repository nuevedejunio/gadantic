package io.nuevedejun.jenetics;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.nuevedejun.Crop;
import io.nuevedejun.CropDecoder;

public class PlotConstraint implements Constraint<IntegerGene, Double> {

  record Coordinate(int x, int y) {
  }

  record Square(int x0, int x1, int y0, int y1, int size) {
    Square(final Coordinate coordinate, final int size) {
      this(
          coordinate.x(), coordinate.x() + size,
          coordinate.y(), coordinate.y() + size, size);
    }

    /**
     * Creates a new {@code Square} instance with the specified size,
     * using the current object's origin coordinates.
     *
     * @param size the length of the sides of the new square
     * @return a new {@code Square} with the same origin and the given size
     */
    public Square withSize(final int size) {
      return new Square(new Coordinate(x0, y0), size);
    }

    /**
     * Checks if the specified {@code Square} is completely contained within the
     * bounds defined by this object.
     *
     * @param square the {@code Square} to check for containment
     * @return {@code true} if the given square is entirely within the bounds;
     *         {@code false} otherwise
     */
    public boolean contains(final Square square) {
      return square.x0() >= x0 && square.y0() >= y0
          && square.x1() <= x1 && square.y1() <= y1;
    }
  }

  record IntArrayValue(int pos, int value) {
  }

  final Coordinate[] coordinates = IntStream.range(0, 9).boxed()
      .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
      .toArray(Coordinate[]::new);

  @Override
  public boolean test(final Phenotype<IntegerGene, Double> individual) {
    final var perkChromosome = individual.genotype().get(0);
    final var areaChromosome = individual.genotype().get(1);
    final var kindChromosome = individual.genotype().get(2);

    final var matrix = new Square[9][9];
    for (final Coordinate coordinate : coordinates) {
      final int perk = perkChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
      final int area = areaChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
      final int kind = kindChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();

      final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
      final Crop crop = cropDecoder.get(area, kind);

      final Square square = new Square(coordinate, crop.size());
      if (invalidCropTile(matrix, square)) {
        return false;
      }
      fillMatrix(matrix, square);
    }
    return true;
  }

  @Override
  public Phenotype<IntegerGene, Double> repair(
      final Phenotype<IntegerGene, Double> individual, final long generation) {
    // shuffle the coordinates to avoid bias towards the first ones
    final var shuffled = new ArrayList<>(Arrays.asList(coordinates));
    Collections.shuffle(shuffled);

    final var perkChromosome = individual.genotype().get(0);
    final var areaChromosome = individual.genotype().get(1);
    final var kindChromosome = individual.genotype().get(2);

    final var matrix = new Square[9][9];
    final List<IntArrayValue> changes = new ArrayList<>();
    for (final Coordinate coordinate : shuffled) {
      final int perk = perkChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
      final int area = areaChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
      final int kind = kindChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();

      final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
      final Crop crop = cropDecoder.get(area, kind);

      final Square square = new Square(coordinate, crop.size());
      final int valid = validCropSize(matrix, square);

      if (valid < crop.size()) {
        int decreasing = area - 1;
        while (cropDecoder.get(decreasing, kind).size() > valid) {
          decreasing--;
        }
        changes.add(new IntArrayValue(coordinate.x() * 9 + coordinate.y(), decreasing));
      }
    }

    return Phenotype.of(Genotype.of(
        perkChromosome,
        areaChromosome.as(IntegerChromosome.class).map(arr -> {
          changes.forEach(change -> arr[change.pos()] = change.value());
          return arr;
        }),
        kindChromosome), generation);
  }

  boolean invalidCropTile(final Square[][] matrix, final Square square) {
    // check if the crop fits in the plot
    if (square.x1() > 9 || square.y1() > 9) {
      return true;
    }

    // check partial overlap in crop tiles
    // this set is here to avoid checking the same square multiple times
    for (int i = square.x0(); i < square.x1(); i++) {
      for (int j = square.y0(); j < square.y1(); j++) {
        final Square current = matrix[i][j];
        if (current != null && !square.contains(current)) {
          return !current.contains(square);
        }
      }
    }
    return false;
  }

  /**
   * Mark the tiles occupied by the crop.
   * 
   * @param matrix the matrix to fill
   * @param target the square to fill with
   */
  private void fillMatrix(final Square[][] matrix, final Square target) {
    for (int i = target.x0(); i < target.x1(); i++) {
      Arrays.fill(matrix[i], target.y0(), target.y1(), target);
    }
  }

  private int validCropSize(final Square[][] matrix, final Square square) {
    final int remainRight = 9 - square.x0();
    final int remainDown = 9 - square.y0();
    int valid = min(square.size(), min(remainRight, remainDown));
    while (invalidCropTile(matrix, square.withSize(valid))) {
      valid--;
    }
    return valid;
  }
}
