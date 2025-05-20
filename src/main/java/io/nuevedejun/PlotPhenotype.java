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
import static io.nuevedejun.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.PlotPhenotype.Perk.WEED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
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
    TOMATOES(1, WATER), POTATOES(1, WATER), CABBAGE(1, WATER), RICE(1, HARVEST), WHEAT(1, HARVEST),
    CORN(1, HARVEST), CARROTS(1, WEED), ONIONS(1, WEED), BOK_CHOY(1, WEED), COTTON(1, QUALITY),
    // 4-tile crops
    BLUEBERRIES(2, HARVEST), BEANS(2, HARVEST), PEPPERS(2, QUALITY), PUMPKINS(2, QUALITY),
    // 9-tile crops
    APPLES(3, HARVEST);

    private final int size;
    private final Perk perk;
  }

  public enum Perk {
    WATER, WEED, QUALITY, HARVEST
  }

  /**
   * Decodes the crop type based on the type of perk, area, and kind.
   * <p>
   * These values do not map directly with crop characteristics, but instead where
   * chosen to guarantee a normal distribution of the probability of each crop
   * type. When new crops are added, the ranges of those parameters must be
   * adjusted.
   */
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

    private static final List<Coordinate> COORDINATES = IntStream.range(0, 9).boxed()
        .flatMap(i -> IntStream.range(0, 9).mapToObj(j -> new Coordinate(i, j)))
        .toList();

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

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  enum Border {
    UPPER_LEFT('\u250C'), UPPER_RIGHT('\u2510'), LOWER_RIGHT('\u2518'), LOWER_LEFT('\u2514'),
    LEFT_T('\u251C'), UPPER_T('\u252C'), RIGHT_T('\u2524'), LOWER_T('\u2534'),
    CROSS('\u253C'), DASH('\u2500'), PIPE('\u2502'),
    BLANK(' ');

    private static final Map<Character, Border> MAP = Map.copyOf(Stream.of(Border.values())
        .collect(Collectors.toMap(Border::character, Function.identity())));

    static Border from(final char c) {
      log.entry((int) c);
      return log.exit(MAP.get(c));
    }

    private final char character;
  }

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  static class Plot {
    private static final int CELL_WIDTH = 11;
    private static final int LINE_LEN = 9 * CELL_WIDTH + 2;
    private static final int LINES = 9 * 2 + 1;

    private static final String HAS_WATER = "\u2660";
    private static final String HAS_WEED = "\u2619";
    private static final String HAS_QUALITY = "\u2605";
    private static final String HAS_HARVEST = "\u2698";

    static Plot decode(final Genotype<IntegerGene> genotype) {
      final RichCrop[][] array = new RichCrop[9][9];
      for (final var tile : TiledCrop.tiles(genotype, true)) {
        if (array[tile.x()][tile.y()] == null) {
          fillCropTile(array, tile);
        }
      }
      final HashSet<RichCrop> set = HashSet.newHashSet(9 * 9);
      for (int i = 0; i < array.length; i++) {
        for (int j = 0; j < array[i].length; j++) {
          applyBuffs(array, i, j);
          set.add(array[i][j]);
        }
      }
      int water = 0;
      int weed = 0;
      int quality = 0;
      int harvest = 0;
      for (final var crop : set) {
        if (crop.hasWater()) {
          water += crop.crop().size() * crop.crop().size();
        }
        if (crop.hasWeed()) {
          weed += crop.crop().size() * crop.crop().size();
        }
        if (crop.hasQuality()) {
          quality += crop.crop().size() * crop.crop().size();
        }
        if (crop.hasHarvest()) {
          harvest += crop.crop().size() * crop.crop().size();
        }
      }
      final String layoutUrl = createLayoutUrl(array);
      return new Plot(Set.of(set.toArray(new RichCrop[0])), water, weed, quality, harvest, layoutUrl);
    }

    private static String createLayoutUrl(final RichCrop[][] array) {
      final StringBuilder sb = new StringBuilder()
          .append("https://palia-garden-planner.vercel.app/?layout=")
          .append("v0.4_D-111-111-111_CR");

      for (int i = 0; i < 9; i += 3) {
        for (int j = 0; j < 9; j += 3) {
          sb.append('-');
          for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
              sb.append(mapLayout(array[l][k].crop()));
            }
          }
        }
      }

      return sb.toString();
    }

    private static String mapLayout(final Crop crop) {
      return switch (crop) {
        case TOMATOES -> "T";
        case POTATOES -> "P";
        case CABBAGE -> "Cb";
        case RICE -> "R";
        case WHEAT -> "W";
        case CORN -> "Cr";
        case CARROTS -> "C";
        case ONIONS -> "O";
        case BOK_CHOY -> "Bk";
        case COTTON -> "Co";
        case BLUEBERRIES -> "B";
        case BEANS -> "Bt";
        case PEPPERS -> "S";
        case PUMPKINS -> "Pm";
        case APPLES -> "A";
      };
    }

    private static void fillCropTile(final RichCrop[][] array, final TiledCrop tile) {
      final var rich = new RichCrop(tile.crop(), tile.x(), tile.y());
      for (int i = 0; i < tile.crop().size(); i++) {
        for (int j = 0; j < tile.crop().size(); j++) {
          array[tile.x() + i][tile.y() + j] = rich;
        }
      }
    }

    private static void applyBuffs(final RichCrop[][] crops, final int x, final int y) {
      final var crop = crops[x][y];
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i != j && i != -j && x + i >= 0 && x + i < 9 && y + j >= 0 && y + j < 9) {
            final var target = crops[x + i][y + j];
            crop.buff(target);
          }
        }
      }
    }

    private static StringBuilder replace(final StringBuilder sb, final int start, final String str) {
      return sb.replace(start, start + str.length(), str);
    }

    private static void drawCell(final StringBuilder sb, final int pos, final int size) {
      final int cellWidth = size * CELL_WIDTH;
      final int offset = 2 * size * LINE_LEN;
      drawUpperLeft(sb, pos);
      drawUpperRight(sb, pos + cellWidth);
      drawLowerRight(sb, pos + cellWidth + offset);
      drawLowerLeft(sb, pos + offset);
      drawHorizontal(sb, pos + 1, cellWidth - 1); // upper border
      drawVertical(sb, pos + LINE_LEN, 2 * size - 1); // left border
    }

    private static void drawUpperLeft(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case UPPER_LEFT, BLANK -> UPPER_LEFT;
        case UPPER_RIGHT, UPPER_T, DASH -> UPPER_T;
        case LOWER_RIGHT, CROSS, RIGHT_T, LOWER_T -> CROSS;
        case LOWER_LEFT, LEFT_T, PIPE -> LEFT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private static void drawUpperRight(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case UPPER_RIGHT, BLANK -> UPPER_RIGHT;
        case UPPER_LEFT, UPPER_T, DASH -> UPPER_T;
        case LOWER_LEFT, CROSS, LEFT_T, LOWER_T -> CROSS;
        case LOWER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private static void drawLowerRight(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case LOWER_RIGHT, BLANK -> LOWER_RIGHT;
        case LOWER_LEFT, LOWER_T, DASH -> LOWER_T;
        case UPPER_LEFT, UPPER_T, CROSS, LEFT_T -> CROSS;
        case UPPER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private static void drawLowerLeft(final StringBuilder sb, final int pos) {
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case LOWER_LEFT, BLANK -> LOWER_LEFT;
        case LOWER_RIGHT, LOWER_T, DASH -> LOWER_T;
        case UPPER_RIGHT, UPPER_T, RIGHT_T, CROSS -> CROSS;
        case UPPER_LEFT, LEFT_T, PIPE -> LEFT_T;
      };
      sb.setCharAt(pos, replacement.character());
    }

    private static void drawHorizontal(final StringBuilder sb, final int start, final int len) {
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

    private static void drawVertical(final StringBuilder sb, final int start, final int len) {
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

    private final Set<RichCrop> crops;
    private final int water;
    private final int weed;
    private final int quality;
    private final int harvest;
    private final String layoutUrl;

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
        final String badges = (annotated.hasWater() ? HAS_WATER : "")
            + (annotated.hasWeed() ? HAS_WEED : "")
            + (annotated.hasQuality() ? HAS_QUALITY : "")
            + (annotated.hasHarvest() ? HAS_HARVEST : "");
        final int desired = crop.size() * CELL_WIDTH - 1 - badges.length();
        final String format = "%-" + desired + "." + desired + "s%s";
        final String text = String.format(format, name, badges);
        replace(sb, start + LINE_LEN + 1, text);
      }
      // draw left border
      drawVertical(sb, root + 2 * LINE_LEN - 2, LINES - 2);
      // draw lower border
      drawHorizontal(sb, root + 1 + (LINES - 1) * LINE_LEN, LINE_LEN - 3);
      sb.append(crops.size()).append(" crops\n");
      sb.append(water).append("(").append(100 * water / 81).append("%) water   ");
      sb.append(weed).append("(").append(100 * weed / 81).append("%) weed   ");
      sb.append(quality).append("(").append(100 * quality / 81).append("%) quality   ");
      sb.append(harvest).append("(").append(100 * harvest / 81).append("%) harvest\n");
      sb.append("Garden Planner: ").append(layoutUrl).append('\n');
      return sb.toString();
    }
  }

  @RequiredArgsConstructor
  @Getter
  @Accessors(fluent = true)
  static class RichCrop {
    private final Crop crop;
    private final int x;
    private final int y;

    private final Map<Perk, Integer> perks = new EnumMap<>(Map.of(
        WATER, 0,
        WEED, 0,
        QUALITY, 0,
        HARVEST, 0));

    public void buff(final RichCrop other) {
      if (this.crop() != other.crop()) {
        other.perks().put(crop.perk(), other.perks().get(crop.perk()) + 1);
      }
    }

    public boolean hasWater() {
      return perks.get(WATER) >= crop.size();
    }

    public boolean hasWeed() {
      return perks.get(WEED) >= crop.size();
    }

    public boolean hasQuality() {
      return perks.get(QUALITY) >= crop.size();
    }

    public boolean hasHarvest() {
      return perks.get(HARVEST) >= crop.size();
    }
  }

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
}
