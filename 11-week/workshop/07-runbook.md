# CP8 — Runbook: Product Stock Query Fast Burn

## 1. Identification and scope

- **Alert:** `ProductStockQueryAvailabilityFastBurn` (`severity="critical"`, `team="by_sellens"`).
- **Protected SLO:** CP6 Product stock-query availability, at least 99.5% over a rolling 28 calendar days.
- **Operation/boundary:** completed `GET /api/v1/products/{id}/stock` requests handled by `product-service`. The Product Service owns current `Stock` and availability; `inventory-service` owns Product information and immutable `initialStock`.
- **Alert condition:** both the 1-hour and 5-minute error ratios exceed 7.2% (14.4 × the 0.5% error allowance), as defined in [`06-alerts.yml`](./06-alerts.yml). CP6 counts handled `2xx` and `4xx` as good; `5xx` and other recorded status classes are errors. The 501 harness response is not a production failure signal.
- **Status:** This is a proposed operational runbook, not an approved production procedure. The workshop's CP3 harness is isolated and returns deliberate `501` responses; it is not the real inventory service.

**Roles and prerequisites.** Proposed responders are the On-Call Engineer for initial diagnosis, Tech Lead for technical decisions, Incident Commander (IC) for coordination, and Comms Lead/Product Owner for impact communication. The BySellens incident guide names IC, Tech Lead, On-Call Engineer and Comms Lead; CP7 also names the Product Owner. Actual assignments, escalation channels, access and permissions must be confirmed by the team. Before production diagnosis, obtain approved access to the alerting/Prometheus view, Product Service logs, and the real service's deployment and health interface. Do not put credentials or personal data in incident notes.

## 2. Symptoms and impact

An administrator may see a failed stock lookup, an error instead of current available quantity, or repeated difficulty checking inventory. The alert means the observed completed Product stock-query requests have a high error ratio in both recent windows. It does **not** identify a root cause, prove all users are affected, establish that stock quantities are wrong, or prove that asynchronous updates failed.

CP6's workload assumption is about 5 stock queries per operating hour, 8 a.m.–4 p.m. Monday–Saturday, `America/Bogota`; it is not measured traffic. A single failure among five requests is a 20% error ratio (40× burn), so low volume can make short windows volatile. A window with no requests has no denominator and cannot demonstrate health; the alert may be absent even if the service is unavailable. Prometheus windows are wall-clock ranges and include any requests observed outside the assumed schedule.

## 3. Confirm the signal

Run these queries only in an approved Prometheus UI/API configured for the real Product Service. No production Prometheus URL is documented. They use CP4/CP6 labels and the CP7 error definition.

**Errors and valid requests, last five minutes:**

```promql
sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock",status_class!~"2xx|4xx"}[5m]))
```

```promql
sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[5m]))
```

**Burn rate, last hour and last five minutes:** compare both values with `14.4`; CP7 fires only when both are above threshold.

```promql
(sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock",status_class!~"2xx|4xx"}[1h])) or vector(0)) / sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[1h])) / (1 - 0.995)
```

```promql
(sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock",status_class!~"2xx|4xx"}[5m])) or vector(0)) / sum(increase(http_server_requests_total{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[5m])) / (1 - 0.995)
```

**Latency proportion within 250 ms** (context only; CP6 SLO 2):

```promql
sum(increase(http_server_request_duration_seconds_bucket{service="product-service",method="GET",route="/api/v1/products/{id}/stock",le="0.25"}[5m])) / sum(increase(http_server_request_duration_seconds_count{service="product-service",method="GET",route="/api/v1/products/{id}/stock"}[5m]))
```

An absent/zero request denominator produces no meaningful burn or latency ratio; `or vector(0)` on the error numerator only handles an absent error series when a denominator exists. Check scrape/target availability separately. Zero errors with a positive denominator means zero observed burn for that interval, not proof of correct stock or full-system availability. Do not interpret absent samples as success.

**Trace and logs.** Ask the authorized user/reporting path for a failing request's `trace_id`; search only a real configured log store if one is confirmed. No central log or trace backend is documented. CP2 requires JSON fields including `timestamp`, `service`, `level`, `trace_id`, `span_id`, `event`, `message`, `duration_ms`, `http_method`, `route`, and `http_status`; never share tokens, credentials, customer details or raw request bodies.

The following is a reproducible **local harness-only** log search, not production diagnosis. In PowerShell, from `11-week/workshop/03-instrumentation`, start the harness and capture its JSON console output:

```powershell
mvn spring-boot:run 2>&1 | Tee-Object -FilePath .\harness.log
```

In a second PowerShell window at the same directory, issue a known trace and search that local file:

```powershell
$traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
curl.exe -i -H "traceparent: 00-$traceId-00f067aa0ba902b7-01" http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440001/stock
Select-String -Path .\harness.log -SimpleMatch ('"trace_id":"' + $traceId + '"')
```

This requires Java 17+, Maven 3.9+, and the local harness from CP3. It returns the deliberate `501` and logs it as `5xx`; it tests JSON/trace handling only. Do not treat it as evidence of a real incident, production recovery or current stock.

## 4. Mitigations and safety gates

**Three executable production mitigations cannot be provided from the available evidence.** The Product Service runbook in `code-corhuila/bysellens-docs` says implementation and executable procedures are pending, gives no execution command, and says no deployed Product endpoint is demonstrated. The local repository contains no Product Dockerfile, Compose file or deployment manifest. The `10-devops` local setup examples are templates, not actual project configuration. The entries below are candidates only; they are not instructions to execute.

1. **Candidate — restart/recover Product Service (lowest change scope).**
   - **Condition:** only after the Tech Lead confirms a stuck/unhealthy real Product instance and the deployment owner identifies its platform and safe restart procedure.
   - **Steps/command:** **pending validation**; no service instance name, platform command, health endpoint or restart procedure is documented. Do not run `docker compose`, `kubectl`, or harness commands against production.
   - **Expected result/risk:** a restart might restore a failed process; it may interrupt in-flight reads or conceal a persistent dependency/configuration fault. **Revert:** no restart rollback procedure is documented; stop and escalate if health is not restored or effects are unclear.
2. **Candidate — revert a confirmed bad Product release/configuration (higher risk).**
   - **Condition:** only when evidence correlates the onset with a specific change and the release owner confirms a known-good artifact and approval.
   - **Steps/command:** **pending validation**; release identifiers, deployment pipeline, rollback command, approval flow and configuration history are not documented. The generic incident playbook mentions rollback but does not establish Product's implementation.
   - **Expected result/risk:** restore prior behavior if the change caused the failures; risk includes incompatible schema/configuration or a longer outage. **Revert:** the forward/reapply procedure and compatibility are unknown; stop and escalate rather than improvising.
3. **Candidate — restore Product-to-PostgreSQL connectivity/configuration (highest change risk).**
   - **Condition:** only after read-only evidence identifies a database connectivity/configuration fault and the Product owner and database/service owner approve a specific correction.
   - **Steps/command:** **pending validation**; PostgreSQL and target `product_db.stocks` are documented, but no deployed address, credentials, query, network/config variables, access permissions or repair command are verified. Do not change database contents or secrets.
   - **Expected result/risk:** restore stock reads if connectivity is the cause; a wrong change can make stock unavailable or compromise consistency. **Revert:** no configuration rollback procedure is documented; stop if the proposed change lacks an approved before/after value and rollback.

Until those procedures are supplied and validated, restrict responders to approved read-only diagnostics, user-impact assessment and escalation. Do not edit stock manually, purge queues, replay messages, resend sales, or retry mutations: idempotency and actual event-processing state have not been verified for a deployed service.

## 5. Escalation and coordination

Escalate a firing critical alert, repeated real `5xx`, missing/stale telemetry that prevents assessment, any suspected stock inconsistency, or a mitigation requiring unconfirmed access/change authority. The On-Call Engineer performs initial evidence gathering; Tech Lead directs diagnosis; IC coordinates decisions when activated; Comms Lead/Product Owner communicates user impact. This role mapping is proposed pending team confirmation. Follow the documented incident-management severity process based on actual impact; do not equate Prometheus `critical` automatically with a particular P-level. Channels, individual contacts and response permissions remain to be confirmed.

Share: exact alert name/severity; first observed time (UTC and `America/Bogota`); affected operation and observed user impact; query windows, request/error counts and scrape status; a sanitized `trace_id` and relevant CP2 log fields; recent changes if known; actions and outcomes; and any stock-consistency concern. Never include secrets, tokens or unnecessary personal/product data.

## 6. Verify recovery

1. Confirm telemetry is present and the 1-hour and 5-minute request denominators are positive. Re-evaluate both CP7 burn queries; the alert condition must remain false with actual traffic, not merely disappear because a window has no samples.
2. Observe for at least one full **active one-hour window** with real requests, plus the 5-minute window, because the fast alert's longest condition is 1 hour. At the CP6 assumed rate this is about five requests, not a guarantee of a representative sample. If the period has no traffic, keep status unverified and wait for an active period; do not generate unapproved production requests.
3. Review the 5-minute 250-ms latency proportion and real error logs/traces as supporting evidence. A non-firing alert alone does not prove the CP6 28-day SLO has recovered.
4. If an approved read-only Product endpoint and safe known product are available, have an authorized operator verify that current Stock can be retrieved and compare it with an authoritative business record. No independent audit/query mechanism or deployed URL is documented; do not mutate stock to test consistency. If this check is unavailable, record consistency as unverified.

The harness can be used only as a separate local synthetic check. Its `501` is intentional, contributes to its own `5xx` metric and cannot verify production recovery or be counted as a successful synthetic stock read.

## 7. Closeout and pending work

Retain the alert/query snapshots, time window and timezone, scrape/denominator evidence, sanitized trace/log excerpts, user impact, recent changes, decision/approval trail, actions and outcomes, recovery checks, and unresolved risks in the team's approved incident record (system/channel TBD). Record whether stock consistency was verified. Follow the documented post-incident review process when applicable.

To make this runbook executable, the deployment/service owner must provide and validate: real environment/service identity and URLs; supported health/readiness check; deployment platform and read-only status/log commands; restart, rollback and configuration-change procedures with approval and reversal; authorized PostgreSQL/RabbitMQ diagnostics and recovery/idempotency policy; real Prometheus and log/trace access; and confirmed roles/channels. No such access or action was exercised here.

## References

- [CP1 journey](./01-journey.md): Product stock ownership, GET route and `SaleRegistered`/`StockUpdated` flow.
- [CP2 log contract](./02-log-contract.md): JSON fields, trace lookup and sensitive-data exclusions.
- [CP3 harness guide](./03-instrumentation/HOW-TO-RUN.md) and [implementation](./03-instrumentation/src/main/java/com/bysellens/productservice/instrumentation/InstrumentationApplication.java): local `/metrics`, console logs and deliberate `501`.
- [CP4 metric catalog](./04-metrics.md), [CP6 SLO](./05-slo.md), [CP7 rules](./06-alerts.yml) and [policy](./06-alert-policy.md).
- BySellens [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md) and [Product Service runbook](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/runbook.md): stock ownership and pending operational procedures.
- BySellens [Incident Management](https://github.com/code-corhuila/bysellens-docs/blob/main/13-operations/incident-management.md), [local setup](https://github.com/code-corhuila/bysellens-docs/blob/main/10-devops/local-setup.md) and [environments](https://github.com/code-corhuila/bysellens-docs/blob/main/10-devops/environments.md): generic role/response templates and unverified deployment examples.
