package io.nuevedejun.gadantic;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {
  // TODO figure how to register shutdown hook correctly
  public static void main(final String[] args) {
    Quarkus.run(Gadantic.class, (code, throwable) -> {
      if (throwable != null) {
        Log.error("Uncaught exception", throwable);
      }
      Gadantic.shutdown();
      System.exit(code);
    }, args);
  }
}
