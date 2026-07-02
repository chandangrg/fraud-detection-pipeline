package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
  @Modifying
  @Query(
      value =
          "insert into processed_events(event_id,processed_at) values (:eventId,current_timestamp)"
              + " on conflict do nothing",
      nativeQuery = true)
  int claim(@Param("eventId") String eventId);
}
