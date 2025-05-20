package io.nuevedejun;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.nuevedejun.PlotPhenotype.CropDecoder;
import io.nuevedejun.PlotPhenotype.TiledCrop;

class PlotConstraint implements Constraint<IntegerGene, Double> {

  record Square(int x0, int x1, int y0, int y1, int size) {
    Square(final int x, final int y, final int size) {
      this(x, x + size, y, y + size, size);
    }

    static Square of(final TiledCrop tile) {
      return new Square(tile.x(), tile.y(), tile.crop().size);
    }

    /**
     * Creates a new {@code Square} instance with the specified size,
     * using the current object's origin coordinates.
     *
     * @param size the length of the sides of the new square
     * @return a new {@code Square} with the same origin and the given size
     */
    Square withSize(final int size) {
      return new Square(x0, y0, size);
    }

    /**
     * Checks if the specified {@code Square} is completely contained within the
     * bounds defined by this object.
     *
     * @param square the {@code Square} to check for containment
     * @return {@code true} if the given square is entirely within the bounds;
     *         {@code false} otherwise
     */
    boolean contains(final Square square) {
      return square.x0() >= x0 && square.y0() >= y0
          && square.x1() <= x1 && square.y1() <= y1;
    }
  }

  record IntArrayValue(int pos, int value) {
  }

  @Override
  public boolean test(final Phenotype<IntegerGene, Double> individual) {
    final var matrix = new Square[9][9];
    for (final var tile : TiledCrop.tiles(individual.genotype())) {
      final Square square = Square.of(tile);
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

    final var matrix = new Square[9][9];
    final List<IntArrayValue> changes = new ArrayList<>();
    // shuffle the coordinates to avoid bias towards the first ones
    for (final var tile : TiledCrop.tiles(individual.genotype(), true)) {
      final Square square = Square.of(tile);
      final int valid = validCropSize(matrix, square);

      if (valid < tile.crop().size) {
        int decreasing = tile.area() - 1;
        final CropDecoder cropDecoder = tile.decoder();
        while (cropDecoder.get(decreasing, tile.kind()).size > valid) {
          decreasing--;
        }
        changes.add(new IntArrayValue(tile.x() * 9 + tile.y(), decreasing));
      }
    }

    final var perkChromosome = individual.genotype().get(0);
    final var areaChromosome = individual.genotype().get(1);
    final var kindChromosome = individual.genotype().get(2);
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
  void fillMatrix(final Square[][] matrix, final Square target) {
    for (int i = target.x0(); i < target.x1(); i++) {
      Arrays.fill(matrix[i], target.y0(), target.y1(), target);
    }
  }

  int validCropSize(final Square[][] matrix, final Square square) {
    final int remainRight = 9 - square.x0();
    final int remainDown = 9 - square.y0();
    int valid = min(square.size(), min(remainRight, remainDown));
    while (invalidCropTile(matrix, square.withSize(valid))) {
      valid--;
    }
    fillMatrix(matrix, square.withSize(valid));
    return valid;
  }
}
