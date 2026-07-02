package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "transactions",
    indexes = {
      @Index(name = "ux_tx_idempotency", columnList = "idempotency_key", unique = true),
      @Index(name = "idx_tx_account", columnList = "account_id"),
    })
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
  private String idempotencyKey;

  @Column(name = "request_fingerprint", nullable = false, length = 64)
  private String requestFingerprint;

  @Column(name = "account_id", nullable = false, length = 80)
  private String accountId;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TransactionStatus status;

  @Column(name = "correlation_id", nullable = false, length = 120)
  private String correlationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Transaction() {}

  public Transaction(
      String key,
      String fingerprint,
      String account,
      BigDecimal amount,
      String currency,
      TransactionStatus status,
      String correlation) {
    this.idempotencyKey = key;
    this.requestFingerprint = fingerprint;
    this.accountId = account;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.correlationId = correlation;
    this.createdAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getRequestFingerprint() {
    return requestFingerprint;
  }

  public String getAccountId() {
    return accountId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public TransactionStatus getStatus() {
    return status;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
