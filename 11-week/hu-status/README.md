<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- SPRINT_GOAL: Document and instrument the BySellens inventory sale journey while clearly tracking the isolated harness limits.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-001 (template placeholder; actual ID not provided) | BySellens inventory observability workshop, CP0–CP5 status update | doing | [CP0](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de0e98590e8ba243dfad8c4400a25889069a21e9), [CP1](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/31cf6544d4ed3d2a488e58eccdfdbd5ec04a50d8), [CP2](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/a595545a4d5b3e4a9a2c619d8452b8cfc65f4c48), [CP3 first part](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ef628f2063132938142a5d97b309c6ef15d2002), [CP3 first-part guide](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f7a5446d4cd5a3aeaa32db0433ba6c1bc627a2c5), [CP3 second part: propagation foundation](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de6fe20ae446ba1246ef7d91ea3df57ea0b2ba8b), [CP3 second part: instrumentation](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/819c6867a9668cfa2084d29c19afe55b9f60ade7), [CP3 tests](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ab8352e155a6f0f6b19a9b31dffb4583973cada), [CP3 guide and evidence](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f470b5836277cee88e9bc104ded1cc7366168ed2), [CP4](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/47e1da9c57949fa1d9ec1c1e51bbc5381407089a) |

## 2. My individual contribution
- **CP0:** Configured the workshop README and linked the participant-owned reflection document; the reflection itself was not written.
- **CP1:** Mapped the sale-and-stock-deduction journey across React, `sales-service`, `customer-service`, `inventory-service`, `product-service`, and RabbitMQ. Documented the service hops and proposed trace propagation; the production journey and propagation are not verified as running.
- **CP2:** Defined the structured-log contract and authored seven illustrative JSON records. The records were parser-validated for required fields, a common trace ID, chronological timestamps, and the two documented failure examples; they are design examples, not production logs.
- **CP3:** The first part added incoming W3C `traceparent` handling, response context, and JSON request logging. The second part was completed after the initial CP3 close: an outbound `RestClient` call now propagates the same trace ID in a child span to the documented Product stock route; a request counter and latency histogram are exposed at `/metrics`.
- **CP3 verification:** `mvn clean verify` completed with `BUILD SUCCESS`; Surefire reported 2 tests, 0 failures, 0 errors, and 0 skipped. The integration test uses a local simulated dependency to capture the actual outbound HTTP request, verify trace ID/span relationships, and verify both metric families and recorded samples. It exercises the harness, not the real BySellens service.
- **CP4:** Documented a proposed catalog of latency, traffic, error, and saturation metrics for the four real journey services, including PromQL and cardinality estimates. Separately, the CP3 harness implements a request counter and latency histogram exposed at `/metrics`; these harness metrics are not measurements from the real services. The proposed PromQL queries have not been run against an external Prometheus server.

## 3. Blockers and risks
- CP3 is implemented only as an isolated harness: the caller probe and local Product stock endpoint return `501`; no real sales are validated or persisted, and no real stock is read or updated. The test dependency is simulated locally.
- CP4 PromQL queries are proposals and have not been executed against an external Prometheus server or real scrape data. Harness metrics do not establish production inventory performance or saturation.
- The four CP3 second-part commits are present on `origin/main`. The available evidence does not verify two distinct required CP3 push operations; multiple commits in one push are not proof of multiple pushes.
- No HU ID was supplied; `HU-XXX-001` remains the template placeholder.

## 4. Plan for next week
- If delivery requires live-service validation, integrate the instrumentation with the actual Product Service and verify behavior without treating the `501` harness as production behavior.
- Validate the proposed CP4 PromQL against a Prometheus-compatible endpoint with real scrape data before claiming operational metrics.
- Confirm the actual HU ID and the required CP3 push evidence with the course/team process.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- [CP0 setup](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de0e98590e8ba243dfad8c4400a25889069a21e9)
- [CP1 journey](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/31cf6544d4ed3d2a488e58eccdfdbd5ec04a50d8)
- [CP2 log contract](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/a595545a4d5b3e4a9a2c619d8452b8cfc65f4c48)
- [CP3 instrumentation code](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ef628f2063132938142a5d97b309c6ef15d2002)
- [CP3 run guide](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f7a5446d4cd5a3aeaa32db0433ba6c1bc627a2c5)
- [CP3 second part: downstream trace context](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de6fe20ae446ba1246ef7d91ea3df57ea0b2ba8b)
- [CP3 second part: route metrics and scrape endpoint](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/819c6867a9668cfa2084d29c19afe55b9f60ade7)
- [CP3 second part: automated tests](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ab8352e155a6f0f6b19a9b31dffb4583973cada)
- [CP3 second part: run guide and test evidence](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f470b5836277cee88e9bc104ded1cc7366168ed2)
- [CP4 metric catalog](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/47e1da9c57949fa1d9ec1c1e51bbc5381407089a)
- [CP5 initial delivery status](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/5492849073403e73b2a4adb530b97c0c66e42f02)
- [CP5 status update with CP3 verification](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/15c13ddea2b602eb33ca04b5439b6bc23fea4e62)
