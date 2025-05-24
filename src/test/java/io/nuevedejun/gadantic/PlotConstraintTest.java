package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.nuevedejun.gadantic.PlotIterableFactory.Coordinate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySortedSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class PlotConstraintTest {

  final PrioritizedIterableFactory iterableFactory = new PrioritizedIterableFactory();

  final PlotConstraint constraint = new PlotConstraint(iterableFactory);

  static List<Arguments> repairTestCases() {
    final int[][][] twoTileOverlap0 = {
        // @formatter:off
        {{2,2,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{2,5,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairLowerRight = {
        // @formatter:off
        {{2,2,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{2,1,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairUpperLeft = {
        // @formatter:off
        {{2,1,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{2,5,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] twoTileOverlap1 = {
        // @formatter:off
        {{0,0,0},{3,3,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{4,4,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairLowerLeft = {
        // @formatter:off
        {{0,0,0},{3,3,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{4,2,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairUpperRight = {
        // @formatter:off
        {{0,0,0},{3,2,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{4,4,5},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] leftOverFlow = {
        // @formatter:off
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{4,5,2},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairLeftOverFlow = {
        // @formatter:off
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{4,4,2},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] bottomOverFlow = {
        // @formatter:off
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{3,5,5},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] repairBottomOverFlow = {
        // @formatter:off
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{3,4,5},{0,0,0},{0,0,0}},
        {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        // @formatter:on
    };
    final int[][][] validPlot = {
        // @formatter:off
        {{3,4,1},{2,1,5},{2,0,3},{0,0,0},{3,2,0},{4,4,3},{4,1,4},{4,1,0},{3,1,4}},
        {{4,0,5},{0,4,0},{2,1,2},{1,0,2},{4,2,2},{1,2,1},{0,0,3},{2,1,3},{3,1,4}},
        {{2,4,3},{3,2,1},{4,0,1},{3,0,2},{0,4,4},{1,5,4},{3,0,5},{3,4,4},{3,1,2}},
        {{4,1,4},{1,0,4},{4,3,5},{1,4,2},{4,5,2},{4,2,2},{3,2,0},{0,1,5},{4,2,3}},
        {{4,4,0},{1,2,4},{3,2,4},{4,2,1},{0,0,4},{0,1,2},{2,1,4},{2,3,5},{0,0,5}},
        {{0,0,3},{4,0,3},{2,2,0},{1,1,1},{1,2,0},{4,2,3},{0,1,4},{0,0,2},{1,0,5}},
        {{3,2,4},{3,2,3},{2,1,1},{3,2,1},{2,2,0},{0,4,1},{2,0,4},{4,0,2},{0,2,2}},
        {{2,4,5},{4,0,4},{4,4,1},{2,0,4},{1,5,3},{1,1,5},{0,1,2},{0,4,3},{0,3,0}},
        {{0,3,4},{3,2,1},{2,1,1},{1,5,3},{1,4,3},{3,2,2},{2,1,0},{0,0,1},{2,0,3}}
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
  @MethodSource("repairTestCases")
  void testConstraint(final int[][][] original, final boolean valid, final int[][][] expected,
      final SequencedSet<Coordinate> priority) {
    iterableFactory.prioritize(priority);
    final var phenotype = encode(original);

    assertEquals(valid, constraint.test(phenotype));
    final var result = constraint.repair(phenotype, 1);

    final int[][][] actual = decode(result);
    assertArrayEquals(expected, actual);
  }

  /**
   * Creates a {@link io.jenetics.Phenotype} from the given genes.
   * 
   * @implNote <strong>Encoding of crops</strong>
   *           <ul>
   *           <li>Perk - there are four perks and they do not have the same
   *           number of crops. Harvest boost has double the amount than the rest.
   *           The encoding takes that into account.
   *           <li>Area and kind - area is related to the size of the crop. The
   *           amount of crops by size depends on the perk, and so does the
   *           encoding.
   *           <li>Kind is just a classifier to select among crops of the same
   *           perk and size.
   *           </ul>
   *           Here is the list of crops with their encodings:
   * 
   *           <pre>
   *Crop         Perk     Area     Kind
   *Tomatoes      0       [0, 6)   [0, 2)
   *Potatoes      0       [0, 6)   [2, 4)
   *Cabbage       0       [0, 6)   [4, 6)
   *Carrots       1       [0, 6)   [0, 2)
   *Onions        1       [0, 6)   [2, 4)
   *Bok choy      1       [0, 6)   [4, 6)
   *Cotton        2       [0, 2)   [0, 6)
   *Peppers       2       [2, 6)   [0, 3)
   *Pumpkins      2       [2, 6)   [3, 6)
   *Rice         [3, 5)   [0, 3)   [0, 2)
   *Wheat        [3, 5)   [0, 3)   [2, 4)
   *Corn         [3, 5)   [0, 3)   [4, 6)
   *Blueberries  [3, 5)   [3, 5)   [0, 3)
   *Beans        [3, 5)   [3, 5)   [3, 6)
   *Apples       [3, 5)    5       [0, 6)
   *           </pre>
   */
  private Phenotype<IntegerGene, Double> encode(final int[][][] genes) {
    final var perkChromosome = IntegerChromosome.of(Stream.of(genes).flatMap(Stream::of)
        .map(arr -> IntegerGene.of(arr[0], 0, 5)).toList());
    final var areaChromosome = IntegerChromosome.of(Stream.of(genes).flatMap(Stream::of)
        .map(arr -> IntegerGene.of(arr[1], 0, 6)).toList());
    final var kindChromosome = IntegerChromosome.of(Stream.of(genes).flatMap(Stream::of)
        .map(arr -> IntegerGene.of(arr[2], 0, 6)).toList());
    return Phenotype.of(Genotype.of(perkChromosome, areaChromosome, kindChromosome), 1);
  }

  private int[][][] decode(final Phenotype<IntegerGene, Double> phenotype) {
    final var perkChromosome = phenotype.genotype().get(0);
    final var areaChromosome = phenotype.genotype().get(1);
    final var kindChromosome = phenotype.genotype().get(2);
    final var result = new int[9][9][9];
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        final var idx = 9 * j + i;
        result[j][i] = new int[] {
            perkChromosome.get(idx).allele(),
            areaChromosome.get(idx).allele(),
            kindChromosome.get(idx).allele() };
      }
    }
    return result;
  }
}
