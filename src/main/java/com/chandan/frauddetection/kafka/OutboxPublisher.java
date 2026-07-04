package com.chandan.frauddetection.kafka;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.entity.OutboxEvent;
import com.chandan.frauddetection.repository.OutboxEventRepository;
import com.chandan.frauddetection.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

  private final OutboxClaimService claims;
  private final OutboxEventRepository repo;
  private final OutboxStateService states;
  private final KafkaTemplate<String, TransactionEvent> kafka;
  private final ObjectMapper mapper;
  private final String topic;
  private final String worker = UUID.randomUUID().toString();
  private final int batch;
  private final long timeout;
  private final Duration lease;

  public OutboxPublisher(
      OutboxClaimService claims,
      OutboxEventRepository repo,
      OutboxStateService states,
      KafkaTemplate<String, TransactionEvent> kafka,
      ObjectMapper mapper,
      @Value("${app.kafka.topic.transaction-events}") String topic,
      @Value("${app.outbox.batch-size:50}") int batch,
      @Value("${app.outbox.send-timeout-ms:5000}") long timeout,
      @Value("${app.outbox.lease-seconds:30}") long leaseSeconds) {
    this.claims = claims;
    this.repo = repo;
    this.states = states;
    this.kafka = kafka;
    this.mapper = mapper;
    this.topic = topic;
    this.batch = batch;
    this.timeout = timeout;
    this.lease = Duration.ofSeconds(leaseSeconds);
  }

  @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
  public void run() {
    for (String id : claims.claim(worker, batch, lease)) publish(id);
  }

  void publish(String id) {
    OutboxEvent e = repo.findById(id).orElseThrow();
    try {
      TransactionEvent event = mapper.readValue(e.getPayload(), TransactionEvent.class);
      kafka.send(topic, event.accountId(), event).get(timeout, TimeUnit.MILLISECONDS);
      states.published(id, worker);
    } catch (com.fasterxml.jackson.core.JsonProcessingException poison) {
      states.dead(id, worker, poison.getMessage());
    } catch (Exception transientFailure) {
      states.retry(id, worker, transientFailure.getMessage(), e.getPublishAttempts() + 1);
    }
  }
}
