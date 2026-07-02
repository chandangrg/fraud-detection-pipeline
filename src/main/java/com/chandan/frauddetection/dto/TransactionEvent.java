package com.chandan.frauddetection.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
    String eventId,
    int schemaVersion,
    String producer,
    String transactionId,
    String accountId,
    BigDecimal amount,
    String currency,
    String status,
    Instant occurredAt,
    String correlationId) {}
