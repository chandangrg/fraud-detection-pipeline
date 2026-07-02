package com.chandan.frauddetection.metrics;

import com.chandan.frauddetection.entity.DlqStatus;
import com.chandan.frauddetection.entity.OutboxStatus;
import com.chandan.frauddetection.repository.DlqRecordRepository;
import com.chandan.frauddetection.repository.OutboxEventRepository;
import com.chandan.frauddetection.repository.ReviewCaseRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class OperationalMetrics implements MeterBinder {

  private final OutboxEventRepository outbox;
  private final DlqRecordRepository dlq;
  private final ReviewCaseRepository reviews;

  public OperationalMetrics(
      OutboxEventRepository outbox, DlqRecordRepository dlq, ReviewCaseRepository reviews) {
    this.outbox = outbox;
    this.dlq = dlq;
    this.reviews = reviews;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder("fraud_outbox_backlog", this, metrics -> metrics.outboxBacklog())
        .description("Outbox rows still requiring publication")
        .register(registry);
    Gauge.builder("fraud_dlq_open", dlq, repository -> repository.countByStatus(DlqStatus.OPEN))
        .description("DLQ records awaiting operator action")
        .register(registry);
    Gauge.builder(
            "fraud_review_cases_open", reviews, repository -> repository.countByStatus("OPEN"))
        .description("Open fraud review cases")
        .register(registry);
  }

  private double outboxBacklog() {
    return (outbox.countByStatus(OutboxStatus.PENDING)
        + outbox.countByStatus(OutboxStatus.RETRY)
        + outbox.countByStatus(OutboxStatus.IN_FLIGHT));
  }
}
