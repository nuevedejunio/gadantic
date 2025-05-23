package io.nuevedejun.gadantic;

import static io.nuevedejun.gadantic.PlotPhenotype.Border.BLANK;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.CROSS;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.DASH;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.LEFT_T;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.LOWER_LEFT;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.LOWER_RIGHT;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.LOWER_T;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.PIPE;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.RIGHT_T;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.UPPER_LEFT;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.UPPER_RIGHT;
import static io.nuevedejun.gadantic.PlotPhenotype.Border.UPPER_T;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.PlotPhenotype.Border;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.Perk;
import io.nuevedejun.gadantic.PlotPhenotype.TiledCrop;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;

@RequiredArgsConstructor
@XSlf4j
class Plot {
  @RequiredArgsConstructor
  @ToString
  static class RichCrop {
    final Crop crop;
    final int x;
    final int y;

    @ToString.Exclude
    final Map<Perk, Integer> perks = new EnumMap<>(Map.of(
        WATER, 0,
        WEED, 0,
        QUALITY, 0,
        HARVEST, 0));

    void buff(final RichCrop other) {
      if (this.crop != other.crop) {
        other.perks.put(crop.perk, other.perks.get(crop.perk) + 1);
      }
    }

    boolean has(final Perk perk) {
      return perks.get(perk) >= crop.size;
    }
  }

  static final int CELL_WIDTH = 11;
  static final int LINE_LEN = 9 * CELL_WIDTH + 2;
  static final int LINES = 9 * 2 + 1;

  static final String HAS_WATER = "\u2660";
  static final String HAS_WEED = "\u2619";
  static final String HAS_QUALITY = "\u2605";
  static final String HAS_HARVEST = "\u2698";

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
    log.trace("Set of decoded crops is: {}", set);

    int water = 0;
    int weed = 0;
    int quality = 0;
    int harvest = 0;
    for (final var crop : set) {
      if (crop.has(WATER)) {
        water += crop.crop.size * crop.crop.size;
      }
      if (crop.has(WEED)) {
        weed += crop.crop.size * crop.crop.size;
      }
      if (crop.has(QUALITY)) {
        quality += crop.crop.size * crop.crop.size;
      }
      if (crop.has(HARVEST)) {
        harvest += crop.crop.size * crop.crop.size;
      }
    }
    final int distinct = set.stream().map(r -> r.crop).collect(Collectors.toSet()).size();
    final String layoutUrl = createLayoutUrl(array);
    return new Plot(Set.of(set.toArray(new RichCrop[0])), water, weed, quality, harvest, distinct, layoutUrl);
  }

  static String createLayoutUrl(final RichCrop[][] array) {
    final StringBuilder sb = new StringBuilder()
        .append("https://palia-garden-planner.vercel.app/?layout=")
        .append("v0.4_D-111-111-111_CR");

    for (int i = 0; i < 9; i += 3) {
      for (int j = 0; j < 9; j += 3) {
        sb.append('-');
        for (int k = i; k < i + 3; k++) {
          for (int l = j; l < j + 3; l++) {
            sb.append(mapLayout(array[l][k].crop));
          }
        }
      }
    }

    return sb.toString();
  }

  static String mapLayout(final Crop crop) {
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

  static void fillCropTile(final RichCrop[][] array, final TiledCrop tile) {
    final var rich = new RichCrop(tile.crop(), tile.x(), tile.y());
    for (int i = 0; i < tile.crop().size; i++) {
      for (int j = 0; j < tile.crop().size; j++) {
        array[tile.x() + i][tile.y() + j] = rich;
      }
    }
  }

  static void applyBuffs(final RichCrop[][] crops, final int x, final int y) {
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

  final Set<RichCrop> crops;
  final int water;
  final int weed;
  final int quality;
  final int harvest;
  final int distinct;
  final String layoutUrl;

  StringBuilder replace(final StringBuilder sb, final int start, final String str) {
    return sb.replace(start, start + str.length(), str);
  }

  void drawCell(final StringBuilder sb, final int pos, final int size) {
    final int cellWidth = size * CELL_WIDTH;
    final int offset = 2 * size * LINE_LEN;
    drawUpperLeft(sb, pos);
    drawUpperRight(sb, pos + cellWidth);
    drawLowerRight(sb, pos + cellWidth + offset);
    drawLowerLeft(sb, pos + offset);
    drawHorizontal(sb, pos + 1, cellWidth - 1); // upper border
    drawVertical(sb, pos + LINE_LEN, 2 * size - 1); // left border
  }

  void drawUpperLeft(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case UPPER_LEFT, BLANK -> UPPER_LEFT;
      case UPPER_RIGHT, UPPER_T, DASH -> UPPER_T;
      case LOWER_RIGHT, CROSS, RIGHT_T, LOWER_T -> CROSS;
      case LOWER_LEFT, LEFT_T, PIPE -> LEFT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  void drawUpperRight(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case UPPER_RIGHT, BLANK -> UPPER_RIGHT;
      case UPPER_LEFT, UPPER_T, DASH -> UPPER_T;
      case LOWER_LEFT, CROSS, LEFT_T, LOWER_T -> CROSS;
      case LOWER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  void drawLowerRight(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case LOWER_RIGHT, BLANK -> LOWER_RIGHT;
      case LOWER_LEFT, LOWER_T, DASH -> LOWER_T;
      case UPPER_LEFT, UPPER_T, CROSS, LEFT_T -> CROSS;
      case UPPER_RIGHT, RIGHT_T, PIPE -> RIGHT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  void drawLowerLeft(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case LOWER_LEFT, BLANK -> LOWER_LEFT;
      case LOWER_RIGHT, LOWER_T, DASH -> LOWER_T;
      case UPPER_RIGHT, UPPER_T, RIGHT_T, CROSS -> CROSS;
      case UPPER_LEFT, LEFT_T, PIPE -> LEFT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  void drawHorizontal(final StringBuilder sb, final int start, final int len) {
    final StringBuilder line = new StringBuilder();
    for (int i = 0; i < len; i++) {
      final Border replacement = switch (Border.from(sb.charAt(start + i))) {
        case DASH, BLANK -> DASH;
        case LOWER_LEFT, LOWER_RIGHT, LOWER_T -> LOWER_T;
        case UPPER_LEFT, UPPER_RIGHT, UPPER_T -> UPPER_T;
        case LEFT_T, RIGHT_T, PIPE, CROSS -> CROSS;
      };
      line.append(replacement.character);
    }
    replace(sb, start, line.toString());
  }

  void drawVertical(final StringBuilder sb, final int start, final int len) {
    for (int i = 0; i < len; i++) {
      final int pos = start + i * LINE_LEN;
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case PIPE, BLANK -> PIPE;
        case LOWER_LEFT, UPPER_LEFT, LEFT_T -> LEFT_T;
        case LOWER_RIGHT, UPPER_RIGHT, RIGHT_T -> RIGHT_T;
        case LOWER_T, UPPER_T, CROSS, DASH -> CROSS;
      };
      sb.setCharAt(pos, replacement.character);
    }
  }

  String str() {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < LINES; i++) {
      // reserve space for the table
      sb.repeat(BLANK.character, LINE_LEN - 1).append('\n');
    }
    for (final var annotated : crops) {
      log.trace("Drawing cell of {}", annotated);

      final int start = annotated.x * CELL_WIDTH + 2 * annotated.y * LINE_LEN;
      final Crop crop = annotated.crop;
      drawCell(sb, start, crop.size);

      final String name = crop.name();
      final String badges = (annotated.has(WATER) ? HAS_WATER : "")
          + (annotated.has(WEED) ? HAS_WEED : "")
          + (annotated.has(QUALITY) ? HAS_QUALITY : "")
          + (annotated.has(HARVEST) ? HAS_HARVEST : "");
      final int desired = crop.size * CELL_WIDTH - 1 - badges.length();
      final String format = "%-" + desired + "." + desired + "s%s";
      final String text = String.format(format, name, badges);
      replace(sb, start + LINE_LEN + 1, text);
    }
    // draw left border
    drawVertical(sb, 2 * LINE_LEN - 2, LINES - 2);
    // draw lower border
    drawHorizontal(sb, 1 + (LINES - 1) * LINE_LEN, LINE_LEN - 3);

    sb.append(HAS_WATER).append(" water; ");
    sb.append(HAS_WEED).append(" weed; ");
    sb.append(HAS_QUALITY).append(" quality; ");
    sb.append(HAS_HARVEST).append(" harvest\n");
    sb.append(crops.size()).append(" crops; ").append(distinct).append(" distinct\n");
    sb.append(water).append(" (").append(100 * water / 81).append("%) water; ");
    sb.append(weed).append(" (").append(100 * weed / 81).append("%) weed; ");
    sb.append(quality).append(" (").append(100 * quality / 81).append("%) quality; ");
    sb.append(harvest).append(" (").append(100 * harvest / 81).append("%) harvest\n");
    sb.append("Open in Garden Planner: ").append(layoutUrl).append('\n');
    return sb.toString();
  }
}
