package com.chandan.frauddetection.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.chandan.frauddetection.entity.OutboxEvent;
import com.chandan.frauddetection.repository.OutboxEventRepository;
import com.chandan.frauddetection.service.OutboxClaimService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class OutboxLeasePostgresIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired OutboxEventRepository repository;

  @Autowired OutboxClaimService claims;

  @Test
  void concurrentWorkersReceiveDisjointLeases() throws Exception {
    List<OutboxEvent> events = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      events.add(
          new OutboxEvent(
              "event-" + i, "transaction-" + i, "TransactionAccepted", 1, "{}", "corr-" + i));
    }
    repository.saveAllAndFlush(events);

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<List<String>> first =
          pool.submit(
              () -> {
                start.await(5, TimeUnit.SECONDS);
                return claims.claim("worker-a", 5, Duration.ofSeconds(30));
              });
      Future<List<String>> second =
          pool.submit(
              () -> {
                start.await(5, TimeUnit.SECONDS);
                return claims.claim("worker-b", 5, Duration.ofSeconds(30));
              });
      start.countDown();

      List<String> a = first.get(10, TimeUnit.SECONDS);
      List<String> b = second.get(10, TimeUnit.SECONDS);
      Set<String> union = new HashSet<>(a);
      union.addAll(b);

      assertThat(a).hasSize(5);
      assertThat(b).hasSize(5);
      assertThat(a).doesNotContainAnyElementsOf(b);
      assertThat(union).hasSize(10);
    } finally {
      pool.shutdownNow();
    }
  }
}
