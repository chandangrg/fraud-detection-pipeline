package com.chandan.frauddetection.controller;

import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.exception.FraudDecisionNotFoundException;
import com.chandan.frauddetection.repository.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudDecisionController {

  private final FraudDecisionRepository decisions;
  private final ReviewCaseRepository reviews;

  public FraudDecisionController(FraudDecisionRepository decisions, ReviewCaseRepository reviews) {
    this.decisions = decisions;
    this.reviews = reviews;
  }

  @GetMapping("/decisions/{transactionId}")
  public FraudDecision decision(@PathVariable String transactionId) {
    return decisions
        .findByTransactionId(transactionId)
        .orElseThrow(() -> new FraudDecisionNotFoundException(transactionId));
  }

  @GetMapping("/reviews")
  public List<ReviewCase> reviews() {
    return reviews.findTop100ByStatusOrderByCreatedAtAsc("OPEN");
  }
}
