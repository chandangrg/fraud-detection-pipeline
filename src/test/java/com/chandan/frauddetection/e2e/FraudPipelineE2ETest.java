package com.chandan.frauddetection.e2e;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "e2e.enabled", matches = "true")
class FraudPipelineE2ETest {

  private static final String BASE_URL =
      System.getProperty("e2e.base-url", "http://localhost:8081");

  private static final String ADMIN_API_KEY =
      System.getProperty("e2e.admin-api-key", "local-admin-key");

  @BeforeAll
  static void configureRestAssured() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  void healthEndpointReportsUp() {
    request()
        .when()
        .get("/actuator/health")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("status", equalTo("UP"));
  }

  @Test
  void lowRiskTransactionIsIdempotentAndProducesApproveDecision() {

    String idempotencyKey = "e2e-low-" + UUID.randomUUID();
    String correlationId = "e2e-correlation-" + UUID.randomUUID();

    Map<String, Object> body =
        Map.of(
            "idempotencyKey",
            idempotencyKey,
            "accountId",
            "acct-low",
            "amount",
            new BigDecimal("500.00"),
            "currency",
            "USD");

    Response created =
        request()
            .header("X-Correlation-Id", correlationId)
            .body(body)
            .when()
            .post("/api/v1/transactions");

    created
        .then()
        .statusCode(201)
        .header("X-Correlation-Id", equalTo(correlationId))
        .body("transactionId", notNullValue())
        .body("status", equalTo("AUTHORIZED"))
        .body("accountId", equalTo("acct-low"))
        .body("correlationId", equalTo(correlationId));

    String transactionId = created.jsonPath().getString("transactionId");

    request()
        .header("X-Correlation-Id", correlationId)
        .body(body)
        .when()
        .post("/api/v1/transactions")
        .then()
        .statusCode(200)
        .body("transactionId", equalTo(transactionId))
        .body("status", equalTo("AUTHORIZED"));

    Map<String, Object> conflictingBody = new HashMap<>(body);
    conflictingBody.put("amount", new BigDecimal("501.00"));

    request()
        .header("X-Correlation-Id", correlationId)
        .body(conflictingBody)
        .when()
        .post("/api/v1/transactions")
        .then()
        .statusCode(409)
        .body("status", equalTo(409))
        .body("message", containsString(idempotencyKey))
        .body("correlationId", equalTo(correlationId));

    awaitDecision(transactionId)
        .then()
        .statusCode(200)
        .body("transactionId", equalTo(transactionId))
        .body("outcome", equalTo("APPROVE"))
        .body("riskScore", equalTo(0))
        .body("correlationId", equalTo(correlationId));
  }

  @Test
  void highRiskTransactionProducesBlockedDecisionAndReviewCase() {

    String idempotencyKey = "e2e-high-" + UUID.randomUUID();
    String correlationId = "e2e-correlation-" + UUID.randomUUID();

    Map<String, Object> body =
        Map.of(
            "idempotencyKey",
            idempotencyKey,
            "accountId",
            "acct-high",
            "amount",
            new BigDecimal("12000.00"),
            "currency",
            "USD");

    Response created =
        request()
            .header("X-Correlation-Id", correlationId)
            .body(body)
            .when()
            .post("/api/v1/transactions");

    created
        .then()
        .statusCode(201)
        .body("transactionId", notNullValue())
        .body("status", equalTo("REVIEW_REQUIRED"))
        .body("accountId", equalTo("acct-high"));

    String transactionId = created.jsonPath().getString("transactionId");

    awaitDecision(transactionId)
        .then()
        .statusCode(200)
        .body("transactionId", equalTo(transactionId))
        .body("outcome", equalTo("BLOCK"))
        .body("riskScore", equalTo(90))
        .body("reasons", containsString("score=90"))
        .body("correlationId", equalTo(correlationId));

    request()
        .when()
        .get("/api/v1/fraud/reviews")
        .then()
        .statusCode(200)
        .body("transactionId", hasItem(transactionId));
  }

  @Test
  void invalidTransactionReturnsBadRequest() {
    String correlationId = "e2e-invalid-" + UUID.randomUUID();

    Map<String, Object> invalidBody =
        Map.of(
            "idempotencyKey",
            "e2e-invalid-" + UUID.randomUUID(),
            "accountId",
            "acct-low",
            "amount",
            BigDecimal.ZERO,
            "currency",
            "usd");

    request()
        .header("X-Correlation-Id", correlationId)
        .body(invalidBody)
        .when()
        .post("/api/v1/transactions")
        .then()
        .statusCode(400)
        .header("X-Correlation-Id", equalTo(correlationId))
        .body("status", equalTo(400))
        .body("correlationId", equalTo(correlationId));
  }

  @Test
  void adminEndpointRequiresApiKey() {
    request().when().get("/api/v1/admin/outbox").then().statusCode(401);

    request()
        .header("X-Admin-Api-Key", ADMIN_API_KEY)
        .when()
        .get("/api/v1/admin/outbox")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  private static RequestSpecification request() {
    return RestAssured.given()
        .baseUri(BASE_URL)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON);
  }

  private static Response awaitDecision(String transactionId) {
    AtomicReference<Response> latestResponse = new AtomicReference<>();

    await()
        .atMost(Duration.ofSeconds(20))
        .pollInterval(Duration.ofMillis(500))
        .until(
            () -> {
              Response response =
                  request().when().get("/api/v1/fraud/decisions/{transactionId}", transactionId);

              latestResponse.set(response);

              if (response.statusCode() != 200 && response.statusCode() != 404) {
                throw new AssertionError(
                    "Unexpected decision response: "
                        + response.statusCode()
                        + " "
                        + response.asString());
              }

              return response.statusCode() == 200;
            });

    return latestResponse.get();
  }
}
