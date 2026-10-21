# CP6 — Inventory SLOs and Error Budget

## Scope and evidence status

This document measures the CP1 inventory segment, not the sale as a whole. `product-service` owns current `Stock` and its available quantity; `inventory-service` owns Product identity/details and immutable `initialStock`. A sale is only the documented trigger for an asynchronous stock deduction. The selected read is `GET /api/v1/products/{id}/stock` at the Product API boundary.

The actual documentation source is [`code-corhuila/bysellens-docs`](https://github.com/code-corhuila/bysellens-docs), not the workshop repository URL in `DOCS_REPO`. The Product and Inventory designs remain pending implementation/runtime verification. The workshop harness implements `http_server_requests_total` and `http_server_request_duration_seconds` for its Product stock probe, but that route returns `501`; its test dependency is simulated. It neither reads nor changes real stock. CP3 tests therefore do not establish production SLO compliance.

CP1 also records an architecture discrepancy: the architecture service catalog presents Product as REST-only, while the Product/Sales event contracts and sale sequence document the `SaleRegistered` → `StockUpdated` flow. This event path is a documented target, not verified runtime behavior; reconcile the source discrepancy before treating it as an operational dependency.

**CP4 has stale implementation statements:** it says the harness has no Prometheus registry/exporter or metrics and that all listed metrics are unimplemented. CP3 subsequently added the two HTTP metric families and `/metrics`. CP4's catalog for real services remains proposed; this file does not modify CP4.

## Workload assumptions and 28-day budget

These exercise estimates were supplied by the participant, not measured: 40 stock-query calls and 100 eligible individual stock-update operations per active day; 8 operating hours per day, Monday–Saturday, `America/Bogota`. A query is one GET call, not a sale or a product sold. An update is one unique eligible stock mutation, not a sale or a message redelivery. Assume a uniform rate only during operating hours.

| Calculation over 28 calendar days | Queries | Stock updates |
|---|---:|---:|
| Active days (6 × 4 weeks) | 24 | 24 |
| Estimated valid operations (per active day × 24) | 960 | 2,400 |
| Active time | 192 h | 192 h |

At the uniform-rate assumption, this is 5 stock queries and 12.5 eligible updates per operating hour; bursts and peak rates are unknown.

Initial objectives below are proposals for the exercise, not documented commitments or measured service levels. Whole-event budgets are rounded down so the target is not exceeded. A 28-day window includes 40,320 calendar minutes but only 11,520 scheduled operating minutes. Availability's 0.5% budget is **57.6 operating minutes**, not calendar downtime; for comparison, 0.5% of all calendar minutes would be 201.6 minutes and is not this SLO. Closed hours have no assumed query traffic. Uniform traffic is a planning assumption, not a peak-load claim.

## SLO 1 — Product stock-query availability

- **User experience / boundary:** An administrator can obtain current stock or a clear domain response. Count completed `GET /api/v1/products/{id}/stock` requests at the `product-service` HTTP handler; this does not measure the upstream Sales call or RabbitMQ.
- **Valid events:** Requests matched to this method and templated route with a recorded HTTP result. **Good:** `2xx` or handled `4xx`; **bad:** `5xx` and other recorded classes. A not-found/business response is handled availability, not a technical failure.
- **SLI:** good valid requests ÷ all valid requests.
- **PromQL (proposed query using CP4 labels):**

  ```promql
  sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock",status_class=~"2xx|4xx"}[28d]))
  /
  sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[28d]))
  ```

- **Objective/window:** At least **99.5%** per rolling **28 calendar days**. With 960 estimated requests, the arithmetic budget is 4.8 failures; at most **4 whole failed requests** fit the target (956/960 = 99.583%; 5 failures would be 99.479%). The equivalent total outage is 57.6 scheduled operating minutes only under uniform traffic and a complete service outage.
- **Exclusions and outcomes:** Exclude unmatched routes, `/metrics`, probes, and requests that never reach this handler; this is a server-side boundary. Technical `5xx`/timeouts are bad. Expected business/client `4xx` responses, including product-not-found when correctly handled, are good for availability and should be reported separately as bounded business outcomes. The CP4 `status_class` label cannot distinguish a business `4xx` from every other `4xx`; policy should be revisited if rate-limiting responses are introduced.
- **No traffic:** A zero denominator makes the SLI undefined, not 100%. Report “no data” and check scrape/traffic health; do not spend or replenish budget from an empty window.

## SLO 2 — Product stock-query latency

- **User experience / boundary:** The same current-stock GET completes promptly at the Product handler; this is not end-to-end sale latency.
- **Valid events:** Every completed request on the matched GET route with a recorded duration, including technical-error responses. **Good:** duration is at most 0.25 seconds. A business rejection is still a valid response and its time is measured; a technical failure is counted by availability and also counts as slow if it exceeds the threshold.
- **SLI:** valid requests in the `le="0.25"` histogram bucket ÷ all valid requests. This is a threshold proportion, not a percentile estimate.
- **PromQL (proposed query; CP4 and the harness define the 0.25-second bucket):**

  ```promql
  sum(increase(http_server_request_duration_seconds_bucket{service="product-service",method="GET",route="/api/v1/products/{id}/stock",le="0.25"}[28d]))
  /
  sum(increase(http_server_request_duration_seconds_count{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[28d]))
  ```

- **Objective/window:** At least **95% within 250 ms** per rolling **28 days**. At 960 estimated queries, at least **912** must be in the bucket; at most **48** may exceed it. This is a proposed target aligned to an available CP4 bucket, not a measured response time.
- **Exclusions and outcomes:** Same route exclusions as SLO 1. Do not exclude fast `5xx` from the latency denominator; availability separately records them as failures. Do not treat an `initialStock` value from Inventory as current-stock response data.
- **No traffic:** A zero denominator is undefined/no data, never a passing latency SLO. A missing scrape is also not success.

## SLO 3 — Individual asynchronous stock-update completion

- **User experience / boundary:** After Product begins processing one eligible stock-changing item from `SaleRegistered`, Product persists the resulting current quantity and emits the corresponding `StockUpdated` within five minutes. This measures **one individual Product stock mutation**, not completion of every item in a sale, sale persistence, or the HTTP `201`.
- **Event relationship and contract limit:** Use the initial `SaleRegistered.eventId` and the documented `StockUpdated.metadata.causationId` (with `productId`) for internal correlation; never make these IDs Prometheus labels. The contract requires Product to deduplicate at-least-once deliveries by `eventId`; runtime behavior is pending. The event contract does not provide a `saleId` on `StockUpdated`, a stable item key, or an aggregate completion state; exact multi-item completion is pending. Define a durable per-item correlation/deduplication rule before implementation. This SLO deliberately does not assert whole-sale completion.
- **Valid/good events:** One unique item operation whose stock rule permits a mutation is eligible. It is **good** only if the quantity is persisted and `StockUpdated` is emitted within 300 seconds from the first accepted Product processing of that item. A duplicate delivery does not create another operation or restart its timer. At five minutes, a still-pending operation is recorded once as not within target; a later update remains a miss and must not be recounted. Technical failures are bad. A valid insufficient-stock business rejection is excluded from this completion denominator and counted separately as a business outcome, not a technical error.
- **Required metrics — PROPOSALS, not present in CP4 or code:** `inventory_stock_update_operations_total{service="product-service",operation="sale_registered_item",result="within_5m|not_within_5m|technical_error|business_rejection"}` increments exactly once per unique item outcome; only the first three results enter the SLI denominator. `result` is a new bounded enum proposed because CP4 has no asynchronous outcome metric. Also propose `inventory_stock_update_duration_seconds` from first accepted processing through persisted quantity plus `StockUpdated` emission, with bounded labels `service`, `operation`, and fixed result; no IDs. Persisted correlation/deadline state, event-consumer instrumentation, and these metrics are not implemented in the harness.
- **SLI:** unique eligible operations completed within 300 seconds ÷ unique eligible operations with `within_5m`, `not_within_5m`, or `technical_error` outcomes.
- **PromQL (exact for the proposed counter):**

  ```promql
  sum(increase(inventory_stock_update_operations_total{service="product-service",operation="sale_registered_item",result="within_5m"}[28d]))
  /
  sum(increase(inventory_stock_update_operations_total{service="product-service",operation="sale_registered_item",result=~"within_5m|not_within_5m|technical_error"}[28d]))
  ```

- **Objective/window:** At least **95% within five minutes** per rolling **28 days**, proposed pending Product/operations approval. Assuming 2,400 unique eligible updates, at least **2,280** must meet the deadline and at most **120** may miss it. This is an event-count budget; it is not convertible to outage minutes.
- **No traffic:** Zero eligible operations or absent proposed metrics is “no data,” not success. Check the event source/consumer and scrape health; this SLO cannot detect undelivered triggers until trigger and consumer accounting are implemented.

## Error-budget policy (proposed; no controls are claimed as implemented)

Apply thresholds separately to each SLO's 28-day bad-event budget; for SLO 1 the budget is at most 4 whole failed requests, SLO 2 at most 48 slow requests, and SLO 3 at most 120 non-completions under the stated estimates.

| Budget consumed | Action and responsible roles |
|---|---|
| 50% | Product Service owner and SRE/observability role review the affected route/event outcomes, telemetry quality, and recent changes; prioritize corrective work. |
| 75% | Product Service owner and SRE/observability role coordinate mitigation and an incident review; Product Owner reviews business impact. Defer nonessential changes affecting the measured inventory path. |
| 100% | Declare the SLO breached; Product Service owner leads remediation with SRE/observability and Product Owner, and defer risky changes except approved fixes. Record cause and recovery evidence. |

Resume ordinary changes only after the cause is addressed, the metric/scrape is validated, and the rolling SLI is back within target with less than 50% of its budget consumed. Budget replenishes only as bad events leave the rolling window; do not reset it manually.

## Limitations and references

No PromQL was executed: no Prometheus server or production scrape data is available. The harness's `501` responses and simulated dependency do not demonstrate real inventory availability, latency, stock consistency, or asynchronous completion. The CP4 HTTP series currently cover harness routes only; all real-service targets and the asynchronous metrics above are proposals.

- [CP1 journey and ownership](./01-journey.md); [CP2 log/error semantics](./02-log-contract.md); [CP3 harness and tests](./03-instrumentation/HOW-TO-RUN.md); [CP4 metric catalog](./04-metrics.md).
- [BySellens Product vision](https://github.com/code-corhuila/bysellens-docs/blob/main/03-product/vision.md) and [architecture overview](https://github.com/code-corhuila/bysellens-docs/blob/main/05-architecture/overview.md).
- [Inventory Service ownership](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/04-inventory-service/README.md) and [Product Service stock design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md).
- [Product event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/events.md), [Sales event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/events.md), and [sale/stock sequence](https://github.com/code-corhuila/bysellens-docs/blob/main/08-uml/diagrams/source/seq-register-sale.puml).
