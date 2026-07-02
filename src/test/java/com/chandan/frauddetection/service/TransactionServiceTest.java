package com.chandan.frauddetection.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.chandan.frauddetection.dto.*;
import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.exception.IdempotencyConflictException;
import com.chandan.frauddetection.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock TransactionRepository repo;

  @Mock TransactionInsertService insert;

  @Mock AccountCacheService accounts;

  RequestFingerprintService fp = new RequestFingerprintService();
  TransactionService service;

  @BeforeEach
  void init() {
    service = new TransactionService(repo, insert, accounts, fp);
  }

  @Test
  void sameReplayReturnsOriginal() {
    TransactionRequest r = req("k", "10");
    Transaction t =
        new Transaction(
            "k",
            fp.fingerprint(r),
            "acct",
            new BigDecimal("10"),
            "USD",
            TransactionStatus.AUTHORIZED,
            "c");
    when(repo.findByIdempotencyKey("k")).thenReturn(Optional.of(t));
    assertThat(service.submit(r).created()).isFalse();
  }

  @Test
  void changedPayloadConflicts() {
    TransactionRequest r = req("k", "10");
    Transaction t =
        new Transaction(
            "k",
            fp.fingerprint(r),
            "acct",
            new BigDecimal("10"),
            "USD",
            TransactionStatus.AUTHORIZED,
            "c");
    when(repo.findByIdempotencyKey("k")).thenReturn(Optional.of(t));
    assertThatThrownBy(() -> service.submit(req("k", "11")))
        .isInstanceOf(IdempotencyConflictException.class);
  }

  private TransactionRequest req(String k, String amount) {
    return new TransactionRequest(k, "acct", new BigDecimal(amount), "USD");
  }
}
