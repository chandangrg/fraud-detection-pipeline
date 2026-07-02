package com.chandan.frauddetection.service;

import com.chandan.frauddetection.dto.*;
import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionInsertService {

  private final TransactionRepository transactions;
  private final OutboxEventRepository outbox;
  private final ObjectMapper mapper;

  public TransactionInsertService(
      TransactionRepository transactions, OutboxEventRepository outbox, ObjectMapper mapper) {
    this.transactions = transactions;
    this.outbox = outbox;
    this.mapper = mapper;
  }

  @Transactional
  public Transaction insert(
      TransactionRequest r, String fingerprint, TransactionStatus status, String correlation) {
    Transaction tx =
        transactions.saveAndFlush(
            new Transaction(
                r.idempotencyKey(),
                fingerprint,
                r.accountId(),
                r.amount(),
                r.currency().toUpperCase(),
                status,
                correlation));
    String eventId = UUID.randomUUID().toString();
    TransactionEvent event =
        new TransactionEvent(
            eventId,
            1,
            "fraud-detection-pipeline",
            tx.getId(),
            tx.getAccountId(),
            tx.getAmount(),
            tx.getCurrency(),
            tx.getStatus().name(),
            tx.getCreatedAt(),
            correlation);
    try {
      outbox.save(
          new OutboxEvent(
              eventId,
              tx.getId(),
              "TransactionAccepted",
              1,
              mapper.writeValueAsString(event),
              correlation));
    } catch (Exception e) {
      throw new IllegalStateException("Cannot serialize transaction event", e);
    }
    return tx;
  }
}
