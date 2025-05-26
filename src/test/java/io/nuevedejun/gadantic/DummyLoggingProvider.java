package io.nuevedejun.gadantic;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.NOP_FallbackServiceProvider;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * This dummy implementation returns {@code true} to all the methods {@code #is*Level*Enabled()}. It
 * does not do any logging at all, but will allow increasing the code coverage a little bit.
 */
public class DummyLoggingProvider extends NOP_FallbackServiceProvider
    implements SLF4JServiceProvider {

  public static class DummyLogger implements Logger {
    public static final DummyLogger INSTANCE = new DummyLogger();

    private DummyLogger() {}

    @Override
    public String getName() {
      return "Dummy";
    }

    @Override
    public boolean isTraceEnabled() {
      return true;
    }

    @Override
    public void trace(final String msg) {/*NOP*/}

    @Override
    public void trace(final String format, final Object arg) {/*NOP*/}

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {/*NOP*/}

    @Override
    public void trace(final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void trace(final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isTraceEnabled(final Marker marker) {
      return true;
    }

    @Override
    public void trace(final Marker marker, final String msg) {/*NOP*/}

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {/*NOP*/}

    @Override
    public void trace(final Marker marker, final String format, final Object arg1,
        final Object arg2) {/*NOP*/}

    @Override
    public void trace(final Marker marker, final String format, final Object... argArray) {/*NOP*/}

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isDebugEnabled() {
      return true;
    }

    @Override
    public void debug(final String msg) {/*NOP*/}

    @Override
    public void debug(final String format, final Object arg) {/*NOP*/}

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {/*NOP*/}

    @Override
    public void debug(final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void debug(final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isDebugEnabled(final Marker marker) {
      return true;
    }

    @Override
    public void debug(final Marker marker, final String msg) {/*NOP*/}

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {/*NOP*/}

    @Override
    public void debug(final Marker marker, final String format, final Object arg1,
        final Object arg2) {/*NOP*/}

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isInfoEnabled() {
      return true;
    }

    @Override
    public void info(final String msg) {/*NOP*/System.out.println("Holy shit it worked");}

    @Override
    public void info(final String format, final Object arg) {/*NOP*/}

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {/*NOP*/}

    @Override
    public void info(final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void info(final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isInfoEnabled(final Marker marker) {
      return true;
    }

    @Override
    public void info(final Marker marker, final String msg) {/*NOP*/}

    @Override
    public void info(final Marker marker, final String format, final Object arg) {/*NOP*/}

    @Override
    public void info(final Marker marker, final String format, final Object arg1,
        final Object arg2) {/*NOP*/}

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isWarnEnabled() {
      return true;
    }

    @Override
    public void warn(final String msg) {/*NOP*/}

    @Override
    public void warn(final String format, final Object arg) {/*NOP*/}

    @Override
    public void warn(final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {/*NOP*/}

    @Override
    public void warn(final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isWarnEnabled(final Marker marker) {
      return true;
    }

    @Override
    public void warn(final Marker marker, final String msg) {/*NOP*/}

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {/*NOP*/}

    @Override
    public void warn(final Marker marker, final String format, final Object arg1,
        final Object arg2) {/*NOP*/}

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isErrorEnabled() {
      return true;
    }

    @Override
    public void error(final String msg) {/*NOP*/}

    @Override
    public void error(final String format, final Object arg) {/*NOP*/}

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {/*NOP*/}

    @Override
    public void error(final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void error(final String msg, final Throwable t) {/*NOP*/}

    @Override
    public boolean isErrorEnabled(final Marker marker) {
      return true;
    }

    @Override
    public void error(final Marker marker, final String msg) {/*NOP*/}

    @Override
    public void error(final Marker marker, final String format, final Object arg) {/*NOP*/}

    @Override
    public void error(final Marker marker, final String format, final Object arg1,
        final Object arg2) {/*NOP*/}

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {/*NOP*/}

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {/*NOP*/}
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return _ -> DummyLogger.INSTANCE;
  }
}
