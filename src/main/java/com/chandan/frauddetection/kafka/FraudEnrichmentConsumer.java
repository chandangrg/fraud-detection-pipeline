package com.chandan.frauddetection.kafka;

import com.chandan.frauddetection.dto.TransactionEvent;
import com.chandan.frauddetection.service.FraudDecisionService;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudEnrichmentConsumer {

  private static final Logger log = LoggerFactory.getLogger(FraudEnrichmentConsumer.class);
  private final FraudDecisionService service;

  public FraudEnrichmentConsumer(FraudDecisionService service) {
    this.service = service;
  }

  @KafkaListener(topics = "${app.kafka.topic.transaction-events}", groupId = "fraud-enrichment-v2")
  public void onEvent(TransactionEvent event) {
    MDC.put("correlationId", event.correlationId());
    try {
      boolean created = service.process(event);
      log.info(
          "fraud event eventId={} transactionId={} processed={}",
          event.eventId(),
          event.transactionId(),
          created);
    } finally {
      MDC.remove("correlationId");
    }
  }
}
