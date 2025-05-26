package io.nuevedejun.gadantic;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

/**
 * Mock the shuffler to provide a predictable iteration order.
 * <p>
 * This implementation allows changing the order of iteration for special classes (only one for
 * now).
 */
public class PrioritizedShuffler implements Iterables.Shuffler {

  public record Coordinate(int x, int y) implements Iterables.Coordinate {
  }


  private SequencedSet<Coordinate> priority = new LinkedHashSet<>();

  /**
   * Add the provided coordinates to the beginning of the iterator.
   *
   * @param coordinates set of coordinates to move to the beginning of the iteration. Iteration will
   * occur in the same order that coordinates appear in the provided set.
   */
  public void prioritize(final SequencedSet<Coordinate> coordinates) {
    priority = coordinates;
  }

  @Override
  public <T> List<T> shuffle(final Iterable<? extends T> original) {
    final LinkedHashSet<T> aux = new LinkedHashSet<>();
    original.forEach(aux::add);

    priority.reversed().forEach(c -> aux.stream()
        // find coordinates within elements
        .filter(el -> el instanceof final Iterables.Coordinate ic
            // filter those that have the same as the prioritized
            && ic.x() == c.x() && ic.y() == c.y())
        .findAny()
        // put them at the beginning of the iteration
        .ifPresent(aux::addFirst));

    return List.copyOf(aux);
  }
}
