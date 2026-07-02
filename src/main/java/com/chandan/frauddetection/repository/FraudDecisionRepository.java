package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.FraudDecision;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudDecisionRepository extends JpaRepository<FraudDecision, String> {
  Optional<FraudDecision> findByTransactionId(String transactionId);
}
