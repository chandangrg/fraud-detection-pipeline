package com.chandan.frauddetection.service;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.repository.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudDecisionService {

  private final ProcessedEventRepository processed;
  private final FraudDecisionRepository decisions;
  private final ReviewCaseRepository reviews;

  public FraudDecisionService(
      ProcessedEventRepository processed,
      FraudDecisionRepository decisions,
      ReviewCaseRepository reviews) {
    this.processed = processed;
    this.decisions = decisions;
    this.reviews = reviews;
  }

  @Transactional
  public boolean process(TransactionEvent event) {
    if (processed.claim(event.eventId()) == 0) return false;
    int score = score(event);
    FraudOutcome outcome =
        score >= 80 ? FraudOutcome.BLOCK : score >= 50 ? FraudOutcome.REVIEW : FraudOutcome.APPROVE;
    String reasons = reasons(event, score);
    decisions.save(
        new FraudDecision(
            event.transactionId(),
            event.eventId(),
            outcome,
            score,
            reasons,
            event.correlationId()));
    if (outcome != FraudOutcome.APPROVE)
      reviews.save(new ReviewCase(event.transactionId(), reasons));
    return true;
  }

  private int score(TransactionEvent e) {
    int score = 0;
    if (e.amount() != null && e.amount().compareTo(new BigDecimal("10000")) > 0) score += 60;
    if ("REVIEW_REQUIRED".equals(e.status())) score += 30;
    return Math.min(score, 100);
  }

  private String reasons(TransactionEvent e, int score) {
    return "amount=" + e.amount() + ", upstreamStatus=" + e.status() + ", score=" + score;
  }
}
