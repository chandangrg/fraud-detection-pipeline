# Changelog

## Unreleased — initial public release
- Added concurrency-safe API idempotency and a transactional outbox.
- Replaced Redis consumer deduplication with an atomic durable processed-event/decision transaction.
- Persisted fraud decisions and review cases.
- Added leased multi-instance outbox publication, bounded sends, retry/backoff, and operator replay.
- Added consumer retries and a raw-text DLQ inspection path with deterministic topic/partition/offset identity.
- Limited Redis to cache optimization with PostgreSQL fallback and a short load lock.
- Added versioned event contracts, Flyway, Testcontainers, CI, JaCoCo, SpotBugs, OpenAPI, metrics, non-root Docker, Kubernetes references, ADRs, SLO examples, and runbooks.


