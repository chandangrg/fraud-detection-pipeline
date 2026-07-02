package com.chandan.frauddetection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Value("${app.kafka.topic.transaction-events}")
  private String transactionEventsTopic;

  @Value("${app.kafka.topic.transaction-events-dlq}")
  private String dlqTopic;

  @Bean
  public NewTopic transactionEventsTopic() {
    // Partitioned by accountId (set as the Kafka message key) so all events
    // for a given account preserve order across partitions.
    return TopicBuilder.name(transactionEventsTopic).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic transactionEventsDlqTopic() {
    return TopicBuilder.name(dlqTopic).partitions(3).replicas(1).build();
  }
}
