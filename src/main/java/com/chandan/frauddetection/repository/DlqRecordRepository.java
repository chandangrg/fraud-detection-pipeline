package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DlqRecordRepository extends JpaRepository<DlqRecord, String> {
  List<DlqRecord> findTop100ByStatusOrderByReceivedAtAsc(DlqStatus status);

  long countByStatus(DlqStatus status);
}
