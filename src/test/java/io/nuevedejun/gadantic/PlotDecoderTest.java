package io.nuevedejun.gadantic;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.nuevedejun.gadantic.Iterables.Cell;
import io.nuevedejun.gadantic.PlotPhenotype.Crop;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;

import static io.nuevedejun.gadantic.PlotCodec.encode;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.APPLES;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.BEANS;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.BLUEBERRIES;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.BOK_CHOY;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.CABBAGE;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.CARROTS;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.CORN;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.COTTON;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.ONIONS;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.PEPPERS;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.POTATOES;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.PUMPKINS;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.RICE;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.TOMATOES;
import static io.nuevedejun.gadantic.PlotPhenotype.Crop.WHEAT;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class PlotDecoderTest {

  final PlotDecoder decoder = PlotDecoder.create();

  static List<Arguments> decodeTestCases() {
    final int[] fewCropsArray = {
        // @formatter:off
        14,  0,  0,  9, 13,  0, 14,  0,  0,
         0,  0,  0,  9,  0,  0,  0,  0,  0,
         0,  0,  0,  7,  9,  9,  0,  0,  0,
        13,  0,  9, 14,  0,  0,  7,  9,  9,
         0,  0,  9,  0,  0,  0,  9, 13,  0,
         9,  9,  7,  0,  0,  0,  9,  0,  0,
        14,  0,  0,  9,  9,  7, 14,  0,  0,
         0,  0,  0, 13,  0,  9,  0,  0,  0,
         0,  0,  0,  0,  0,  9,  0,  0,  0
        // @formatter:on
    };
    final var fewCropsSet = Set.of(
        new Cell<>(0, 0, APPLES), new Cell<>(0, 6, APPLES), new Cell<>(6, 6, APPLES),
        new Cell<>(6, 0, APPLES), new Cell<>(3, 3, APPLES), new Cell<>(4, 0, PUMPKINS),
        new Cell<>(0, 3, PUMPKINS), new Cell<>(3, 7, PUMPKINS), new Cell<>(7, 4, PUMPKINS),
        new Cell<>(3, 0, COTTON), new Cell<>(3, 1, COTTON), new Cell<>(4, 2, COTTON),
        new Cell<>(5, 2, COTTON), new Cell<>(2, 3, COTTON), new Cell<>(2, 4, COTTON),
        new Cell<>(0, 5, COTTON), new Cell<>(1, 5, COTTON), new Cell<>(3, 6, COTTON),
        new Cell<>(4, 6, COTTON), new Cell<>(5, 7, COTTON), new Cell<>(5, 8, COTTON),
        new Cell<>(7, 3, COTTON), new Cell<>(8, 3, COTTON), new Cell<>(6, 4, COTTON),
        new Cell<>(6, 5, COTTON), new Cell<>(3, 2, ONIONS), new Cell<>(2, 5, ONIONS),
        new Cell<>(5, 6, ONIONS), new Cell<>(6, 3, ONIONS));

    final int[] allCropsArray = {
        // @formatter:off
         0,  1,  2,  3,  4,  5,  6,  7,  8,
         9, 10,  0, 11,  0, 12,  0, 13,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,
        14,  0,  0, 14,  0,  0, 14,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,
        14,  0,  0, 14,  0,  0, 14,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0
        // @formatter:on
    };
    final var allCropsSet = Set.of(
        new Cell<>(0, 0, TOMATOES), new Cell<>(1, 0, POTATOES), new Cell<>(2, 0, CABBAGE),
        new Cell<>(3, 0, RICE), new Cell<>(4, 0, WHEAT), new Cell<>(5, 0, CORN),
        new Cell<>(6, 0, CARROTS), new Cell<>(7, 0, ONIONS), new Cell<>(8, 0, BOK_CHOY),
        new Cell<>(0, 1, COTTON), new Cell<>(1, 1, BLUEBERRIES), new Cell<>(3, 1, BEANS),
        new Cell<>(5, 1, PEPPERS), new Cell<>(7, 1, PUMPKINS), new Cell<>(0, 2, TOMATOES),
        new Cell<>(0, 3, APPLES), new Cell<>(3, 3, APPLES), new Cell<>(6, 3, APPLES),
        new Cell<>(0, 6, APPLES), new Cell<>(3, 6, APPLES), new Cell<>(6, 6, APPLES));

    return List.of(
        argumentSet("Few distinct crops; 100% quality ;)",
            fewCropsArray, fewCropsSet, 0, 17, 81, 36, 4,
            "https://palia-garden-planner.vercel.app/?layout=v0.4_D-111-111-111_CR-AAAAAAAAA-CoPmPmCoPmPmOCoCo-AAAAAAAAA-PmPmCoPmPmCoCoCoO-AAAAAAAAA-OCoCoCoPmPmCoPmPm-AAAAAAAAA-CoCoOPmPmCoPmPmCo-AAAAAAAAA"),
        argumentSet("All crops; varied measurements",
            allCropsArray, allCropsSet, 9, 8, 27, 24, 15,
            "https://palia-garden-planner.vercel.app/?layout=v0.4_D-111-111-111_CR-TPCbCoBBTBB-RWCrBtBtSBtBtS-COBkSPmPmSPmPm-AAAAAAAAA-AAAAAAAAA-AAAAAAAAA-AAAAAAAAA-AAAAAAAAA-AAAAAAAAA"));
  }

  @ParameterizedTest
  @MethodSource("decodeTestCases")
  void testDecode(final int[] array, final Set<Cell<Crop>> expectedCrops, final int expectedWater,
      final int expectedWeed, final int expectedQuality, final int expectedHarvest,
      final int expectedDistinct, final String expectedUrl) {

    final Genotype<IntegerGene> genotype = encode(array).genotype();

    final Plot result = decoder.decode(genotype);

    final var actualCrops = result.crops().stream()
        .map(rc -> new Cell<>(rc.x(), rc.y(), rc.crop()))
        .collect(toSet());
    assertEquals(expectedCrops, actualCrops);
    assertEquals(expectedWater, result.water());
    assertEquals(expectedWeed, result.weed());
    assertEquals(expectedQuality, result.quality());
    assertEquals(expectedHarvest, result.harvest());
    assertEquals(expectedDistinct, result.distinct());
    assertEquals(expectedUrl, result.layoutUrl());
  }
}
