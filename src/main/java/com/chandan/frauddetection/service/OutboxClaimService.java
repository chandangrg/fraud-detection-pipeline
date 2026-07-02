package com.chandan.frauddetection.service;

import com.chandan.frauddetection.entity.OutboxEvent;
import com.chandan.frauddetection.repository.OutboxEventRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxClaimService {

  private final OutboxEventRepository r;

  public OutboxClaimService(OutboxEventRepository r) {
    this.r = r;
  }

  @Transactional
  public List<String> claim(String worker, int size, Duration lease) {
    List<OutboxEvent> rows = r.lockNextBatch(Instant.now(), size);
    Instant until = Instant.now().plus(lease);
    rows.forEach(x -> x.claim(worker, until));
    return rows.stream().map(OutboxEvent::getId).toList();
  }
}
