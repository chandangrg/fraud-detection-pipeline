package com.chandan.frauddetection.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * The main listener uses JSON deserialization. The DLQ listener intentionally consumes the value as
 * raw UTF-8 text so malformed JSON is still inspectable instead of failing deserialization a second
 * time on the DLQ.
 */
@Configuration
public class DlqKafkaListenerConfig {

  @Bean("dlqKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, String> dlqKafkaListenerContainerFactory(
      @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));

    // Persisting the inspection copy is the purpose of this consumer. If
    // PostgreSQL is temporarily unavailable, keep retrying the DLQ record
    // rather than routing it back to the same topic or silently skipping it.
    factory.setCommonErrorHandler(
        new DefaultErrorHandler(new FixedBackOff(1_000L, Long.MAX_VALUE)));
    return factory;
  }
}
