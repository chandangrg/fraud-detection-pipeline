package com.chandan.frauddetection.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.repository.FraudDecisionRepository;
import com.chandan.frauddetection.service.FraudDecisionService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    properties = {"app.scheduling.enabled=false", "spring.kafka.listener.auto-startup=false"})
class DurableIdempotencyPostgresIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired FraudDecisionService service;

  @Autowired FraudDecisionRepository decisions;

  @Test
  void replayCreatesOnlyOneDecision() {
    TransactionEvent e =
        new TransactionEvent(
            "event-1",
            1,
            "test",
            "tx-1",
            "acct",
            new BigDecimal("25"),
            "USD",
            "AUTHORIZED",
            Instant.now(),
            "corr");
    assertThat(service.process(e)).isTrue();
    assertThat(service.process(e)).isFalse();
    assertThat(decisions.findByTransactionId("tx-1")).isPresent();
  }
}
