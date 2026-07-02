package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

  @Id
  @Column(name = "event_id", length = 36)
  private String eventId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  protected ProcessedEvent() {}

  public ProcessedEvent(String id) {
    eventId = id;
    processedAt = Instant.now();
  }

  public String getEventId() {
    return eventId;
  }
}
