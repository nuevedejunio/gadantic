package io.nuevedejun;

import lombok.Getter;
import lombok.experimental.Accessors;

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

  Crop(final int size) {
    this.size = size;
  }
}
