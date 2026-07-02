package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "outbox_events",
    indexes =
        @Index(name = "idx_fraud_outbox_due", columnList = "status,next_attempt_at,lease_until"))
public class OutboxEvent {

  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "aggregate_id", nullable = false, length = 80)
  private String aggregateId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "schema_version", nullable = false)
  private int schemaVersion;

  @Column(nullable = false, columnDefinition = "text")
  private String payload;

  @Column(name = "correlation_id", nullable = false, length = 120)
  private String correlationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private OutboxStatus status;

  @Column(name = "publish_attempts", nullable = false)
  private int publishAttempts;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "lease_owner", length = 120)
  private String leaseOwner;

  @Column(name = "lease_until")
  private Instant leaseUntil;

  @Column(name = "last_error", length = 1000)
  private String lastError;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Version private long version;

  protected OutboxEvent() {}

  public OutboxEvent(
      String id,
      String aggregateId,
      String eventType,
      int schemaVersion,
      String payload,
      String correlationId) {
    this.id = id;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.schemaVersion = schemaVersion;
    this.payload = payload;
    this.correlationId = correlationId;
    this.status = OutboxStatus.PENDING;
    this.createdAt = Instant.now();
    this.nextAttemptAt = createdAt;
  }

  public void claim(String owner, Instant until) {
    status = OutboxStatus.IN_FLIGHT;
    leaseOwner = owner;
    leaseUntil = until;
  }

  public void published() {
    status = OutboxStatus.PUBLISHED;
    publishedAt = Instant.now();
    leaseOwner = null;
    leaseUntil = null;
    lastError = null;
  }

  public void retry(String error, Instant at) {
    status = OutboxStatus.RETRY;
    publishAttempts++;
    nextAttemptAt = at;
    lastError = clip(error);
    leaseOwner = null;
    leaseUntil = null;
  }

  public void dead(String error) {
    status = OutboxStatus.DEAD_LETTER;
    publishAttempts++;
    lastError = clip(error);
    leaseOwner = null;
    leaseUntil = null;
  }

  public void replay() {
    status = OutboxStatus.RETRY;
    nextAttemptAt = Instant.now();
    lastError = null;
    leaseOwner = null;
    leaseUntil = null;
  }

  private String clip(String s) {
    return s == null ? null : s.substring(0, Math.min(s.length(), 1000));
  }

  public String getId() {
    return id;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public String getEventType() {
    return eventType;
  }

  public int getSchemaVersion() {
    return schemaVersion;
  }

  public String getPayload() {
    return payload;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public OutboxStatus getStatus() {
    return status;
  }

  public int getPublishAttempts() {
    return publishAttempts;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public Instant getLeaseUntil() {
    return leaseUntil;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
