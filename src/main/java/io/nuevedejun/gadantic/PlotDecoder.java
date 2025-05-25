package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.PlotIterableFactory.TiledCrop;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import io.nuevedejun.gadantic.PlotPhenotype.Perk;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.XSlf4j;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.nuevedejun.gadantic.PlotPhenotype.Perk.HARVEST;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.QUALITY;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WATER;
import static io.nuevedejun.gadantic.PlotPhenotype.Perk.WEED;

public interface PlotDecoder {

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  @ToString
  class RichCrop {
    @Getter
    private final Crop crop;
    @Getter
    private final int x;
    @Getter
    private final int y;

    @ToString.Exclude
    private final Map<Perk, Integer> perks = new EnumMap<>(Map.of(
        WATER, 0,
        WEED, 0,
        QUALITY, 0,
        HARVEST, 0));

    private void buff(final RichCrop other) {
      if (this.crop != other.crop) {
        other.perks.put(crop.perk(), other.perks.get(crop.perk()) + 1);
      }
    }

    public boolean has(final Perk perk) {
      return perks.get(perk) >= crop.size();
    }
  }

  Plot decode(final Genotype<IntegerGene> genotype);

  static Standard standard(final PlotIterableFactory iterableFactory) {
    return new Standard(iterableFactory);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @XSlf4j
  class Standard implements PlotDecoder {
    private final PlotIterableFactory iterableFactory;

    @Override
    public Plot decode(final Genotype<IntegerGene> genotype) {
      final RichCrop[][] array = new RichCrop[9][9];
      for (final var tile : iterableFactory.tiles(genotype, true)) {
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
          water += crop.crop.size() * crop.crop.size();
        }
        if (crop.has(WEED)) {
          weed += crop.crop.size() * crop.crop.size();
        }
        if (crop.has(QUALITY)) {
          quality += crop.crop.size() * crop.crop.size();
        }
        if (crop.has(HARVEST)) {
          harvest += crop.crop.size() * crop.crop.size();
        }
      }
      final int distinct = set.stream().map(r -> r.crop).collect(Collectors.toSet()).size();
      final String layoutUrl = createLayoutUrl(array);
      return new Plot(Set.of(set.toArray(new RichCrop[0])), water, weed, quality, harvest, distinct, layoutUrl);
    }

    private void fillCropTile(final RichCrop[][] array, final TiledCrop tile) {
      final var rich = new RichCrop(tile.crop(), tile.x(), tile.y());
      for (int i = 0; i < tile.crop().size(); i++) {
        for (int j = 0; j < tile.crop().size(); j++) {
          array[tile.x() + i][tile.y() + j] = rich;
        }
      }
    }

    private void applyBuffs(final RichCrop[][] crops, final int x, final int y) {
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

    private String createLayoutUrl(final RichCrop[][] array) {
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
