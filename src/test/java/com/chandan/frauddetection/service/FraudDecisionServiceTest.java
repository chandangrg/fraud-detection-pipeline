package com.chandan.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudDecisionServiceTest {

  @Mock ProcessedEventRepository processed;

  @Mock FraudDecisionRepository decisions;

  @Mock ReviewCaseRepository reviews;

  FraudDecisionService service;

  @BeforeEach
  void init() {
    service = new FraudDecisionService(processed, decisions, reviews);
  }

  @Test
  void duplicateEventIsNoOp() {
    when(processed.claim("e1")).thenReturn(0);
    assertThat(service.process(event("e1", new BigDecimal("100"), "AUTHORIZED"))).isFalse();
    verifyNoInteractions(decisions, reviews);
  }

  @Test
  void highAmountAndReviewStatusCreatesReviewCase() {
    when(processed.claim("e2")).thenReturn(1);
    assertThat(service.process(event("e2", new BigDecimal("15000"), "REVIEW_REQUIRED"))).isTrue();
    verify(decisions).save(argThat(d -> d.getOutcome() == FraudOutcome.BLOCK));
    verify(reviews).save(any());
  }

  private TransactionEvent event(String id, BigDecimal amount, String status) {
    return new TransactionEvent(
        id, 1, "test", "tx1", "acct", amount, "USD", status, Instant.now(), "corr");
  }
}
