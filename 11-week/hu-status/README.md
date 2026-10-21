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
| HU-XXX-001 (template placeholder; actual ID not provided) | BySellens inventory observability workshop, CP0–CP10 status update | doing | [CP0](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de0e98590e8ba243dfad8c4400a25889069a21e9), [CP1](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/31cf6544d4ed3d2a488e58eccdfdbd5ec04a50d8), [CP2](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/a595545a4d5b3e4a9a2c619d8452b8cfc65f4c48), [CP3 first part](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ef628f2063132938142a5d97b309c6ef15d2002), [CP3 first-part guide](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f7a5446d4cd5a3aeaa32db0433ba6c1bc627a2c5), [CP3 second part: propagation foundation](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/de6fe20ae446ba1246ef7d91ea3df57ea0b2ba8b), [CP3 second part: instrumentation](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/819c6867a9668cfa2084d29c19afe55b9f60ade7), [CP3 tests](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6ab8352e155a6f0f6b19a9b31dffb4583973cada), [CP3 guide and evidence](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f470b5836277cee88e9bc104ded1cc7366168ed2), [CP4](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/47e1da9c57949fa1d9ec1c1e51bbc5381407089a), [CP6 — SLOs and error budget](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/efc855f9231724832c44f07aa4c10b4e792ed25c), [CP7 — burn-rate alerts](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/415069ac27aba186484e175554d6aa5a5e3c04bb), [CP8 — runbook](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6d5df83eb8cf8c34c1d4944555c114478fac894f), [CP9 — incident drill and postmortem](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/b7debd68c953254be79cd2c21e6d1b4010e25fff) |

## 2. My individual contribution
- **CP0:** Configured the workshop README and linked the participant-owned reflection document; the reflection itself was not written.
- **CP1:** Mapped the sale-and-stock-deduction journey across React, `sales-service`, `customer-service`, `inventory-service`, `product-service`, and RabbitMQ. Documented the service hops and proposed trace propagation; the production journey and propagation are not verified as running.
- **CP2:** Defined the structured-log contract and authored seven illustrative JSON records. The records were parser-validated for required fields, a common trace ID, chronological timestamps, and the two documented failure examples; they are design examples, not production logs.
- **CP3:** The first part added incoming W3C `traceparent` handling, response context, and JSON request logging. The second part was completed after the initial CP3 close: an outbound `RestClient` call now propagates the same trace ID in a child span to the documented Product stock route; a request counter and latency histogram are exposed at `/metrics`.
- **CP3 verification:** `mvn clean verify` completed with `BUILD SUCCESS`; Surefire reported 2 tests, 0 failures, 0 errors, and 0 skipped. The integration test uses a local simulated dependency to capture the actual outbound HTTP request, verify trace ID/span relationships, and verify both metric families and recorded samples. It exercises the harness, not the real BySellens service.
- **CP4:** Documented a proposed catalog of latency, traffic, error, and saturation metrics for the four real journey services, including PromQL and cardinality estimates. Separately, the CP3 harness implements a request counter and latency histogram exposed at `/metrics`; these harness metrics are not measurements from the real services. The proposed PromQL queries have not been run against an external Prometheus server.
- **CP6:** Defined proposed inventory SLOs for stock-query availability, 250 ms latency, and asynchronous per-item stock-update completion; recorded the 40-query/100-update per active day estimates, the 28-day window, operating schedule, and error budgets. The traffic values are exercise assumptions, not measured demand.
- **CP7:** Added multi-window fast- and slow-burn availability rules using the CP6 target, labels, and valid/error criteria. The policy explains low-volume/no-data behavior and that the windows are wall-clock ranges. The committed evidence does not establish a successful `promtool` execution or evaluation against a Prometheus server; no real PromQL evaluation is claimed.
- **CP8:** Added the fast-burn runbook with confirmation, escalation, and recovery guidance. Restart, rollback, and Product-to-PostgreSQL recovery procedures remain candidates pending validation; they are not approved or executable instructions.
- **CP9:** Added a clearly hypothetical 40-minute latency incident, a trace and exactly four sample JSON logs, SLI/budget arithmetic, CP7 alert analysis, and a blameless postmortem. A JSON parser validated the four records and a tool verified the arithmetic. The scenario is not production evidence; it demonstrates that successful slow responses do not trigger the availability-only CP7 alerts.
- **CP10:** This change updates the weekly status only. It does not create or verify the personal implementation plan or reflection.

## 3. Blockers and risks
- CP3 is implemented only as an isolated harness: the caller probe and local Product stock endpoint return `501`; no real sales are validated or persisted, and no real stock is read or updated. The test dependency is simulated locally.
- CP4 PromQL queries are proposals and have not been executed against an external Prometheus server or real scrape data. Harness metrics do not establish production inventory performance or saturation.
- The four CP3 second-part commits are present on `origin/main`. The available evidence does not verify two distinct required CP3 push operations; multiple commits in one push are not proof of multiple pushes.
- The workshop is not the real inventory system: the isolated CP3 harness returns deliberate `501` responses, uses a simulated dependency, and neither reads nor updates actual Stock.
- CP7 PromQL/alert behavior has not been evaluated against a real Prometheus server. A latency-only degradation with successful responses is not covered by the CP7 availability rules.
- Production telemetry, timeout configuration, on-call assignments, and executable mitigation/reversal procedures remain unverified. CP8's three production mitigation candidates are pending validation.
- CP1 records a discrepancy between the architecture service catalog and the Product/Sales event contracts for the `SaleRegistered` → `StockUpdated` path; resolve it against the product documentation/runtime before relying on that path operationally.
- No HU ID was supplied; `HU-XXX-001` remains the template placeholder.

## 4. Plan for next week
- If delivery requires live-service validation, integrate the instrumentation with the actual Product Service and verify behavior without treating the `501` harness as production behavior.
- Validate the proposed CP4 PromQL and CP7 alert rules against a Prometheus-compatible endpoint with real scrape data; add an approved latency alert if the Product Owner accepts its objective and window.
- Have the deployment/service owner validate actual timeout settings, on-call roles, and safe mitigation and rollback procedures before making the CP8 runbook executable.
- Confirm the actual HU ID and course/team evidence for distinct pushes and the required 15-minute intervals.
- Personally add a section to the existing Google Docs reflection titled **Implementation plan for my project**. It must be at least one page and explain, in English, which improvements to the real inventory you would implement first and in what order, which stories you would take into the next sprint, the risks or blockers you see, and what you personally learned. This section has not been written or verified here.
- Complete and personally review the reflection and implementation plan before the communicated deadline: Wednesday, 21 October 2026, 23:59 `America/Bogota`.

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
- [CP6 — SLOs and error budget](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/efc855f9231724832c44f07aa4c10b4e792ed25c)
- [CP7 — burn-rate rules and policy](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/415069ac27aba186484e175554d6aa5a5e3c04bb)
- [CP8 — fast-burn runbook](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/6d5df83eb8cf8c34c1d4944555c114478fac894f)
- [CP9 — hypothetical incident drill and postmortem](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/b7debd68c953254be79cd2c21e6d1b4010e25fff)

## 7. Delivery 2 submission status

- CP6, CP7, CP8, and CP9 each have a distinct commit on `origin/main`; CP9's push is confirmed. CP10 is this status-file update and has not been committed or pushed. Do not combine it with another checkpoint.
- The delivery requires at least five separate pushes for CP6–CP10 and at least 15 minutes between consecutive pushes. The remote commit history confirms the four checkpoint commits, but commit history does not prove push operations or their times. Push-time evidence for every interval is not available here, so compliance with the five-push/15-minute requirement remains **unverified** until confirmed through the course/team's accepted evidence.
- At least two real work blocks are required. No reliable duration or separate-work-block evidence was provided; this requirement remains **unverified** and must not be inferred from commit timestamps.
- CP6–CP9 files and commit links are present. The HU ID is still the placeholder `HU-XXX-001`; no confirmed ID was found.
- The reflection document is linked in the workshop README, but its content and the required one-page **Implementation plan for my project** have not been reviewed or verified. Add that section personally to the same Google Doc.
- CP10 has no commit URL yet. The latest confirmed pushed workshop commit is CP9, [`b7debd68c953254be79cd2c21e6d1b4010e25fff`](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/b7debd68c953254be79cd2c21e6d1b4010e25fff).
