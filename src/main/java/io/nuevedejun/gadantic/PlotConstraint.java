package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.nuevedejun.gadantic.Iterables.Cell;
import io.nuevedejun.gadantic.Iterables.Grid;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.jenetics.util.RandomRegistry.random;
import static io.nuevedejun.gadantic.Gadantic.LOG_FQCN;
import static io.nuevedejun.gadantic.Iterables.arr;
import static io.nuevedejun.gadantic.Iterables.coordinates;
import static io.nuevedejun.gadantic.Iterables.grid;
import static java.lang.Math.min;

/**
 * Constraint that validates and repairs invalid plot layouts.
 * Ensures crops don't overlap and stay within grid bounds.
 */
@ApplicationScoped
public class PlotConstraint implements Constraint<IntegerGene, Double> {
  private final Iterables.Shuffler shuffler;

  PlotConstraint(final Iterables.Shuffler shuffler) {
    this.shuffler = shuffler;
  }

  private record Square(int x0, int x1, int y0, int y1, int size) {
    private Square(final int x, final int y, final int size) {
      this(x, x + size, y, y + size, size);
    }

    private static Square of(final Cell<Crop> tile) {
      return new Square(tile.x(), tile.y(), tile.value().size);
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


  private enum CheckResult {
    OVERRIDE, REJECT, IGNORE
  }

  @Override
  public boolean test(final Phenotype<IntegerGene, Double> individual) {
    Log.trace(LOG_FQCN, "Testing individual: {0}", arr(pretty(individual)), null);

    final IntegerChromosome chromosome = individual.genotype().chromosome()
        .as(IntegerChromosome.class);
    final var grid = grid(Arrays.asList(new Square[9 * 9]), 9);
    final var cropGrid = grid(Arrays.stream(chromosome.toArray()).mapToObj(Crop::at).toList(), 9);
    for (final var cell : cropGrid) {
      final Square square = Square.of(cell);
      switch (checkCropTile(grid, square)) {
        case REJECT:
          Log.trace("Individual was rejected");
          return false;
        case OVERRIDE:
          fillMatrix(grid, square);
          break;
        case IGNORE:
          // do nothing
          break;
      }
    }
    Log.trace("Individual was approved");
    return true;
  }

  @Override
  public Phenotype<IntegerGene, Double> repair(
      final Phenotype<IntegerGene, Double> individual, final long generation) {
    Log.trace(LOG_FQCN, "Repairing individual: {0}; at generation {1}",
        arr(pretty(individual), generation), null);

    final IntegerChromosome chromosome = individual.genotype().chromosome()
        .as(IntegerChromosome.class);
    final var grid = grid(Arrays.asList(new Square[9 * 9]), 9);
    final List<IntArrayValue> changes = new ArrayList<>();
    // shuffle the coordinates to avoid bias towards the first ones
    final var cropGrid = grid(Arrays.stream(chromosome.toArray()).mapToObj(Crop::at).toList(), 9);
    for (final var cell : shuffler.shuffle(cropGrid)) {
      final Square square = Square.of(cell);
      final int valid = validCropSize(grid, square);
      Log.trace(LOG_FQCN, "Valid size for {0} is {1}", arr(square, valid), null);

      Crop replacement = cell.value();
      boolean replace = false;
      while (replacement.size > valid) {
        Log.trace(LOG_FQCN, "Crop {0} is invalid in place of {1}", arr(replacement, cell), null);
        replacement = Crop.at(random().nextInt(Crop.len()));
        replace = true;
      }
      if (replace) {
        changes.add(new IntArrayValue(grid.width() * cell.y() + cell.x(), replacement.ordinal()));
      }
    }

    final var old = individual.genotype().chromosome();
    final var fixed = Genotype.of(old.as(IntegerChromosome.class).map(arr -> {
      changes.forEach(change -> arr[change.pos()] = change.value());
      return arr;
    }));
    Log.trace(LOG_FQCN, "Repaired plot: {0}", arr(pretty(fixed)), null);
    return Phenotype.of(fixed, generation);
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
        final var chromosome = genotype.chromosome();
        sb.append("Pretty genotype");
        for (int j = 0; j < 9; j++) {
          sb.append("\n");
          for (int i = 0; i < 9; i++) {
            final int code = chromosome.get(j * 9 + i).allele();
            final Crop crop = Crop.at(code);
            sb.append(String.format("%4.4s ", crop));
          }
        }
        return sb.toString();
      }
    };
  }

  private CheckResult checkCropTile(final Grid<Square> grid, final Square square) {
    // check if the crop fits in the plot
    if (square.x1() > grid.width() || square.y1() > grid.height()) {
      return CheckResult.REJECT;
    }
    // check partial overlap in crop tiles
    for (final var c : coordinates(square.x0(), square.x1(), square.y0(), square.y1())) {
      final Square current = grid.at(c);
      if (current != null && !square.contains(current)) {
        if (current.contains(square)) {
          return CheckResult.IGNORE;
        } else {
          return CheckResult.REJECT;
        }
      }
    }
    return CheckResult.OVERRIDE;
  }

  /**
   * Mark the tiles occupied by the crop.
   *
   * @param grid the grid to fill
   * @param target the square to fill with
   */
  private void fillMatrix(final Grid<Square> grid, final Square target) {
    for (final var c : coordinates(target.x0(), target.x1(), target.y0(), target.y1())) {
      grid.set(c, target);
    }
  }

  private int validCropSize(final Grid<Square> grid, final Square square) {
    final int remainRight = grid.width() - square.x0();
    final int remainDown = grid.height() - square.y0();
    int valid = min(square.size(), min(remainRight, remainDown));
    CheckResult result;
    do {
      Log.trace(LOG_FQCN, "Attempting to fit {0}. Current [valid] is {1}",
          arr(square, valid), null);
      result = checkCropTile(grid, square.withSize(valid));
      valid--;
    } while (result == CheckResult.REJECT);

    valid++; // undo last decrement
    if (result == CheckResult.OVERRIDE) {
      fillMatrix(grid, square.withSize(valid));
    }
    return valid;
  }
}
