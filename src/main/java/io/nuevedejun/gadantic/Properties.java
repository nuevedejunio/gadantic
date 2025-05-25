package io.nuevedejun.gadantic;

import lombok.extern.slf4j.XSlf4j;

import java.util.OptionalLong;
import java.util.function.Function;

public interface Properties {

  double waterWeight(double fallback);

  double weedWeight(double fallback);

  double qualityWeight(double fallback);

  double harvestWeight(double fallback);

  double distinctWeight(double fallback);

  int population(int fallback);

  OptionalLong generations();

  String saveFile(String fallback);

  long shutdownMillis(long fallback);

  /**
   * Returns an instance of {@code Properties} that obtains property values from the system
   * properties.
   */
  static Properties system() {
    return new System();
  }

  /** Obtains property values from the system properties. */
  @XSlf4j
  class System implements Properties {
    public static final String WEIGHT_WATER_PROPERTY = "weight.water";
    public static final String WEIGHT_WEED_PROPERTY = "weight.weed";
    public static final String WEIGHT_QUALITY_PROPERTY = "weight.quality";
    public static final String WEIGHT_HARVEST_PROPERTY = "weight.harvest";
    public static final String WEIGHT_DISTINCT_PROPERTY = "weight.distinct";
    public static final String POPULATION_PROPERTY = "population";
    public static final String GENERATIONS_PROPERTY = "generations";
    public static final String SAVE_FILE_PROPERTY = "save-file";
    public static final String SHUTDOWN_MILLIS_PROPERTY = "shutdown.wait-millis";

    private System() {}

    /**
     * Reads a system property and applies a mapping function to it.
     *
     * @param name the name of the property
     * @param mapper the mapping function
     * @param defaultValue the value to use if the property does not exist
     * @return the property value after applying the mapping function, if the property is present.
     * The default value otherwise.
     */
    private <T> T property(final String name, final Function<String, T> mapper,
        final T defaultValue) {
      final String got = java.lang.System.getProperty(name);
      if (got == null) {
        log.info("Using property {}: {} (default)", name, defaultValue);
        return defaultValue;
      } else {
        log.info("Using property {}: {}", name, got);
        return mapper.apply(got);
      }
    }

    @Override
    public double waterWeight(final double fallback) {
      return property(WEIGHT_WATER_PROPERTY, Double::parseDouble, fallback);
    }

    @Override
    public double weedWeight(final double fallback) {
      return property(WEIGHT_WEED_PROPERTY, Double::parseDouble, fallback);
    }

    @Override
    public double qualityWeight(final double fallback) {
      return property(WEIGHT_QUALITY_PROPERTY, Double::parseDouble, fallback);
    }

    @Override
    public double harvestWeight(final double fallback) {
      return property(WEIGHT_HARVEST_PROPERTY, Double::parseDouble, fallback);
    }

    @Override
    public double distinctWeight(final double fallback) {
      return property(WEIGHT_DISTINCT_PROPERTY, Double::parseDouble, fallback);
    }

    @Override
    public int population(final int fallback) {
      return property(POPULATION_PROPERTY, Integer::parseInt, fallback);
    }

    @Override
    public OptionalLong generations() {
      final Long result = property(GENERATIONS_PROPERTY, Long::parseLong, null);
      return result == null ? OptionalLong.empty() : OptionalLong.of(result);
    }

    @Override
    public String saveFile(final String fallback) {
      return property(SAVE_FILE_PROPERTY, Function.identity(), fallback);
    }

    @Override
    public long shutdownMillis(final long fallback) {
      return property(SHUTDOWN_MILLIS_PROPERTY, Long::parseLong, fallback);
    }
  }
}
