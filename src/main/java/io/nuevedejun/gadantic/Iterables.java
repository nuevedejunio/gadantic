package io.nuevedejun.gadantic;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility class for grid and coordinate operations.
 */
public final class Iterables {

  private Iterables() {throw new UnsupportedOperationException("do not instantiate");}

  /**
   * Represents a 2D coordinate.
   */
  public interface Coordinate {

    /**
     * Returns the x coordinate.
     *
     * @return x value
     */
    int x();

    /**
     * Returns the y coordinate.
     *
     * @return y value
     */
    int y();

    /**
     * Adds another coordinate to this one.
     *
     * @param op the coordinate to add
     * @return new coordinate with summed values
     */
    default Coordinate plus(final Coordinate op) {
      return new CoordinateImpl(x() + op.x(), y() + op.y());
    }
  }


  private record CoordinateImpl(int x, int y) implements Coordinate {
  }


  /**
   * Represents a grid cell with coordinates and a value.
   *
   * @param <T> the type of value stored in the cell
   */
  public record Cell<T>(int x, int y, T value) implements Coordinate {
  }


  /**
   * Represents a 2D grid of values.
   *
   * @param <T> the type of values stored in the grid
   */
  public interface Grid<T> extends Iterable<Cell<T>> {

    /**
     * Returns the grid width.
     *
     * @return width in cells
     */
    int width();

    /**
     * Returns the grid height.
     *
     * @return height in cells
     */
    int height();

    /**
     * Gets the value at the specified coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the value at that position
     */
    T at(int x, int y);

    /**
     * Gets the value at the specified coordinate.
     *
     * @param coordinate the coordinate
     * @return the value at that position
     */
    default T at(final Coordinate coordinate) {
      return at(coordinate.x(), coordinate.y());
    }

    /**
     * Sets the value at the specified coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param value the value to set
     */
    void set(int x, int y, T value);

    /**
     * Sets the value at the specified coordinate.
     *
     * @param coordinate the coordinate
     * @param value the value to set
     */
    default void set(final Coordinate coordinate, final T value) {
      set(coordinate.x(), coordinate.y(), value);
    }

    /**
     * Checks if the coordinate is within grid bounds.
     *
     * @param c the coordinate to check
     * @return true if within bounds
     */
    default boolean contains(final Coordinate c) {
      return c.x() >= 0 && c.x() < width()
          && c.y() >= 0 && c.y() < height();
    }

    class Impl<T> implements Grid<T> {
      private final ArrayList<T> backed;
      private final int width;
      private final int height;

      private Impl(final List<T> backed, final int width) {
        lengthFitsExactlyInWidth(backed.size(), width);
        this.backed = new ArrayList<>(backed);
        this.width = width;
        this.height = backed.size() / width;
      }

      private static void lengthFitsExactlyInWidth(final int len, final int width) {
        if (len % width > 0) {
          throw new IllegalArgumentException(
              "length " + len + " does not fit exactly in width " + width);
        }
      }

      @Override
      public int width() {
        return width;
      }

      @Override
      public int height() {
        return height;
      }

      @Override
      public T at(final int x, final int y) {
        return backed.get(x + y * width);
      }

      @Override
      public void set(final int x, final int y, final T value) {
        backed.set(x + y * width, value);
      }

      @Override
      public Iterator<Cell<T>> iterator() {
        return new Iterator<>() {
          private int pos = 0;

          @Override
          public boolean hasNext() {
            return pos < backed.size();
          }

          @Override
          public Cell<T> next() {
            if (!hasNext()) {
              throw new NoSuchElementException("out of bounds for length " + backed.size());
            }
            final var next = new Cell<>(pos % width, pos / width, backed.get(pos));
            pos++;
            return next;
          }
        };
      }
    }
  }

  /**
   * Creates a grid from a list of values.
   *
   * @param <T> the type of values
   * @param backed the backing list
   * @param width the grid width
   * @return a new grid
   */
  public static <T> Grid<T> grid(final List<T> backed, final int width) {
    return new Grid.Impl<>(backed, width);
  }

  /**
   * Shuffles the elements in a finite iterable.
   * @implNote I created this method in an interface to more easily mock it in unit tests.
   */
  @FunctionalInterface
  public interface Shuffler {
    /**
     * Shuffles the elements of an iterable.
     *
     * @param <T> the element type
     * @param original the iterable to shuffle
     * @return shuffled list
     */
    <T> List<T> shuffle(final Iterable<? extends T> original);

    @ApplicationScoped
    class Impl implements Shuffler {
      @Override
      public <T> List<T> shuffle(final Iterable<? extends T> original) {
        final ArrayList<T> aux = new ArrayList<>();
        original.forEach(aux::add);
        Collections.shuffle(aux);
        return List.copyOf(aux);
      }
    }
  }

  /**
   * Creates an iterable of coordinates from (0,0) to (x,y).
   *
   * @param x the x limit (exclusive)
   * @param y the y limit (exclusive)
   * @return iterable of coordinates
   */
  public static Iterable<Coordinate> coordinates(final int x, final int y) {
    return coordinates(0, x, 0, y);
  }

  /**
   * Creates an iterable of coordinates within specified bounds.
   *
   * @param x0 the x start (inclusive)
   * @param x the x limit (exclusive)
   * @param y0 the y start (inclusive)
   * @param y the y limit (exclusive)
   * @return iterable of coordinates
   */
  public static Iterable<Coordinate> coordinates(
      final int x0, final int x,
      final int y0, final int y) {

    checkLowerUpperLimits(x0, x);
    checkLowerUpperLimits(y0, y);

    final int spanX = x - x0;
    final int spanY = y - y0;
    final int limit = spanX * spanY;

    return () -> new Iterator<>() {
      int pos = 0;

      @Override
      public boolean hasNext() {
        return pos < limit;
      }

      @Override
      public Coordinate next() {
        if (!hasNext()) {
          throw new NoSuchElementException(
              "out of limits x=[" + x0 + ", " + x + "); y=[" + y0 + ", " + y + ")");
        }
        final int cx = x0 + pos % spanX;
        final int cy = y0 + pos / spanY;
        pos++;
        return new CoordinateImpl(cx, cy);
      }
    };
  }

  private static void checkLowerUpperLimits(final int lower, final int upper) {
    if (lower > upper) {
      throw new IllegalArgumentException(
          "lower limit " + lower + " is greater than upper limit " + upper);
    }
  }

  /**
   * Shortcut to create an array of objects.
   *
   * @param args the objects provided as a vararg parameter
   * @return the vararg array
   */
  @SafeVarargs
  public static <T> T[] arr(final T... args) {
    return args;
  }
}
