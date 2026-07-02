package com.chandan.frauddetection.service;

import com.chandan.frauddetection.dto.*;
import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.exception.*;
import com.chandan.frauddetection.repository.TransactionRepository;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

  private final TransactionRepository repository;
  private final TransactionInsertService insert;
  private final AccountCacheService accounts;
  private final RequestFingerprintService fingerprints;

  public TransactionService(
      TransactionRepository repository,
      TransactionInsertService insert,
      AccountCacheService accounts,
      RequestFingerprintService fingerprints) {
    this.repository = repository;
    this.insert = insert;
    this.accounts = accounts;
    this.fingerprints = fingerprints;
  }

  public TransactionSubmissionResult submit(TransactionRequest r) {
    String fp = fingerprints.fingerprint(r);
    var existing = repository.findByIdempotencyKey(r.idempotencyKey());
    if (existing.isPresent())
      return new TransactionSubmissionResult(validate(existing.get(), fp), false);
    Account a = accounts.getAccount(r.accountId());
    if (r.amount().compareTo(a.getDailyLimit()) > 0)
      throw new LimitExceededException(r.accountId());
    TransactionStatus status =
        "HIGH".equalsIgnoreCase(a.getRiskTier())
            ? TransactionStatus.REVIEW_REQUIRED
            : TransactionStatus.AUTHORIZED;
    try {
      return new TransactionSubmissionResult(insert.insert(r, fp, status, correlation()), true);
    } catch (DataIntegrityViolationException race) {
      Transaction winner =
          repository.findByIdempotencyKey(r.idempotencyKey()).orElseThrow(() -> race);
      return new TransactionSubmissionResult(validate(winner, fp), false);
    }
  }

  public Transaction get(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
  }

  private Transaction validate(Transaction t, String fp) {
    if (!t.getRequestFingerprint().equals(fp))
      throw new IdempotencyConflictException(t.getIdempotencyKey());
    return t;
  }

  private String correlation() {
    String c = MDC.get("correlationId");
    return c == null ? "system" : c;
  }
}
