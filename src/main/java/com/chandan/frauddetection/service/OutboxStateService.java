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
  public void published(String id) {
    r.findById(id).orElseThrow().published();
  }

  @Transactional
  public void retry(String id, String error, int n) {
    r.findById(id)
        .orElseThrow()
        .retry(error, Instant.now().plusSeconds(Math.min(300, 1L << Math.min(n, 8))));
  }

  @Transactional
  public void dead(String id, String error) {
    r.findById(id).orElseThrow().dead(error);
  }

  @Transactional
  public void replay(String id) {
    r.findById(id).orElseThrow().replay();
  }
}
