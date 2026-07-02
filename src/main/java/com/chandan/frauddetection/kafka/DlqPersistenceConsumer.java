package com.chandan.frauddetection.kafka;

import com.chandan.frauddetection.entity.DlqRecord;
import com.chandan.frauddetection.repository.DlqRecordRepository;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DlqPersistenceConsumer {

  private static final String EXCEPTION_MESSAGE_HEADER = "kafka_dlt-exception-message";

  private final DlqRecordRepository repository;

  public DlqPersistenceConsumer(DlqRecordRepository repository) {
    this.repository = repository;
  }

  @KafkaListener(
      topics = "${app.kafka.topic.transaction-events-dlq}",
      groupId = "fraud-dlq-inspector-v2",
      containerFactory = "dlqKafkaListenerContainerFactory")
  public void capture(ConsumerRecord<String, String> record) {
    DlqRecord dlq =
        new DlqRecord(
            record.key(),
            record.value() == null ? "" : record.value(),
            header(record, EXCEPTION_MESSAGE_HEADER),
            record.topic(),
            record.partition(),
            record.offset());

    // A deterministic ID based on topic/partition/offset makes redelivery
    // idempotent if the process crashes after the insert but before the
    // Kafka offset is committed.
    if (!repository.existsById(dlq.getId())) {
      repository.saveAndFlush(dlq);
    }
  }

  private String header(ConsumerRecord<?, ?> record, String name) {
    Header header = record.headers().lastHeader(name);
    return header == null ? "consumer failure" : new String(header.value(), StandardCharsets.UTF_8);
  }
}
