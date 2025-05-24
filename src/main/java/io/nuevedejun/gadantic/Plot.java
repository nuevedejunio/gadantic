package io.nuevedejun.gadantic;

import static io.nuevedejun.gadantic.Plot.Border.BLANK;
import static io.nuevedejun.gadantic.Plot.Border.CROSS;
import static io.nuevedejun.gadantic.Plot.Border.DASH;
import static io.nuevedejun.gadantic.Plot.Border.LEFT_T;
import static io.nuevedejun.gadantic.Plot.Border.LOWER_LEFT;
import static io.nuevedejun.gadantic.Plot.Border.LOWER_RIGHT;
import static io.nuevedejun.gadantic.Plot.Border.LOWER_T;
import static io.nuevedejun.gadantic.Plot.Border.PIPE;
import static io.nuevedejun.gadantic.Plot.Border.RIGHT_T;
import static io.nuevedejun.gadantic.Plot.Border.UPPER_LEFT;
import static io.nuevedejun.gadantic.Plot.Border.UPPER_RIGHT;
import static io.nuevedejun.gadantic.Plot.Border.UPPER_T;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.nuevedejun.gadantic.PlotDecoder.RichCrop;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

@RequiredArgsConstructor
@XSlf4j
public class Plot {

  @RequiredArgsConstructor
  enum Border {
    UPPER_LEFT('┌'), UPPER_RIGHT('┐'), LOWER_RIGHT('┘'), LOWER_LEFT('└'),
    LEFT_T('├'), UPPER_T('┬'), RIGHT_T('┤'), LOWER_T('┴'),
    CROSS('┼'), DASH('─'), PIPE('│'),
    BLANK(' ');

    static final Map<Character, Border> MAP = Map.copyOf(Stream.of(Border.values())
        .collect(Collectors.toMap(b -> b.character, Function.identity())));

    static Border from(final char c) {
      log.entry((int) c);
      return log.exit(MAP.get(c));
    }

    final char character;
  }

  static final int CELL_WIDTH = 11;
  static final int LINE_LEN = 9 * CELL_WIDTH + 2;
  static final int LINES = 9 * 2 + 1;

  static final String HAS_WATER = "♠";
  static final String HAS_WEED = "☙";
  static final String HAS_QUALITY = "★";
  static final String HAS_HARVEST = "⚘";

  final Set<RichCrop> crops;
  final int water;
  final int weed;
  final int quality;
  final int harvest;
  final int distinct;
  final String layoutUrl;

  void replace(final StringBuilder sb, final int start, final String str) {
    sb.replace(start, start + str.length(), str);
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
