package io.nuevedejun.gadantic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IterablesTest {

  private final Iterables.Shuffler shuffler = Iterables.Shuffler.create();

  @Test
  void testShuffle() {
    final Set<Integer> numbers = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

    final List<Integer> result = shuffler.shuffle(numbers);

    // have same elements
    assertEquals(numbers, Set.copyOf(result));
    // have different order
    assertNotEquals(List.copyOf(numbers), result);
  }
}
