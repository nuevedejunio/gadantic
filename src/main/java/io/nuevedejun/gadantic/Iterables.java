package io.nuevedejun.gadantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class Iterables {

  private Iterables() {throw new UnsupportedOperationException("do not instantiate");}

  public interface Coordinate {

    int x();

    int y();

    default Coordinate plus(final Coordinate op) {
      return new CoordinateImpl(x() + op.x(), y() + op.y());
    }
  }


  private record CoordinateImpl(int x, int y) implements Coordinate {
  }


  public record Cell<T>(int x, int y, T value) implements Coordinate {
  }


  public interface Grid<T> extends Iterable<Cell<T>> {

    int width();

    int height();

    T at(int x, int y);

    default T at(final Coordinate coordinate) {
      return at(coordinate.x(), coordinate.y());
    }

    void set(int x, int y, T value);

    default void set(final Coordinate coordinate, final T value) {
      set(coordinate.x(), coordinate.y(), value);
    }

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

  public static <T> Grid<T> grid(final List<T> backed, final int width) {
    return new Grid.Impl<>(backed, width);
  }

  /**
   * Shuffles the elements in a finite iterable. Created this method in an interface to more easily
   * mock it in unit tests.
   */
  @FunctionalInterface
  public interface Shuffler {
    <T> List<T> shuffle(final Iterable<? extends T> original);

    static Shuffler create() {
      return new Impl();
    }

    class Impl implements Shuffler {
      private Impl() {}

      @Override
      public <T> List<T> shuffle(final Iterable<? extends T> original) {
        final ArrayList<T> aux = new ArrayList<>();
        original.forEach(aux::add);
        Collections.shuffle(aux);
        return List.copyOf(aux);
      }
    }
  }

  public static Iterable<Coordinate> coordinates(final int x, final int y) {
    return coordinates(0, x, 0, y);
  }

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
