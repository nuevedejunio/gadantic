package io.nuevedejun.gadantic;

import io.jenetics.util.RandomRegistry;
import io.nuevedejun.gadantic.PrioritizedShuffler.Coordinate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.SequencedSet;
import java.util.Set;

import static io.nuevedejun.gadantic.PlotCodec.decode;
import static io.nuevedejun.gadantic.PlotCodec.encode;
import static java.util.Collections.emptySortedSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class PlotConstraintTest {

  final PrioritizedShuffler iterableFactory = new PrioritizedShuffler();

  final PlotConstraint constraint = PlotConstraint.create(iterableFactory);

  @BeforeAll
  static void setUp() {
    // fix the seed
    RandomRegistry.random(new Random(0));
  }

  static List<Arguments> constraintTestCases() {
    final int[] twoTileOverlap0 = {
        // @formatter:off
          12,   0,   0,   0,   0,   0,   0,   0,   0,
           0,  13,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairLowerRight = {
        // @formatter:off
          12,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairUpperLeft = {
        // @formatter:off
           4,   0,   0,   0,   0,   0,   0,   0,   0,
           0,  13,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] twoTileOverlap1 = {
        // @formatter:off
           0,  10,   0,   0,   0,   0,   0,   0,   0,
          11,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairLowerLeft = {
        // @formatter:off
           0,  10,   0,   0,   0,   0,   0,   0,   0,
           2,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairUpperRight = {
        // @formatter:off
           0,   5,   0,   0,   0,   0,   0,   0,   0,
          11,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] leftOverFlow = {
        // @formatter:off
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,  14,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairLeftOverFlow = {
        // @formatter:off
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   8,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] bottomOverFlow = {
        // @formatter:off
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,  14,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] repairBottomOverFlow = {
        // @formatter:off
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0,
           0,   0,   0,   0,   0,   0,  11,   0,   0,
           0,   0,   0,   0,   0,   0,   0,   0,   0
        // @formatter:on
    };
    final int[] validPlot = {
        // @formatter:off
          10,   9,   9,   0,   3,  11,   5,   3,   5,
           5,   0,   9,   7,   4,   6,   1,   9,   5,
          13,   3,   3,   4,   2,   8,   5,  11,   4,
           5,   8,  11,   7,  14,   4,   3,   2,   4,
          10,   8,   5,   3,   2,   1,   9,  13,   2,
           1,   4,  12,   6,   6,   4,   2,   1,   8,
           5,   4,   9,   3,  12,   0,   9,   4,   1,
          13,   5,  10,   9,   7,   8,   1,   1,   0,
           2,   3,   9,   7,   7,   4,   9,   0,   9
        // @formatter:on
    };

    return List.of(
        argumentSet("Repair 2-tile overlap; priority upper left", twoTileOverlap0, false, repairLowerRight,
            emptySortedSet()),
        argumentSet("Repair 2-tile overlap; priority lower right", twoTileOverlap0, false, repairUpperLeft,
            new LinkedHashSet<>(Set.of(new Coordinate(1, 1)))),
        argumentSet("Repair 2-tile overlap; priority upper right", twoTileOverlap1, false, repairLowerLeft,
            new LinkedHashSet<>(Set.of(new Coordinate(1, 0)))),
        argumentSet("Repair 2-tile overlap; priority lower left", twoTileOverlap1, false, repairUpperRight,
            new LinkedHashSet<>(Set.of(new Coordinate(0, 1)))),
        argumentSet("Repair left border overflow", leftOverFlow, false, repairLeftOverFlow,
            emptySortedSet()),
        argumentSet("Repair bottom border overflow", bottomOverFlow, false, repairBottomOverFlow,
            emptySortedSet()),
        argumentSet("Preserve valid plot", validPlot, true, validPlot,
            emptySortedSet()));
  }

  @ParameterizedTest
  @MethodSource("constraintTestCases")
  void testConstraint(final int[] original, final boolean valid, final int[] expected,
      final SequencedSet<Coordinate> priority) {
    iterableFactory.prioritize(priority);
    final var phenotype = encode(original);

    assertEquals(valid, constraint.test(phenotype));
    final var result = constraint.repair(phenotype, 1);

    final int[] actual = decode(result);
    assertArrayEquals(expected, actual);
  }
}
