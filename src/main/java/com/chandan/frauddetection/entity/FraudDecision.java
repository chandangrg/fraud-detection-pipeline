package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "fraud_decisions",
    indexes = @Index(name = "ux_fraud_tx", columnList = "transaction_id", unique = true))
public class FraudDecision {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "transaction_id", nullable = false, unique = true, length = 80)
  private String transactionId;

  @Column(name = "event_id", nullable = false, length = 36)
  private String eventId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FraudOutcome outcome;

  @Column(name = "risk_score", nullable = false)
  private int riskScore;

  @Column(nullable = false, length = 1000)
  private String reasons;

  @Column(name = "correlation_id", nullable = false, length = 120)
  private String correlationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FraudDecision() {}

  public FraudDecision(
      String tx,
      String event,
      FraudOutcome outcome,
      int score,
      String reasons,
      String correlation) {
    this.transactionId = tx;
    this.eventId = event;
    this.outcome = outcome;
    this.riskScore = score;
    this.reasons = reasons;
    this.correlationId = correlation;
    this.createdAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getEventId() {
    return eventId;
  }

  public FraudOutcome getOutcome() {
    return outcome;
  }

  public int getRiskScore() {
    return riskScore;
  }

  public String getReasons() {
    return reasons;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
