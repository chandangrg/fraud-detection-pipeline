# Study guide

## Recommended sequence

1. Trace a request through account checks, transaction persistence and the outbox.
2. Explain why Kafka publication cannot be part of the database commit.
3. Deliver the same event twice and inspect the durable processed-event record.
4. Submit a high-risk transaction and inspect the decision and review case.
5. Stop Kafka, observe outbox retries, restart it and confirm recovery.
6. Throw a consumer exception, inspect the DLQ record, correct the cause and replay it.
7. Stop Redis and prove transaction acceptance still uses PostgreSQL.

## Interview questions

- What ordering guarantee is achieved by using `accountId` as the Kafka key?
- Why does at-least-once delivery require idempotent consumers?
- Why is a Redis TTL marker insufficient for durable deduplication?
- How do leases recover when a publisher crashes after claiming rows?
- Which DLQ messages are safe to replay?

## Verification commands

```bash
./mvnw verify
docker compose --profile app up --build
curl http://localhost:8081/actuator/health
```
