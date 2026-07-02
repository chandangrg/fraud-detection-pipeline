package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
  Optional<Transaction> findByIdempotencyKey(String key);
}
