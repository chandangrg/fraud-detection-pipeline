package com.chandan.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "dlq_records",
    indexes = {
      @Index(
          name = "ux_dlq_position",
          columnList = "dlq_topic,dlq_partition,dlq_offset",
          unique = true),
      @Index(name = "idx_dlq_status_received", columnList = "status,received_at"),
    })
public class DlqRecord {

  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "event_key", length = 120)
  private String eventKey;

  @Column(nullable = false, columnDefinition = "text")
  private String payload;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "dlq_topic", nullable = false, length = 200)
  private String dlqTopic;

  @Column(name = "dlq_partition", nullable = false)
  private int dlqPartition;

  @Column(name = "dlq_offset", nullable = false)
  private long dlqOffset;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DlqStatus status;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "replayed_at")
  private Instant replayedAt;

  protected DlqRecord() {}

  public DlqRecord(
      String key, String payload, String error, String topic, int partition, long offset) {
    this.id = deterministicId(topic, partition, offset);
    this.eventKey = key;
    this.payload = payload;
    this.errorMessage = clip(error);
    this.dlqTopic = topic;
    this.dlqPartition = partition;
    this.dlqOffset = offset;
    this.status = DlqStatus.OPEN;
    this.receivedAt = Instant.now();
  }

  public void replayed() {
    if (status != DlqStatus.OPEN) {
      throw new IllegalStateException("DLQ record is not open");
    }
    status = DlqStatus.REPLAYED;
    replayedAt = Instant.now();
  }

  private static String deterministicId(String topic, int partition, long offset) {
    String position = topic + "|" + partition + "|" + offset;
    return UUID.nameUUIDFromBytes(position.getBytes(StandardCharsets.UTF_8)).toString();
  }

  private String clip(String value) {
    return value == null ? null : value.substring(0, Math.min(1000, value.length()));
  }

  public String getId() {
    return id;
  }

  public String getEventKey() {
    return eventKey;
  }

  public String getPayload() {
    return payload;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getDlqTopic() {
    return dlqTopic;
  }

  public int getDlqPartition() {
    return dlqPartition;
  }

  public long getDlqOffset() {
    return dlqOffset;
  }

  public DlqStatus getStatus() {
    return status;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public Instant getReplayedAt() {
    return replayedAt;
  }
}
