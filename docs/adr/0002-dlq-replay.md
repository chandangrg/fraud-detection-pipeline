# ADR 0002: inspect before replay

The Kafka error handler routes exhausted records to a DLQ. A dedicated consumer persists DLQ payloads and
errors for operator inspection. Replay requires an API key and republishes one selected record to the source
topic. Bulk blind replay is intentionally omitted because it can amplify poison-message incidents.
