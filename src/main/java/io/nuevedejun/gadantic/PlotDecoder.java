package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.Iterables.Cell;
import io.nuevedejun.gadantic.Iterables.Grid;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.Perk;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.nuevedejun.gadantic.Gadantic.LOG_FQCN;
import static io.nuevedejun.gadantic.Iterables.arr;
import static io.nuevedejun.gadantic.Iterables.coordinates;
import static io.nuevedejun.gadantic.Iterables.grid;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

public interface PlotDecoder {

  class RichCrop {
    final Crop crop;
    final int x;
    final int y;

    private final Map<Perk, Integer> perks = new EnumMap<>(Map.of(
        WATER, 0,
        WEED, 0,
        QUALITY, 0,
        HARVEST, 0));

    public RichCrop(final Crop crop, final int x, final int y) {
      this.crop = crop;
      this.x = x;
      this.y = y;
    }

    /**
     * Apply this crop's perk to a target crop.
     *
     * @return {@code true} is the perk was applies; {@code false} otherwise
     */
    private boolean buff(final RichCrop other) {
      if (this.crop != other.crop) {
        final int current = other.perks.get(crop.perk);
        final boolean affected = current < other.crop.size;
        other.perks.put(crop.perk, current + 1);
        return affected;
      }
      return false;
    }

    public boolean has(final Perk perk) {
      return perks.get(perk) >= crop.size;
    }

    @Override
    public String toString() {
      return "RichCrop{crop=" + crop + ", x=" + x + ", y=" + y + '}';
    }
  }

  Plot decode(final Genotype<IntegerGene> genotype);

  @ApplicationScoped
  class Impl implements PlotDecoder {

    @Override
    public Plot decode(final Genotype<IntegerGene> genotype) {
      final Grid<RichCrop> plot = grid(Arrays.asList(new RichCrop[81]), 9);
      final var chromosome = genotype.chromosome().as(IntegerChromosome.class);
      final var geneGrid = grid(Arrays.stream(chromosome.toArray()).mapToObj(Crop::at).toList(), 9);
      for (final var cell : geneGrid) {
        if (plot.at(cell) == null) {
          fillCropTile(plot, cell);
        }
      }
      int applied = 0;
      final HashSet<RichCrop> set = HashSet.newHashSet(9 * 9);
      for (final var cell : plot) {
        applied += applyBuffs(plot, cell);
        set.add(cell.value());
      }
      Log.trace(LOG_FQCN, "Set of decoded crops is: {0}", arr(set), null);

      int water = 0;
      int weed = 0;
      int quality = 0;
      int harvest = 0;
      int available = -36;
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
        available += 4 * crop.crop.size;
      }
      final double efficiency = (double) applied / available;
      final int distinct = set.stream().map(r -> r.crop).collect(Collectors.toSet()).size();
      final String layoutUrl = createLayoutUrl(plot);
      return new Plot(Set.of(set.toArray(new RichCrop[0])),
          water, weed, quality, harvest, distinct, efficiency, layoutUrl);
    }

    private void fillCropTile(final Grid<RichCrop> plot, final Cell<Crop> cell) {
      final var rich = new RichCrop(cell.value(), cell.x(), cell.y());
      for (final var c : coordinates(cell.value().size, cell.value().size)) {
        plot.set(cell.plus(c), rich);
      }
    }

    private int applyBuffs(final Grid<RichCrop> crops, final Cell<RichCrop> cell) {
      final var crop = cell.value();
      int count = 0;
      for (final var c : coordinates(-1, 2, -1, 2)) {
        if (c.x() != c.y() && c.x() != -c.y() && crops.contains(cell.plus(c))) {
          final var target = crops.at(cell.plus(c));
          if (crop.buff(target)) {
            count++;
          }
        }
      }
      return count;
    }

    private String createLayoutUrl(final Grid<RichCrop> grid) {
      final StringBuilder sb = new StringBuilder()
          .append("https://palia-garden-planner.vercel.app/?layout=")
          .append("v0.4_D-111-111-111_CR");

      for (int i = 0; i < 9; i += 3) {
        for (int j = 0; j < 9; j += 3) {
          sb.append('-');
          for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
              sb.append(mapLayout(grid.at(l, k).crop));
            }
          }
        }
      }

      return sb.toString();
    }

    private String mapLayout(final Crop crop) {
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
  }
}
