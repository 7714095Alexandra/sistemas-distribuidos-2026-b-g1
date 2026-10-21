<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- SPRINT_GOAL: Document and incrementally instrument the BySellens inventory sale journey; CP3 remains in progress.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-001 (template placeholder; actual ID not provided) | BySellens inventory observability workshop, CP0–CP4 | doing | [CP0](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de0e98590e8ba243dfad8c4400a25889069a21e9), [CP1](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/31cf6544d4ed3d2a488e58eccdfdbd5ec04a50d8), [CP2](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/a595545a4d5b3e4a9a2c619d8452b8cfc65f4c48), [CP3 code](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ef628f2063132938142a5d97b309c6ef15d2002), [CP3 guide](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f7a5446d4cd5a3aeaa32db0433ba6c1bc627a2c5), [CP4](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/47e1da9c57949fa1d9ec1c1e51bbc5381407089a) |

## 2. My individual contribution
- **CP0:** Configured the workshop README and linked the participant-owned reflection document; the reflection itself was not written.
- **CP1:** Mapped the sale-and-stock-deduction journey across React, `sales-service`, `customer-service`, `inventory-service`, `product-service`, and RabbitMQ. Documented the service hops and proposed trace propagation; the production journey and propagation are not verified as running.
- **CP2:** Defined the structured-log contract and authored seven illustrative JSON records. The records were parser-validated for required fields, a common trace ID, chronological timestamps, and the two documented failure examples; they are design examples, not production logs.
- **CP3 (first part only):** Added an isolated Java 17 / Spring Boot `product-service` harness with incoming W3C `traceparent` handling, response context, and JSON request logging. `mvn clean package` completed with `BUILD SUCCESS`; manual requests with and without a valid `traceparent` returned `501` and emitted request logs. The harness is not the real BySellens service.
- **CP4:** Documented proposed latency, traffic, error, and saturation metrics for the four journey services, including PromQL and cardinality estimates. The metrics are not implemented or measured, and the PromQL was not run against Prometheus.

## 3. Blockers and risks
- CP3 is incomplete: the harness has no outbound trace-context propagation, request-result counter, latency histogram, or automated test proving that the received trace ID matches the outbound one. No `src/test` tree is present.
- The CP3 endpoint deliberately returns `501`; the isolated harness has no Stock data, database, or production capacity. It cannot demonstrate real inventory behavior, performance, or saturation.
- Both CP3 commits are present on `origin/main`, but the available public GitHub push-event history does not verify two distinct CP3 pushes. Two commits are not evidence of two pushes.
- No HU ID was supplied; `HU-XXX-001` remains the template placeholder.

## 4. Plan for next week
- Complete the remaining CP3 instrumentation requirements and automated propagation test against an explicitly identified service boundary; do not treat the `501` harness as production behavior.
- Add and verify metrics only when they can be tied to the real service implementation; validate the proposed PromQL with a Prometheus-compatible endpoint and scrape data.
- Confirm the actual HU ID and the required CP3 push evidence with the course/team process.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- [CP0 setup](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de0e98590e8ba243dfad8c4400a25889069a21e9)
- [CP1 journey](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/31cf6544d4ed3d2a488e58eccdfdbd5ec04a50d8)
- [CP2 log contract](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/a595545a4d5b3e4a9a2c619d8452b8cfc65f4c48)
- [CP3 instrumentation code](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ef628f2063132938142a5d97b309c6ef15d2002)
- [CP3 run guide](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f7a5446d4cd5a3aeaa32db0433ba6c1bc627a2c5)
- [CP4 metric catalog](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/47e1da9c57949fa1d9ec1c1e51bbc5381407089a)
