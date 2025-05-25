package io.nuevedejun.gadantic;

import io.nuevedejun.gadantic.PlotDecoder.RichCrop;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

@XSlf4j
public record Plot(
    Set<RichCrop> crops,
    int water, int weed, int quality, int harvest, int distinct,
    String layoutUrl) {

  private static final int CELL_WIDTH = 11;
  private static final int LINE_LEN = 9 * CELL_WIDTH + 2;
  private static final int LINES = 9 * 2 + 1;

  private static final String HAS_WATER = "♠";
  private static final String HAS_WEED = "☙";
  private static final String HAS_QUALITY = "★";
  private static final String HAS_HARVEST = "⚘";

  public static final List<String> LEGEND = List.of(
      HAS_WATER + " water",
      HAS_WEED + " weed",
      HAS_QUALITY + " quality",
      HAS_HARVEST + " harvest");

  @RequiredArgsConstructor
  private enum Border {
    UPPER_LEFT('┌'), UPPER_RIGHT('┐'), LOWER_RIGHT('┘'), LOWER_LEFT('└'),
    LEFT_T('├'), UPPER_T('┬'), RIGHT_T('┤'), LOWER_T('┴'),
    CROSS('┼'), DASH('─'), PIPE('│'),
    BLANK(' ');

    private static final Map<Character, Border> MAP = Map.copyOf(Stream.of(Border.values())
        .collect(Collectors.toMap(b -> b.character, Function.identity())));

    private static Border from(final char c) {
      log.entry((int) c);
      return log.exit(MAP.get(c));
    }

    private final char character;
  }

  public String tableString() {
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < LINES; i++) {
      // reserve space for the table
      sb.repeat(Border.BLANK.character, LINE_LEN - 1).append('\n');
    }
    sb.deleteCharAt(sb.length() - 1);

    for (final var annotated : crops) {
      log.trace("Drawing cell of {}", annotated);

      final int start = annotated.x() * CELL_WIDTH + 2 * annotated.y() * LINE_LEN;
      final Crop crop = annotated.crop();
      drawCell(sb, start, crop.size());

      final String name = crop.name();
      final String badges = (annotated.has(WATER) ? HAS_WATER : "")
          + (annotated.has(WEED) ? HAS_WEED : "")
          + (annotated.has(QUALITY) ? HAS_QUALITY : "")
          + (annotated.has(HARVEST) ? HAS_HARVEST : "");
      final int desired = crop.size() * CELL_WIDTH - 1 - badges.length();
      final String format = "%-" + desired + "." + desired + "s%s";
      final String text = String.format(format, name, badges);
      replace(sb, start + LINE_LEN + 1, text);
    }
    // draw left border
    drawVertical(sb, 2 * LINE_LEN - 2, LINES - 2);
    // draw lower border
    drawHorizontal(sb, 1 + (LINES - 1) * LINE_LEN, LINE_LEN - 3);
    return sb.toString();
  }

  private void replace(final StringBuilder sb, final int start, final String str) {
    sb.replace(start, start + str.length(), str);
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
      case UPPER_LEFT, BLANK -> Border.UPPER_LEFT;
      case UPPER_RIGHT, UPPER_T, DASH -> Border.UPPER_T;
      case LOWER_RIGHT, CROSS, RIGHT_T, LOWER_T -> Border.CROSS;
      case LOWER_LEFT, LEFT_T, PIPE -> Border.LEFT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  private void drawUpperRight(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case UPPER_RIGHT, BLANK -> Border.UPPER_RIGHT;
      case UPPER_LEFT, UPPER_T, DASH -> Border.UPPER_T;
      case LOWER_LEFT, CROSS, LEFT_T, LOWER_T -> Border.CROSS;
      case LOWER_RIGHT, RIGHT_T, PIPE -> Border.RIGHT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  private void drawLowerRight(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case LOWER_RIGHT, BLANK -> Border.LOWER_RIGHT;
      case LOWER_LEFT, LOWER_T, DASH -> Border.LOWER_T;
      case UPPER_LEFT, UPPER_T, CROSS, LEFT_T -> Border.CROSS;
      case UPPER_RIGHT, RIGHT_T, PIPE -> Border.RIGHT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  private void drawLowerLeft(final StringBuilder sb, final int pos) {
    final Border replacement = switch (Border.from(sb.charAt(pos))) {
      case LOWER_LEFT, BLANK -> Border.LOWER_LEFT;
      case LOWER_RIGHT, LOWER_T, DASH -> Border.LOWER_T;
      case UPPER_RIGHT, UPPER_T, RIGHT_T, CROSS -> Border.CROSS;
      case UPPER_LEFT, LEFT_T, PIPE -> Border.LEFT_T;
    };
    sb.setCharAt(pos, replacement.character);
  }

  private void drawHorizontal(final StringBuilder sb, final int start, final int len) {
    final StringBuilder line = new StringBuilder();
    for (int i = 0; i < len; i++) {
      final Border replacement = switch (Border.from(sb.charAt(start + i))) {
        case DASH, BLANK -> Border.DASH;
        case LOWER_LEFT, LOWER_RIGHT, LOWER_T -> Border.LOWER_T;
        case UPPER_LEFT, UPPER_RIGHT, UPPER_T -> Border.UPPER_T;
        case LEFT_T, RIGHT_T, PIPE, CROSS -> Border.CROSS;
      };
      line.append(replacement.character);
    }
    replace(sb, start, line.toString());
  }

  private void drawVertical(final StringBuilder sb, final int start, final int len) {
    for (int i = 0; i < len; i++) {
      final int pos = start + i * LINE_LEN;
      final Border replacement = switch (Border.from(sb.charAt(pos))) {
        case PIPE, BLANK -> Border.PIPE;
        case LOWER_LEFT, UPPER_LEFT, LEFT_T -> Border.LEFT_T;
        case LOWER_RIGHT, UPPER_RIGHT, RIGHT_T -> Border.RIGHT_T;
        case LOWER_T, UPPER_T, CROSS, DASH -> Border.CROSS;
      };
      sb.setCharAt(pos, replacement.character);
    }
  }
}
