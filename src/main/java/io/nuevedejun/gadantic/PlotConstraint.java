package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.nuevedejun.gadantic.Iterables.Cell;
import io.nuevedejun.gadantic.Iterables.Grid;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.jenetics.util.RandomRegistry.random;
import static io.nuevedejun.gadantic.Iterables.coordinates;
import static io.nuevedejun.gadantic.Iterables.grid;
import static java.lang.Math.min;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@XSlf4j
public class PlotConstraint implements Constraint<IntegerGene, Double> {

  public static PlotConstraint create(final Iterables.Shuffler shuffler) {
    return new PlotConstraint(shuffler);
  }

  private record Square(int x0, int x1, int y0, int y1, int size) {
    private Square(final int x, final int y, final int size) {
      this(x, x + size, y, y + size, size);
    }

    private static Square of(final Cell<Crop> tile) {
      return new Square(tile.x(), tile.y(), tile.value().size());
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


  private final Iterables.Shuffler shuffler;

  @Override
  public boolean test(final Phenotype<IntegerGene, Double> individual) {
    log.entry(pretty(individual));

    final IntegerChromosome chromosome = individual.genotype().chromosome()
        .as(IntegerChromosome.class);
    final var grid = grid(Arrays.asList(new Square[9 * 9]), 9);
    final var cropGrid = grid(Arrays.stream(chromosome.toArray()).mapToObj(Crop::at).toList(), 9);
    for (final var cell : cropGrid) {
      final Square square = Square.of(cell);
      switch (checkCropTile(grid, square)) {
        case REJECT:
          return log.exit(false);
        case OVERRIDE:
          fillMatrix(grid, square);
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

    final IntegerChromosome chromosome = individual.genotype().chromosome()
        .as(IntegerChromosome.class);
    final var grid = grid(Arrays.asList(new Square[9 * 9]), 9);
    final List<IntArrayValue> changes = new ArrayList<>();
    // shuffle the coordinates to avoid bias towards the first ones
    final var cropGrid = grid(Arrays.stream(chromosome.toArray()).mapToObj(Crop::at).toList(), 9);
    for (final var cell : shuffler.shuffle(cropGrid)) {
      final Square square = Square.of(cell);
      final int valid = validCropSize(grid, square);
      log.trace("Valid size for {} is {}", square, valid);

      Crop replacement = cell.value();
      boolean replace = false;
      while (replacement.size() > valid) {
        log.trace("Crop {} is invalid in position ({}, {}).", replacement, cell.x(), cell.y());
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
      log.trace("Attempting to fit {}. Current [valid] is {}", square, valid);
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
