# Performance testing

Run the k6 scenario only after PostgreSQL, Redis, Kafka, and the API are healthy.

For every recorded run, document:

- hardware and operating environment
- application configuration
- virtual-user count and duration
- dataset and cache state
- request throughput and error rate
- p50, p95, and p99 latency

Performance results should be committed only when another engineer can reproduce the test from the documented configuration.
