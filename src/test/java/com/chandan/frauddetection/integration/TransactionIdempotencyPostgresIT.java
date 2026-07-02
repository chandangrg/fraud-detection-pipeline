package com.chandan.frauddetection.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chandan.frauddetection.dto.TransactionRequest;
import com.chandan.frauddetection.dto.TransactionSubmissionResult;
import com.chandan.frauddetection.entity.Account;
import com.chandan.frauddetection.repository.OutboxEventRepository;
import com.chandan.frauddetection.service.AccountCacheService;
import com.chandan.frauddetection.service.TransactionService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    properties = {
      "app.scheduling.enabled=false",
      "spring.kafka.listener.auto-startup=false",
      "spring.kafka.admin.fail-fast=false",
    })
class TransactionIdempotencyPostgresIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired TransactionService transactions;

  @Autowired OutboxEventRepository outbox;

  @MockBean AccountCacheService accounts;

  @Test
  void concurrentRequestsWithTheSameKeyCreateOneTransactionAndOneOutboxEvent() throws Exception {
    when(accounts.getAccount("acct-race"))
        .thenReturn(new Account("acct-race", "Synthetic", "LOW", new BigDecimal("50000")));
    TransactionRequest request =
        new TransactionRequest("transaction-race-1", "acct-race", new BigDecimal("125.00"), "USD");

    ExecutorService pool = Executors.newFixedThreadPool(8);
    try {
      List<Future<TransactionSubmissionResult>> futures = new ArrayList<>();
      for (int i = 0; i < 8; i++) futures.add(pool.submit(() -> transactions.submit(request)));

      Set<String> ids = new HashSet<>();
      int created = 0;
      for (Future<TransactionSubmissionResult> future : futures) {
        TransactionSubmissionResult result = future.get(10, TimeUnit.SECONDS);
        ids.add(result.transaction().getId());
        if (result.created()) created++;
      }

      assertThat(ids).hasSize(1);
      assertThat(created).isEqualTo(1);
      assertThat(outbox.count()).isEqualTo(1);
    } finally {
      pool.shutdownNow();
    }
  }
}
