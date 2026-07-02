package com.chandan.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionEventContractTest {

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void eventRoundTripsWithoutLosingEnvelopeFields() throws Exception {
    TransactionEvent original =
        new TransactionEvent(
            "event-1",
            1,
            "fraud-detection-pipeline",
            "transaction-1",
            "acct-1",
            new BigDecimal("50.00"),
            "USD",
            "AUTHORIZED",
            Instant.parse("2026-01-01T00:00:00Z"),
            "corr-1");

    TransactionEvent restored =
        mapper.readValue(mapper.writeValueAsBytes(original), TransactionEvent.class);

    assertThat(restored).isEqualTo(original);
    assertThat(restored.schemaVersion()).isEqualTo(1);
    assertThat(restored.eventId()).isEqualTo("event-1");
  }
}
