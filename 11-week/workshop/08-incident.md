# CP9 — Hypothetical Inventory Incident Drill and Blameless Postmortem

> **Tabletop exercise only.** This scenario, trace, four JSON records, alert evaluation, and actions are fabricated for analysis. They are not production telemetry, a real alert, or evidence that any mitigation was executed.

## 1. Scenario and assumptions

- **Affected CP1 hop:** synchronous HTTP from `sales-service` to `product-service`: `GET /api/v1/products/{id}/stock`. Product owns current `Stock`; `inventory-service` owns Product information and immutable `initialStock`.
- **Inventory impact:** delayed stock lookups slow the Sales validation that reads available quantity before a sale. They do not themselves change stock. If a sale is accepted and `SaleRegistered` is published, Product's separate asynchronous processing may later update Stock and publish `StockUpdated`; this latency scenario gives no evidence about whether that update completes.
- **Exercise interval:** Wednesday, 21 October 2026, 09:00–09:40 `America/Bogota` (14:00–14:40 UTC), inside CP6's assumed 08:00–16:00 Monday–Saturday schedule.
- **Workload:** CP6's estimate is 40 stock queries per active day, or 5/hour. It is an estimate, not measured traffic. Forty minutes therefore implies 3⅓ expected queries, including ⅚ expected slow queries at 25%. For an explicit discrete alert example, use four queries at 09:00, 09:12, 09:24, and 09:36, with only the last taking 3 seconds. Four is a possible integer sample, not a revised traffic estimate or a claim of measured demand.
- **Prior state / evaluation:** assume no other bad latency events in the rolling 28-day window and no HTTP errors before or during this drill. Prometheus rules evaluate every minute as in CP7. Assume telemetry is present for each completed request.
- **Response and timeout:** assume the delayed calls complete successfully as HTTP `200` after 3 seconds, and the Sales request completes as `201`; the other 75% complete within 250 ms. CP2 documents no configured timeout value. CP3's harness has no documented request-timeout value either. Thus 3 seconds alone proves neither a timeout nor an HTTP error. The successful response assumption is for this hypothetical only; the production timeout and status are unknown.
- For SLO 2 arithmetic, assume the 3-second delay is also observed at Product's CP6 handler boundary (and therefore exceeds 250 ms); a caller-side round trip alone would not establish that server-side duration.

## 2. Hypothetical trace of the slow request

The final discrete request starts at 09:36 local. Assume HTTP `traceparent` is propagated as proposed by CP1, but not verified for the real services. Times and IDs below are fictional. This is a focused synchronous trace slice; it does not claim the CP3 harness runs the CP1 journey or show the asynchronous event branch.

```text
trace_id=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
└─ span_id=0000000000000001 sales-service HTTP server: POST /api/v1/sales (root, 3,400 ms)
   └─ span_id=0000000000000002 sales-service HTTP client: GET /api/v1/products/{id}/stock (3,000 ms)
      └─ span_id=0000000000000003 product-service HTTP server: GET /api/v1/products/{id}/stock (2,980 ms)
```

The Product server span starts 20 ms after the caller span and ends with its response; the caller receives that response at 3,000 ms. The remaining 400 ms in the root span represents hypothetical pre/post-hop work, not a measured breakdown. All spans share one trace ID and have distinct span IDs. Exactly four fictional CP2-shaped JSON log records for this trace follow; the Product start and completion records refer to the same server span.

```jsonl
{"timestamp":"2026-10-21T14:36:00.220Z","level":"INFO","service":"product-service","version":"tabletop","environment":"hypothetical","trace_id":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","span_id":"0000000000000003","correlation_id":"corr-cp9-example-01","event":"product.stock.query.started","message":"Hypothetical stock query started.","duration_ms":0,"http_method":"GET","route":"/api/v1/products/{id}/stock"}
{"timestamp":"2026-10-21T14:36:03.200Z","level":"INFO","service":"product-service","version":"tabletop","environment":"hypothetical","trace_id":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","span_id":"0000000000000003","correlation_id":"corr-cp9-example-01","event":"product.stock.query.completed","message":"Hypothetical stock query completed successfully.","duration_ms":2980,"http_method":"GET","route":"/api/v1/products/{id}/stock","http_status":200}
{"timestamp":"2026-10-21T14:36:03.200Z","level":"INFO","service":"sales-service","version":"tabletop","environment":"hypothetical","trace_id":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","span_id":"0000000000000002","correlation_id":"corr-cp9-example-01","event":"sales.stock.lookup.completed","message":"Hypothetical Product stock lookup completed successfully.","duration_ms":3000,"http_method":"GET","route":"/api/v1/products/{id}/stock","http_status":200,"dependency_service":"product-service"}
{"timestamp":"2026-10-21T14:36:03.400Z","level":"INFO","service":"sales-service","version":"tabletop","environment":"hypothetical","trace_id":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","span_id":"0000000000000001","correlation_id":"corr-cp9-example-01","event":"sales.request.completed","message":"Hypothetical sale request completed.","duration_ms":3400,"http_method":"POST","route":"/api/v1/sales","http_status":201}
```

The JSON is illustrative CP2-format output, not captured logs. It contains no product/customer IDs or other personal data. A `201` is not evidence that asynchronous stock updating completed; no `StockUpdated` result is asserted here.

## 3. SLI and error-budget impact

CP6's 28-day estimate is 960 queries and 4.8 availability failure-equivalents (at most 4 whole failures); its latency budget is 48 requests over 250 ms. Assume the rest of the 28-day window is within target and the four drill queries are included in, not added to, the 960-query estimate.

| Measure | During this 40-minute scenario | Rolling 28-day implication under stated assumptions |
|---|---:|---:|
| Availability, CP6 SLO 1 | 4/4 successful responses = **100%**; 0 errors and 0 of 4.8 failure-equivalents consumed | 960/960 = **100%** if all other requests also succeed; slow success is not downtime |
| Latency, CP6 SLO 2 | 3/4 within 250 ms = **75%**, below the 95% objective for this incident slice | Discrete sample: 1 slow / 48 allowed = **2.08%** budget consumed; 959/960 = **99.896%** over the window |
| Latency, expected-value calculation | 3⅓ expected queries, ⅚ expected slow; expected within-threshold proportion remains 75% | ⅚ / 48 = **1.74%** of the latency budget; (960 − ⅚)/960 = **99.913%** expected rolling SLI |
| Async stock-update completion, CP6 SLO 3 | **Not inferable**: no event acceptance, persistence, `StockUpdated`, or completion-time outcome is given | No update count, SLI, or budget consumption can be calculated from this read latency |

The 75% incident-slice latency proportion does not mean the full 28-day SLO is 75%. The discrete one-slow-request case uses the minimum four-request sample that can express exactly 25%; the expected workload arithmetic remains fractional and uses CP6's 5/hour estimate. No delay is converted to availability minutes. A slow synchronous read may delay a sale's processing, but does not prove or time its later asynchronous stock update.

## 4. CP7 alert evaluation

CP7 evaluates the Product GET's `http_server_requests_total` counter with `service="product-service"`, `method="GET"`, `route="/api/v1/products/{id}/stock"`. Its error selector is `status_class!~"2xx|4xx"`; handled `2xx` and `4xx` count as good. Fast burn requires error ratio / 0.005 **> 14.4** in both `[1h]` and `[5m]`; slow burn requires **> 6** in both `[6h]` and `[30m]`. These are the existing availability rules, not latency alerts.

For the explicit successful sequence, by the 09:37 evaluation the 1-hour and longer windows have valid request denominators, while the 5-minute and 30-minute windows include the 09:36 request. Each window has zero errors, so its error numerator is zero and burn rate is zero. Before any requests, an empty denominator yields no result, not demonstrated availability. **Neither alert fires at any evaluation during the 40 minutes** under the assumed successful statuses; the 3-second duration does not enter either alert expression.

Counterfactual only: if the 09:36 request instead completed as a `5xx`, by the next one-minute evaluation at 09:37 the 1-hour error ratio would be 1/4 = 25% (50× burn), and the 5-minute ratio 1/1 = 100% (200×). Both exceed the fast thresholds. The 6-hour ratio would be 1/4 = 25% (50×), and 30-minute ratio 1/3 = 33⅓% (66⅔×), so slow burn would also qualify at that same evaluation. This requires an error response not specified by the scenario; it does not occur in the primary successful-response case. A real firing minute cannot be inferred without actual statuses, samples, scrape timing, and evaluation history.

## 5. Response using CP8

In this hypothetical, follow CP8's **Confirm the signal** and **Escalation and coordination** steps: compare status counts and request denominators, inspect the Product stock-query latency proportion, and correlate sanitized trace/log records if an authorized real backend exists. The CP3 harness is not a substitute: it uses a simulated dependency, returns deliberate `501`, and does not read or update real Stock.

CP8's **Verify recovery** calls for at least one active hour with real traffic, positive denominators, and both burn windows clear; it also says not to infer recovery from no traffic. For a latency-only incident, additionally confirm the 250-ms histogram proportion and real user impact. Its runbook has no approved production mitigation: restart, rollback, and Product-to-PostgreSQL recovery are explicitly pending validation. Do not claim or execute them from this drill. If no verified action can reduce the delay, maintain read-only diagnosis and escalate to the Product Service owner/Tech Lead and Incident Commander roles; any intervention remains hypothetical until the deployment owner validates its procedure and rollback. Do not replay events, alter quantities, or purge queues.

## 6. Blameless postmortem

**Summary.** Hypothetically, for 40 minutes one quarter of synchronous stock lookups took 3 seconds but returned successfully. Current stock reads were delayed; the assumed status mix does not burn availability budget, while the incident slice misses the latency target. The CP7 availability alerts do not detect this latency-only condition.

**Timeline (local, `America/Bogota`; hypothetical):**

| Time | Event |
|---|---|
| 09:00 | Assumed degradation begins during CP6 operating hours. |
| 09:00–09:40 | Four illustrative stock queries; one at 09:36 takes 3 seconds, all four assumed successful. |
| 09:37 | CP7 evaluation has request denominators but no errors; neither burn alert fires. |
| 09:40 | Assumed degradation ends; no production recovery or mitigation is claimed. |
| 10:40 or later | Earliest end of the CP8 minimum one-active-hour observation if real traffic is available; latency and stock consistency still require separate verification. |

**Impact and budgets.** At CP6's estimated 5 queries/hour, the interval represents 3⅓ expected lookups and ⅚ expected slow lookups. In the discrete example, one of four requests is slow. With all responses successful, availability remains 100% in the sample and consumes no availability budget. The slow request consumes 1/48 (2.08%) of the rolling latency budget; its incident-slice latency SLI is 75%. The expected-value budget use is 1.74%. No async update impact or SLO 3 budget can be concluded.

**Hypothetical cause and contributors, not findings.** A Product stock-query latency regression is the scenario's assumed symptom; the underlying root cause is **unknown**. Resource contention, an internal dependency, or a recent change are investigation hypotheses only—none is established by project evidence or this drill. Contributors to delayed detection would be that CP7 covers availability rather than latency, traffic is low, and production timeout/telemetry configuration is unverified.

**What helped / what was missing.** CP1 identifies the stock-query hop and Product's ownership of current Stock; CP2 provides a sanitized trace/log contract; CP6 defines the latency threshold; CP8 provides a cautious confirmation/escalation path. Missing are verified production telemetry and timeout settings, a latency alert, executable/approved Product mitigations, and evidence linking query latency to asynchronous update completion.

**Follow-up user stories (proposed roles; acceptance criteria are verifiable):**

1. **As the Product Service owner,** I want server-side stock-query latency and status telemetry so that successful-but-slow reads are visible. **Accept:** a test request lasting 3 seconds appears above the `le="0.25"` bucket and in the request count; bounded CP4 labels are used; a successful `2xx` remains distinct from a `5xx`; production rollout and scrape are verified.
2. **As the SRE/observability role,** I want an approved latency alert for the CP6 stock-query SLO so that latency-only degradation is detectable. **Accept:** the Product Owner approves threshold/window; rule tests cover 3-second slow successes, errors, zero traffic, and missing series; `promtool check rules` passes; a staging evaluation demonstrates firing and recovery without changing CP7's availability objectives.
3. **As the Product Service owner,** I want to correlate accepted stock-update triggers with persisted Stock and `StockUpdated` completion so that delayed reads are not mistaken for update failure. **Accept:** tests verify unique eligible-event accounting, duplicate delivery handling, the five-minute CP6 deadline, and late completion; no IDs are Prometheus labels; a delayed query alone neither passes nor fails this SLO.

## References and evidence limits

- [CP1 journey](./01-journey.md): Sales-to-Product stock GET, `SaleRegistered` / `StockUpdated`, and ownership boundary.
- [CP2 log contract](./02-log-contract.md): required JSON fields, status semantics, and sensitive-data exclusions.
- [CP3 harness guide](./03-instrumentation/HOW-TO-RUN.md), [client](./03-instrumentation/src/main/java/com/bysellens/productservice/instrumentation/ProductStockClient.java), and [test](./03-instrumentation/src/test/java/com/bysellens/productservice/instrumentation/TracePropagationMetricsTest.java): simulated dependency, deliberate `501`, and instrumentation limits.
- [CP4 metric catalog](./04-metrics.md), [CP6 SLOs](./05-slo.md), [CP7 alert rules](./06-alerts.yml), and [CP8 runbook](./07-runbook.md).
- BySellens [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md), [Product event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/events.md), and [incident management](https://github.com/code-corhuila/bysellens-docs/blob/main/13-operations/incident-management.md).

No production fault was injected; PromQL was not run against a real Prometheus server; no CP8 mitigation was performed. CP4's descriptions of the harness are stale regarding the counter/histogram and `/metrics`; CP6 already records this discrepancy. No other checkpoint file was changed for CP9.
