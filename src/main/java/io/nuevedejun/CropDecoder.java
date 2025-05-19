package io.nuevedejun;

@FunctionalInterface
public interface CropDecoder {

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

  static CropDecoder ofPerk(final int perk) {
    return switch (perk) {
      case 0 -> CropDecoder::decodeWater;
      case 1 -> CropDecoder::decodeWeed;
      case 2 -> CropDecoder::decodeQuality;
      default /* 3, 4 */ -> CropDecoder::decodeHarvest;
    };
  }

  Crop get(int area, int kind);
}
