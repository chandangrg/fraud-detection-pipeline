# ADR 0001: durable consumer idempotency

A Redis `hasKey`/`set` pair is not an atomic durability boundary. The consumer now writes `ProcessedEvent`,
`FraudDecision`, and any `ReviewCase` in one database transaction. The processed-event primary key and the
fraud-decision unique transaction ID resolve concurrent duplicate deliveries. Redis remains a cache only.
