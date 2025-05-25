package io.nuevedejun.gadantic;

import lombok.extern.slf4j.XSlf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public interface Properties {

  String getString(String key);

  int getInt(String key);

  long getLong(String key);

  double getDouble(String key);

  static File file() {
    return new File();
  }

  /** Obtains property values from the system properties. */
  @XSlf4j
  class File implements Properties {
    private final java.util.Properties props;

    private File() {
      final var defaults = new java.util.Properties();
      this.props = new java.util.Properties(defaults);
      try (final InputStream din = getClass().getResourceAsStream("/defaults.properties")) {
        defaults.load(din);
      } catch (final IOException e) {
        throw new IllegalStateException("Exception caused by dev error. Debug.", e);
      }
      try (final InputStream ain = Files.newInputStream(Paths.get("application.properties"))) {
        props.load(ain);
      } catch (final NoSuchFileException e) {
        log.info("File application.properties was not found in working directory. "
            + "Default properties will be used.");
      } catch (final IOException e) {
        log.warn("An exception prevented reading file application.properties. "
            + "Default properties will be used.", e);
      }
    }

    private <T> T log(final String key, final T value) {
      log.info("Using property {}={}", key, value);
      return value;
    }

    @Override
    public String getString(final String key) {
      return log(key, props.getProperty(key));
    }

    @Override
    public int getInt(final String key) {
      return log(key, Integer.parseInt(props.getProperty(key)));
    }

    @Override
    public long getLong(final String key) {
      return log(key, Long.parseLong(props.getProperty(key)));
    }

    @Override
    public double getDouble(final String key) {
      return log(key, Double.parseDouble(props.getProperty(key)));
    }
  }
}
