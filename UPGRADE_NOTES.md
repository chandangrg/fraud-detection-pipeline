# Audit findings resolved

| Audit finding | Resolution | Primary code/tests |
|---|---|---|
| Kafka publish occurred inside the transaction | Transaction and event are committed to a transactional outbox; publishing is asynchronous | `TransactionInsertService`, `OutboxPublisher` |
| API read-then-write idempotency race | Unique key, request fingerprint and winner lookup after a concurrent constraint race | `TransactionService`, `TransactionIdempotencyPostgresIT` |
| Redis `hasKey/process/set` was non-atomic | Durable database claim and fraud decision commit in one transaction | `ProcessedEventRepository`, `FraudDecisionService`, `DurableIdempotencyPostgresIT` |
| No persisted decision or review workflow | Decisions and review cases are queryable database records | `FraudDecision`, `ReviewCase`, `FraudDecisionController` |
| Multiple outbox workers could duplicate work | PostgreSQL row locking and expiring leases | `OutboxEventRepository`, `OutboxLeasePostgresIT` |
| No operational DLQ path | Consumer retries, DLT publication, raw-text persistence, deterministic redelivery identity, inspection, metrics and API-key-protected OPEN-only replay | `KafkaConsumerConfig`, `DlqKafkaListenerConfig`, `DlqPersistenceConsumer`, `DlqReplayService`, `DlqRecordTest` |
| Cache treated as correctness state | Redis is only an optimization; database fallback and a short load lock protect the read path | `AccountCacheService` |
| No event contract version | Every event carries event ID, schema version, producer, timestamp and correlation ID | `TransactionEvent` |
| Missing deployment/quality evidence | CI, Testcontainers, JaCoCo, SpotBugs, OpenAPI, custom metrics, Docker, Kubernetes manifests, SLO examples, ADRs and runbook | repository root, `docs/` and `deploy/k8s/` |
