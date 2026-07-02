package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
  @Query(
      value =
          "select * from outbox_events where status in ('PENDING','RETRY','IN_FLIGHT') and"
              + " next_attempt_at<=:now and (lease_until is null or lease_until<:now) order by"
              + " created_at limit :limit for update skip locked",
      nativeQuery = true)
  List<OutboxEvent> lockNextBatch(@Param("now") Instant now, @Param("limit") int limit);

  List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

  long countByStatus(OutboxStatus status);
}
