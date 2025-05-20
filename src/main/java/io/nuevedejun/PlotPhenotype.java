package io.nuevedejun;

import static io.nuevedejun.PlotPhenotype.Border.BLANK;
import static io.nuevedejun.PlotPhenotype.Border.CROSS;
import static io.nuevedejun.PlotPhenotype.Border.DASH;
import static io.nuevedejun.PlotPhenotype.Border.LEFT_T;
import static io.nuevedejun.PlotPhenotype.Border.LOWER_LEFT;
import static io.nuevedejun.PlotPhenotype.Border.LOWER_RIGHT;
import static io.nuevedejun.PlotPhenotype.Border.LOWER_T;
import static io.nuevedejun.PlotPhenotype.Border.PIPE;
import static io.nuevedejun.PlotPhenotype.Border.RIGHT_T;
import static io.nuevedejun.PlotPhenotype.Border.UPPER_LEFT;
import static io.nuevedejun.PlotPhenotype.Border.UPPER_RIGHT;
import static io.nuevedejun.PlotPhenotype.Border.UPPER_T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
public class PlotPhenotype {

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  public enum Crop {
    // single-tile crops
    TOMATOES(1), POTATOES(1), CABBAGE(1), RICE(1), WHEAT(1),
    CORN(1), CARROTS(1), ONIONS(1), BOK_CHOY(1), COTTON(1),
    // 4-tile crops
    BLUEBERRIES(2), BEANS(2), PEPPERS(2), PUMPKINS(2),
    // 9-tile crops
    APPLES(3);

    private final int size;
  }

  @FunctionalInterface
  public interface CropDecoder {
    static CropDecoder ofPerk(final int perk) {
      return switch (perk) {
        case 0 -> PlotPhenotype::decodeWater;
        case 1 -> PlotPhenotype::decodeWeed;
        case 2 -> PlotPhenotype::decodeQuality;
        default /* 3, 4 */ -> PlotPhenotype::decodeHarvest;
      };
    }

    Crop get(int area, int kind);
  }

  public record TiledCrop(int x, int y, Crop crop, int perk, int area, int kind, CropDecoder decoder) {

    public TiledCrop(final Coordinate coordinate, final Crop crop,
        final int perk, final int area, final int kind, final CropDecoder decoder) {
      this(coordinate.x(), coordinate.y(), crop, perk, area, kind, decoder);
    }

    public static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype) {
      return tiles(genotype, false);
    }

    public static Iterable<TiledCrop> tiles(final Genotype<IntegerGene> genotype, final boolean shuffled) {
      final var elements = new ArrayList<>(COORDINATES);
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

          final int perk = perkChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
          final int area = areaChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();
          final int kind = kindChromosome.get(coordinate.x() * 9 + coordinate.y()).allele();

          final CropDecoder cropDecoder = CropDecoder.ofPerk(perk);
          final Crop crop = cropDecoder.get(area, kind);

          return new TiledCrop(coordinate, crop, perk, area, kind, cropDecoder);
        }
      };
    }
  }

  record Coordinate(int x, int y) {
  }

  private static final List<Coordinate> COORDINATES = IntStream.range(0, 9).boxed()
      .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
      .toList();

  static Crop decodeWater(final int area, final int kind) {
    return switch (kind) {
      case 0, 1 -> Crop.TOMATOES;
      case 2, 3 -> Crop.POTATOES;
      default /* 4, 5 */ -> Crop.CABBAGE;
    };
  }

  static Crop decodeWeed(final int area, final int kind) {
    return switch (kind) {
      case 0, 1 -> Crop.CARROTS;
      case 2, 3 -> Crop.ONIONS;
      default /* 4, 5 */ -> Crop.BOK_CHOY;
    };
  }

  static Crop decodeQuality(final int area, final int kind) {
    if (area < 2) {
      return Crop.COTTON;
    } else if (kind < 3) {
      return Crop.PEPPERS;
    } else {
      return Crop.PUMPKINS;
    }
  }

  static Crop decodeHarvest(final int area, final int kind) {
    return switch (area) {
      case 0, 1, 2 -> switch (kind) {
        case 0, 1 -> Crop.RICE;
        case 2, 3 -> Crop.WHEAT;
        default /* 4, 5 */ -> Crop.CORN;
      };
      case 3, 4 -> kind < 3 ? Crop.BLUEBERRIES : Crop.BEANS;
      default /* 5 */ -> Crop.APPLES;
    };
  }

  Genotype<IntegerGene> genotype() {
    return Genotype.of(
        /* perk */ IntegerChromosome.of(0, 5, 9 * 9),
        /* area */ IntegerChromosome.of(0, 6, 9 * 9),
        /* kind */ IntegerChromosome.of(0, 6, 9 * 9));
  }

  double fitness(final Genotype<IntegerGene> genotype) {
    final Plot plot = Plot.decode(genotype);
    return 0;
  }

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  enum Border {
    UPPER_LEFT('\u250C'), UPPER_RIGHT('\u2510'), LOWER_RIGHT('\u2518'), LOWER_LEFT('\u2514'),
    LEFT_T('\u251C'), UPPER_T('\u252C'), RIGHT_T('\u2524'), LOWER_T('\u2534'),
    CROSS('\u253C'), DASH('\u2500'), PIPE('\u2502'),
    BLANK(' ');

    private final char character;
    private static final Map<Character, Border> MAP = Map.copyOf(Stream.of(Border.values())
        .collect(Collectors.toMap(Border::character, Function.identity())));

    static Border from(char c) {
      if (log.isDebugEnabled()) {
        log.debug("Border.from('{}' [])", c, Character.codePointAt(new char[] { c }, 0));
      }
      return MAP.get(c);
    }
  }

  @RequiredArgsConstructor
  static class Plot {
    private final Set<AnnotatedCrop> crops;
    private final AnnotatedCrop[][] layout;

    static Plot decode(final Genotype<IntegerGene> genotype) {
      final AnnotatedCrop[][] array = new AnnotatedCrop[9][9];
      final HashSet<AnnotatedCrop> set = HashSet.newHashSet(9 * 9);
      for (final var tile : TiledCrop.tiles(genotype, true)) {
        if (array[tile.x()][tile.y()] == null) {
          final var annotated = new AnnotatedCrop(tile.crop(), tile.x(), tile.y());
          for (int i = 0; i < tile.crop().size(); i++) {
            for (int j = 0; j < tile.crop().size(); j++) {
              array[tile.x() + i][tile.y() + j] = annotated;
            }
          }
        }
      }
      for (int i = 0; i < array.length; i++) {
        for (int j = 0; j < array[i].length; j++) {
          set.add(array[i][j]);
        }
      }
      return new Plot(Set.of(set.toArray(new AnnotatedCrop[0])), array);
    }

    private static final int CELL_WIDTH = 11;
    private static final int LINE_LEN = 9 * CELL_WIDTH + 2;
    private static final int LINES = 9 * 2 + 1;

    public String str() {
      final StringBuilder sb = new StringBuilder();

      final int root = sb.length();
      for (int i = 0; i < LINES; i++) {
        sb.repeat(BLANK.character(), LINE_LEN - 1).append('\n');
      }
      for (final var annotated : crops) {
        final int start = root + annotated.x() * CELL_WIDTH + 2 * annotated.y() * LINE_LEN;
        final Crop crop = annotated.crop();
        drawCell(sb, start, crop.size());
        final String name = crop.name();
        final String text = name.substring(0,
            Math.min(name.length(), crop.size() * CELL_WIDTH - 1));
        replace(sb, start + LINE_LEN + 1, text);
      }
      // draw left border
      drawVertical(sb, root + 2 * LINE_LEN - 2, LINES - 2);
      // draw lower border
      drawHorizontal(sb, root + 1 + (LINES - 1) * LINE_LEN, LINE_LEN - 3);
      sb.append("Crop count: ").append(crops.size());
      return sb.toString();
    }

    private void drawCell(final StringBuilder sb, final int pos, final int size) {
      final int cellWidth = size * CELL_WIDTH;
      final int offset = 2 * size * LINE_LEN;
      drawUpperLeft(sb, pos);
      drawUpperRight(sb, pos + cellWidth);
      drawLowerRight(sb, pos + cellWidth + offset);
      drawLowerLeft(sb, pos + offset);
      drawHorizontal(sb, pos + 1, cellWidth - 1); // upper border
      drawVertical(sb, pos + LINE_LEN, 2 * size - 1); // left border
    }

    private void drawUpperLeft(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case UPPER_LEFT, BLANK -> UPPER_LEFT;
        case UPPER_RIGHT, UPPER_T, DASH -> UPPER_T;
        case LOWER_RIGHT, CROSS, RIGHT_T, LOWER_T -> CROSS;
        case LOWER_LEFT, LEFT_T, PIPE -> LEFT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private void drawUpperRight(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case UPPER_RIGHT, BLANK -> UPPER_RIGHT;
        case UPPER_LEFT, UPPER_T, DASH -> UPPER_T;
        case LOWER_LEFT, CROSS, LEFT_T, LOWER_T -> CROSS;
        case LOWER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private void drawLowerRight(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case LOWER_RIGHT, BLANK -> LOWER_RIGHT;
        case LOWER_LEFT, LOWER_T, DASH -> LOWER_T;
        case UPPER_LEFT, UPPER_T, CROSS, LEFT_T -> CROSS;
        case UPPER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private void drawLowerLeft(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case LOWER_LEFT, BLANK -> LOWER_LEFT;
        case LOWER_RIGHT, LOWER_T, DASH -> LOWER_T;
        case UPPER_RIGHT, UPPER_T, RIGHT_T, CROSS -> CROSS;
        case UPPER_LEFT, LEFT_T, PIPE -> LEFT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private void drawHorizontal(final StringBuilder sb, final int start, final int len) {
      final StringBuilder line = new StringBuilder();
      for (int i = 0; i < len; i++) {
        final Border replacement = switch (Border.from(sb.charAt(start + i))) {
          case DASH, BLANK -> DASH;
          case LOWER_LEFT, LOWER_RIGHT, LOWER_T -> LOWER_T;
          case UPPER_LEFT, UPPER_RIGHT, UPPER_T -> UPPER_T;
          case LEFT_T, RIGHT_T, PIPE, CROSS -> CROSS;
        };
        line.append(replacement.character());
      }
      replace(sb, start, line.toString());
    }

    private void drawVertical(final StringBuilder sb, final int start, final int len) {
      for (int i = 0; i < len; i++) {
        final int pos = start + i * LINE_LEN;
        final Border replacement = switch (Border.from(sb.charAt(pos))) {
          case PIPE, BLANK -> PIPE;
          case LOWER_LEFT, UPPER_LEFT, LEFT_T -> LEFT_T;
          case LOWER_RIGHT, UPPER_RIGHT, RIGHT_T -> RIGHT_T;
          case LOWER_T, UPPER_T, CROSS, DASH -> CROSS;
        };
        sb.setCharAt(pos, replacement.character());
      }
    }

    StringBuilder replace(final StringBuilder sb, final int start, final String str) {
      return sb.replace(start, start + str.length(), str);
    }
  }

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  static class AnnotatedCrop {
    private final Crop crop;
    private final int x;
    private final int y;
  }
}
