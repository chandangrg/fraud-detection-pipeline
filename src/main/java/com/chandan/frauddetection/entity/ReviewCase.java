package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "review_cases")
public class ReviewCase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "transaction_id", nullable = false, length = 80)
  private String transactionId;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(nullable = false, length = 1000)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ReviewCase() {}

  public ReviewCase(String tx, String reason) {
    transactionId = tx;
    this.reason = reason;
    status = "OPEN";
    createdAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getStatus() {
    return status;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
