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

  @Modifying
  @Query(
      value =
          "update outbox_events set status='PUBLISHED', published_at=:now, lease_owner=null,"
              + " lease_until=null, last_error=null, version=version+1 where id=:id and"
              + " lease_owner=:worker and status='IN_FLIGHT'",
      nativeQuery = true)
  int markPublishedIfOwned(
      @Param("id") String id, @Param("worker") String worker, @Param("now") Instant now);

  @Modifying
  @Query(
      value =
          "update outbox_events set status='RETRY', publish_attempts=publish_attempts+1,"
              + " next_attempt_at=:nextAttemptAt, last_error=:error, lease_owner=null,"
              + " lease_until=null, version=version+1 where id=:id and lease_owner=:worker"
              + " and status='IN_FLIGHT'",
      nativeQuery = true)
  int markRetryIfOwned(
      @Param("id") String id,
      @Param("worker") String worker,
      @Param("error") String error,
      @Param("nextAttemptAt") Instant nextAttemptAt);

  @Modifying
  @Query(
      value =
          "update outbox_events set status='DEAD_LETTER', publish_attempts=publish_attempts+1,"
              + " last_error=:error, lease_owner=null, lease_until=null, version=version+1"
              + " where id=:id and lease_owner=:worker and status='IN_FLIGHT'",
      nativeQuery = true)
  int markDeadIfOwned(
      @Param("id") String id, @Param("worker") String worker, @Param("error") String error);
}
