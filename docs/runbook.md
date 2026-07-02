# Operational runbook

## Transaction accepted but no fraud decision

1. Search by transaction ID, event ID, and `X-Correlation-Id`.
2. Check `fraud_outbox_backlog`, Kafka consumer lag, and `fraud_review_cases_open`.
3. If the event is still in the outbox, restore Kafka and allow leased publishers to retry.
4. If a consumer failed repeatedly, inspect `/api/v1/admin/dlq` with the admin API key.
5. Correct the underlying cause before replay. The durable `processed_events` claim makes a repeated valid event a no-op.

## Redis outage

Redis is not a correctness store. Confirm PostgreSQL is healthy, then expect account reads to use database fallback. Watch database-pool saturation and latency; restore Redis before fallback load becomes sustained.

## DLQ persistence or replay

The DLQ listener consumes raw text, so malformed JSON remains inspectable. DLQ records use a deterministic ID derived from topic/partition/offset, preventing duplicate inspection rows after redelivery. Only `OPEN` rows can be replayed. A malformed payload remains `OPEN` when replay parsing fails.

## Safe rollback

Roll back the image first. Preserve additive event compatibility and do not purge `processed_events`, outbox, decisions, or DLQ records during an incident.

## Evidence discipline

Use `performance/transaction-smoke.js`, Kafka lag, and Prometheus exports for measured results. Do not publish estimates as production facts.
