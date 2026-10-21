# CP7 — Inventory Burn-Rate Alert Policy

## Scope and metric semantics

The alerts target only the CP6 Product inventory read: `GET /api/v1/products/{id}/stock`, `service="product-service"`. CP6's availability objective remains 99.5% over a rolling 28 calendar days; valid events are completed handler requests on the route. `2xx` and handled `4xx` are good; `5xx` and any other recorded status classes are errors. CP4 bounds `status_class` to `2xx`, `4xx`, and `5xx`, so the alert's `status_class!~"2xx|4xx"` selector counts the documented bad classes. Business `4xx` outcomes remain good, not technical failures.

The expression calculates `error ratio / (1 - 0.995)`. Numerator and denominator use the same `service`, `method`, and templated `route`; each side is summed without labels before division, preventing accidental label-vector cross joins. `or vector(0)` makes an absent error-class series mean zero observed errors when valid denominator samples exist. A zero or absent denominator yields no alert result, not a passing SLI. Missing/stale series and scrape failures are not proof of availability; monitor telemetry separately.

The rules use the CP4 metric `http_server_requests_total` and labels, matching CP6's valid-event filters. They evaluate the full historical range and do not filter only at the current evaluation instant. CP6 defines valid requests at the Product handler and a 28-calendar-day window; its operating schedule and 40-query/day estimate are workload assumptions, not an exclusion of off-hours requests. Thus any requests during closed hours remain in these SLI windows, consistently with CP6. If the intended contract is to exclude off-hours events, first amend CP6 and implement an event-time-filtered metric or equivalent recording input; a current-time schedule predicate cannot remove off-hours samples already inside a range vector. No such metric is available or claimed here.

## Multi-window rules

[`06-alerts.yml`](./06-alerts.yml) defines two Prometheus alert rules, with one-minute rule evaluation:

- **Fast burn, critical:** both the 1-hour and 5-minute error ratios must exceed `14.4 × 0.5% = 7.2%`. The 1-hour window detects sustained rapid budget use; the 5-minute window confirms that the failure is still occurring. The short window is an explicit `and` condition, not a `for` delay.
- **Slow burn, warning:** both the 6-hour and 30-minute ratios must exceed `6 × 0.5% = 3%`. The longer window detects ongoing degradation; the 30-minute condition confirms recent impact. Again, the short window is part of the expression, not a `for` delay.

Every window is a wall-clock Prometheus range. Traffic is expected only during the assumed 8 a.m.–4 p.m. `America/Bogota`, Monday–Saturday schedule (about 5 stock queries per operating hour); no alert expression applies a misleading current-hour-only filter. A window wholly outside operation has no requests and cannot evaluate burn. A window crossing opening/closing has only the observed requests in its denominator. If the counter series is absent, the rule is absent; if requests exist and there are zero errors, the numerator fallback yields zero burn. Neither case establishes availability. The rules intentionally do not add a no-data alert.

## Error-budget impact calculations

CP6 estimates 40 stock queries per active day × 24 active days = **960 queries** per 28-day window, with 8 hours/day × 24 days = **192 scheduled operating hours**. The 0.5% error budget is 4.8 request-equivalents (at most 4 whole failed queries). Percentages below are the error-budget fraction consumed if the stated burn rate persisted for the entire window while it was fully within operating hours, with the assumed uniform request rate:

| Alert condition | Window | Burn × active time ÷ 192 h | Approx. budget consumed |
|---|---:|---:|---:|
| Fast | 1 h | 14.4 × 1 ÷ 192 | 7.5% |
| Fast | 5 min | 14.4 × (5/60) ÷ 192 | 0.625% |
| Slow | 6 h | 6 × 6 ÷ 192 | 18.75% |
| Slow | 30 min | 6 × 0.5 ÷ 192 | 1.5625% |

These are sustained-rate equivalents, not exact firing times or predictions. For comparison only, dividing by all 672 calendar hours would yield 2.14%, 0.179%, 5.36%, and 0.446%, respectively; that assumes uniform 24/7 traffic and is not the CP6 workload assumption. For a window partly outside scheduled hours, use its actual active-time overlap for an operational-time comparison. The alert itself uses observed request counts and does not assume that closed hours generated requests.

At 5 queries/hour, one failure among five queries in an hour is a 20% error ratio, or burn rate 40; one failure among three queries in a 30-minute interval is 33.3%, or burn rate about 66.7. A single failure can therefore dominate a low-volume short window and may satisfy both windows if it appears in both histories. Conversely, an interval with no requests has no denominator and prevents that burn condition from firing. The multi-window condition reduces isolated noise only when both observed windows have enough request evidence; it cannot manufacture volume or prove health.

The percentages do not identify the exact minute an alert fires. Firing depends on actual errors, traffic distribution, window history, scrape freshness, rule evaluation time, and the conjunction of both windows.

## Routing and operational policy

- `severity="critical"` (fast burn): page the on-call Product Service owner and SRE/observability role; coordinate inventory-impact mitigation. Notify the Product Owner for user/business impact.
- `severity="warning"` (slow burn): notify the Product Service owner and SRE/observability role for investigation and corrective work; involve the Product Owner if inventory operations are affected.
- Both rules carry `team="by_sellens"`. No individual recipients or paging systems are assumed to exist.
- `runbook_url` points to the planned [`07-runbook.md`](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/blob/main/11-week/workshop/07-runbook.md), which is intended for CP8 and does not exist yet.

Do not use these as the primary team-impact page:

1. **Database connection-pool saturation:** a proposed CP4 resource gauge can diagnose a cause but does not show that users failed to retrieve stock; it is not currently implemented for the real service.
2. **RabbitMQ queue depth/consumer lag:** queue topology/capacity and runtime consumers remain unverified, and queue pressure alone does not prove that a current stock query failed. Use as secondary diagnostics only after instrumentation and thresholds are established.

## Implementation and evidence limits

These are proposed real-service alerts, not verified production alerts. CP4's blanket statement that all catalog metrics are unimplemented is stale for the two HTTP metric families in the harness; its real-service catalog remains proposed. CP3's isolated harness exposes the HTTP counter but its Product stock stub returns `501`; the local test dependency is simulated and no actual inventory is read. Its requests would count as `5xx` in the harness, not demonstrate production availability. No Prometheus server is configured, and the PromQL/alert behavior has not been executed. `promtool` availability and YAML parsing results are reported separately by the authoring validation.

Project evidence:

- [CP1 journey](./01-journey.md): Product stock endpoint and service ownership boundary.
- [CP2 log contract](./02-log-contract.md): documented distinction between technical failures and business rejections.
- [CP4 metric catalog](./04-metrics.md): metric name, bounded labels and status-class values.
- [CP6 SLO and workload assumptions](./05-slo.md): 99.5% objective, 28-day window, 40 daily queries, schedule, and valid/good event criteria.
- [CP3 harness guide](./03-instrumentation/HOW-TO-RUN.md): current harness metrics, `501` behavior, and simulated dependency limitation.
- BySellens [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md), [Product event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/events.md), and [system architecture](https://github.com/code-corhuila/bysellens-docs/blob/main/05-architecture/overview.md).
