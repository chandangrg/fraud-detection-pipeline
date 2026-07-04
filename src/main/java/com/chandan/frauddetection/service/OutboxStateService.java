package com.chandan.frauddetection.service;

import com.chandan.frauddetection.repository.OutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxStateService {

  private final OutboxEventRepository r;

  public OutboxStateService(OutboxEventRepository r) {
    this.r = r;
  }

  @Transactional
  public boolean published(String id, String worker) {
    return r.markPublishedIfOwned(id, worker, Instant.now()) == 1;
  }

  @Transactional
  public boolean retry(String id, String worker, String error, int n) {
    Instant nextAttemptAt = Instant.now().plusSeconds(Math.min(300, 1L << Math.min(n, 8)));
    return r.markRetryIfOwned(id, worker, clip(error), nextAttemptAt) == 1;
  }

  @Transactional
  public boolean dead(String id, String worker, String error) {
    return r.markDeadIfOwned(id, worker, clip(error)) == 1;
  }

  @Transactional
  public void replay(String id) {
    r.findById(id).orElseThrow().replay();
  }

  private String clip(String s) {
    return s == null ? null : s.substring(0, Math.min(s.length(), 1000));
  }
}
