package com.chandan.frauddetection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Retry policy for consumer failures: 3 retries with a 1s backoff, then the message is routed to
 * the DLQ topic instead of blocking the partition forever or silently dropping the record. This is
 * what "dead-letter handling" means in practice, not just a topic name in an architecture diagram.
 */
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public DefaultErrorHandler errorHandler(
      KafkaOperations<Object, Object> kafkaOperations,
      @Value("${app.kafka.topic.transaction-events-dlq}") String dlqTopic) {
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaOperations,
            (record, ex) ->
                new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition()));

    // 3 retries, 1 second apart, before giving up and routing to DLQ
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    handler.addNotRetryableExceptions(IllegalArgumentException.class);
    return handler;
  }
}
