package com.chandan.frauddetection.dto;

import com.chandan.frauddetection.entity.Transaction;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    String transactionId,
    String status,
    String accountId,
    BigDecimal amount,
    String currency,
    Instant createdAt,
    String correlationId) {
  public static TransactionResponse from(Transaction t) {
    return new TransactionResponse(
        t.getId(),
        t.getStatus().name(),
        t.getAccountId(),
        t.getAmount(),
        t.getCurrency(),
        t.getCreatedAt(),
        t.getCorrelationId());
  }
}
