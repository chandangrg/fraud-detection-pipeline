# Example SLOs and alert rules

These are example design targets for a production-like deployment. They have not been validated in a production environment.

| Signal | Example target | Example alert |
|---|---:|---|
| Transaction intake availability | 99.9% successful non-4xx requests | 5xx rate > 1% for 5 minutes |
| Intake latency | p95 < 300 ms | p95 > 500 ms for 10 minutes |
| Outbox publication age | 99% < 60 s | oldest unpublished event > 120 s |
| Fraud decision age | 99% < 30 s after Kafka publication | consumer lag or oldest undecided event > 60 s |
| DLQ volume | zero under normal synthetic traffic | any new OPEN record; page on sustained growth |

## Exposed metrics

- `fraud_outbox_backlog`
- `fraud_dlq_open`
- `fraud_review_cases_open`
- standard Kafka consumer, HTTP, JVM, Hikari and Redis metrics under `/actuator/prometheus`

## Required dashboard panels

API latency/error rate, outbox age/count, Kafka consumer lag, processing failures, open DLQ records, open review cases, Redis errors,
database-pool saturation, and JVM memory/GC.
