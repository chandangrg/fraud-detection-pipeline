package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.ReviewCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCaseRepository extends JpaRepository<ReviewCase, String> {
  List<ReviewCase> findTop100ByStatusOrderByCreatedAtAsc(String status);

  long countByStatus(String status);
}
