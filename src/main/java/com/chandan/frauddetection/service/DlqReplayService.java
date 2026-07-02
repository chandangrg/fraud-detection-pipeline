package com.chandan.frauddetection.service;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.entity.DlqRecord;
import com.chandan.frauddetection.entity.DlqStatus;
import com.chandan.frauddetection.repository.DlqRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DlqReplayService {

  private final DlqRecordRepository repository;
  private final KafkaTemplate<String, TransactionEvent> kafka;
  private final ObjectMapper mapper;
  private final String topic;

  public DlqReplayService(
      DlqRecordRepository repository,
      KafkaTemplate<String, TransactionEvent> kafka,
      ObjectMapper mapper,
      @Value("${app.kafka.topic.transaction-events}") String topic) {
    this.repository = repository;
    this.kafka = kafka;
    this.mapper = mapper;
    this.topic = topic;
  }

  @Transactional
  public void replay(String id) {
    DlqRecord record = repository.findById(id).orElseThrow();
    if (record.getStatus() != DlqStatus.OPEN) {
      throw new IllegalStateException("Only OPEN DLQ records can be replayed");
    }
    try {
      TransactionEvent event = mapper.readValue(record.getPayload(), TransactionEvent.class);
      kafka.send(topic, event.accountId(), event).get(5, TimeUnit.SECONDS);
      record.replayed();
    } catch (Exception failure) {
      throw new IllegalStateException("Replay failed; record remains OPEN for inspection", failure);
    }
  }
}
