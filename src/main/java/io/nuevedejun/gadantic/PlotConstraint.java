package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.nuevedejun.gadantic.PlotIterableFactory.TiledCrop;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.CropDecoder;
import io.nuevedejun.gadantic.PlotPhenotype.Perk;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@XSlf4j
public class PlotConstraint implements Constraint<IntegerGene, Double> {

  public static PlotConstraint create(final PlotIterableFactory iterableFactory) {
    return new PlotConstraint(iterableFactory);
  }

  private record Square(int x0, int x1, int y0, int y1, int size) {
    private Square(final int x, final int y, final int size) {
      this(x, x + size, y, y + size, size);
    }

    private static Square of(final TiledCrop tile) {
      return new Square(tile.x(), tile.y(), tile.crop().size());
    }

    /**
     * Creates a new {@code Square} instance with the specified size, using the current object's
     * origin coordinates.
     *
     * @param size the length of the sides of the new square
     * @return a new {@code Square} with the same origin and the given size
     */
    private Square withSize(final int size) {
      return new Square(x0, y0, size);
    }

    /**
     * Checks if the specified {@code Square} is completely contained within the bounds defined by
     * this object.
     *
     * @param square the {@code Square} to check for containment
     * @return {@code true} if the given square is entirely within the bounds; {@code false}
     * otherwise
     */
    private boolean contains(final Square square) {
      return square.x0() >= x0 && square.y0() >= y0
          && square.x1() <= x1 && square.y1() <= y1;
    }
  }


  private record IntArrayValue(int pos, int value) {
  }


  private enum TileCheckResult {
    OVERRIDE, REJECT, IGNORE
  }


  private final PlotIterableFactory iterableFactory;

  @Override
  public boolean test(final Phenotype<IntegerGene, Double> individual) {
    log.entry(pretty(individual));

    final var matrix = new Square[9][9];
    for (final var tile : iterableFactory.tiles(individual.genotype())) {
      final Square square = Square.of(tile);
      switch (checkCropTile(matrix, square)) {
        case REJECT:
          return log.exit(false);
        case OVERRIDE:
          fillMatrix(matrix, square);
          break;
        case IGNORE:
          // do nothing
          break;
      }
    }
    log.atTrace().log(() -> "Approved plot: " + pretty(individual.genotype()));
    return log.exit(true);
  }

  @Override
  public Phenotype<IntegerGene, Double> repair(
      final Phenotype<IntegerGene, Double> individual, final long generation) {
    log.entry(pretty(individual), generation);

    final var matrix = new Square[9][9];
    final List<IntArrayValue> changes = new ArrayList<>();
    // shuffle the coordinates to avoid bias towards the first ones
    for (final var tile : iterableFactory.tiles(individual.genotype(), true)) {
      final Square square = Square.of(tile);
      final int valid = validCropSize(matrix, square);
      log.trace("Valid size for {} is {}", square, valid);

      final CropDecoder cropDecoder = tile.decoder();
      int decreasing = tile.area();
      Crop replacement = tile.crop();
      boolean replace = false;
      while (replacement.size() > valid) {
        log.trace("Crop {} is invalid in position ({}, {}). Current [area] is {}.",
            replacement, tile.x(), tile.y(), decreasing);
        decreasing--;
        replacement = cropDecoder.get(decreasing, tile.kind());
        replace = true;
      }
      if (replace) {
        changes.add(new IntArrayValue(9 * tile.y() + tile.x(), decreasing));
      }
    }

    final var perkChromosome = individual.genotype().get(0);
    final var areaChromosome = individual.genotype().get(1);
    final var kindChromosome = individual.genotype().get(2);
    final var fixed = Genotype.of(
        perkChromosome,
        areaChromosome.as(IntegerChromosome.class).map(arr -> {
          changes.forEach(change -> arr[change.pos()] = change.value());
          return arr;
        }),
        kindChromosome);
    log.atTrace().log(() -> "Repaired plot: " + pretty(fixed));
    return log.exit(Phenotype.of(fixed, generation));
  }

  /**
   * Wraps a phenotype to implement a prettier {@link #toString()}.
   *
   * @param phenotype the phenotype to wrap
   * @return the wrapped phenotype
   */
  private Object pretty(final Phenotype<IntegerGene, Double> phenotype) {
    return new Object() {
      @Override
      public String toString() {
        final var sb = new StringBuilder().append(pretty(phenotype.genotype()));
        phenotype.fitnessOptional().ifPresent(f -> sb.append(" -> ").append(f));
        return sb.toString();
      }
    };
  }

  private Object pretty(final Genotype<IntegerGene> genotype) {
    return new Object() {
      @Override
      public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Raw genotype=").append(genotype).append('\n');
        final var perkChromosome = genotype.get(0);
        final var areaChromosome = genotype.get(1);
        final var kindChromosome = genotype.get(2);
        sb.append("Pretty genotype");
        for (int j = 0; j < 9; j++) {
          sb.append("\n");
          for (int i = 0; i < 9; i++) {
            final int perk = perkChromosome.get(j * 9 + i).allele();
            final int area = areaChromosome.get(j * 9 + i).allele();
            final int kind = kindChromosome.get(j * 9 + i).allele();
            final Crop crop = Perk.of(perk).get(area, kind);
            sb.append(String.format("%4.4s(%d,%d,%d) ", crop, perk, area, kind));
          }
        }
        return sb.toString();
      }
    };
  }

  private TileCheckResult checkCropTile(final Square[][] matrix, final Square square) {
    // check if the crop fits in the plot
    if (square.x1() > 9 || square.y1() > 9) {
      return TileCheckResult.REJECT;
    }
    // check partial overlap in crop tiles
    for (int i = square.x0(); i < square.x1(); i++) {
      for (int j = square.y0(); j < square.y1(); j++) {
        final Square current = matrix[i][j];
        if (current != null && !square.contains(current)) {
          if (current.contains(square)) {
            return TileCheckResult.IGNORE;
          } else {
            return TileCheckResult.REJECT;
          }
        }
      }
    }
    return TileCheckResult.OVERRIDE;
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
    TileCheckResult result;
    do {
      log.trace("Attempting to fit {}. Current [valid] is {}", square, valid);
      result = checkCropTile(matrix, square.withSize(valid));
      valid--;
    } while (result == TileCheckResult.REJECT);

    valid++; // undo last decrement
    if (result == TileCheckResult.OVERRIDE) {
      fillMatrix(matrix, square.withSize(valid));
    }
    return valid;
  }
}
