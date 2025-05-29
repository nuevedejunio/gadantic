package io.nuevedejun.gadantic;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * TODO
 * This is a placeholder class to generate Quarkus jacoco reports. Otherwise, it for some dumb reason does not work. Fucking Quarkus, fucking shit.
 * This class can be deleted once I create the actual service with actual test.
 */
@io.quarkus.test.junit.QuarkusTest
class QuarkusTest {
  @Test
  void testNotFound() {
    given()
        .when().get("/hello")
        .then()
        .statusCode(404);
  }
}
