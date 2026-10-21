# CP2 — Structured Log Contract

## Scope and scenario status

This contract follows the CP1 journey: React submits a sale to `sales-service`; Sales validates the customer through `customer-service`, product information through `inventory-service`, and available stock through `product-service`; a successful sale would be persisted and published as `SaleRegistered`, then Product would deduct Stock and publish `StockUpdated`.

The BySellens sources do **not** document parallel validation, a precedence rule when multiple validations fail, or an implemented timeout value. The seven sample records below are a **proposed design scenario**, not observed logs or implemented behavior: after customer validation, for one sale item Sales starts the required Inventory active-product lookup and Product stock lookup independently. Product returns a quantity below the requested quantity while the Inventory lookup for that same product times out. The request is rejected without persisting a sale or publishing `SaleRegistered`. The proposed response is `503` because active-product information is unavailable; precedence over the independently detected insufficient-stock error is not specified by the source contracts. No retry is implied.

Every sample value, ID, service build label, environment, time, and duration below is fictional and illustrative. In particular, the sample duration does not prescribe a timeout configuration.

## 1. Field contract

Every record is one UTF-8 JSON object on one line. Emit the required fields on every record, using `null` only where the schema explicitly permits it; do not omit them. Durations are non-negative elapsed milliseconds. Timestamps are RFC 3339 UTC with a `Z` suffix.

| Field | Type | Required | Example | Purpose |
|---|---|---:|---|---|
| `timestamp` | string, ISO-8601 UTC | Yes | `2026-10-20T20:00:00.000Z` | Event time in UTC. |
| `level` | string enum | Yes | `INFO` | Severity: `ERROR`, `WARN`, `INFO`, or `DEBUG`. |
| `service` | string | Yes | `sales-service` | Source service from the CP1 journey. |
| `version` | string | Yes | `sample-build` | Deployed build identifier; example is a placeholder, not a documented BySellens version. |
| `environment` | string | Yes | `development` | Deployment environment; example is illustrative, not a known deployment. |
| `trace_id` | 32-character lowercase hex string | Yes | `11111111111111111111111111111111` | Joins records in the distributed trace. |
| `span_id` | 16-character lowercase hex string | Yes | `0000000000000001` | Identifies the CP1 operation represented by this record. |
| `correlation_id` | string | Yes | `corr-sample-001` | Application-level correlation; keep distinct from `trace_id`. The event envelope documents `metadata.correlationId`. |
| `event` | string | Yes | `inventory.stock.validation_rejected` | Stable, low-cardinality event name for filtering and alerts. |
| `message` | string | Yes | `Requested quantity exceeds available stock.` | Short, sanitized explanation; no raw payloads or secrets. |
| `duration_ms` | number, integer or decimal | Yes | `22` | Elapsed operation/request time; use `0` when no duration has accrued. |
| `http_method` | string | Optional | `GET` | HTTP method when recording an HTTP operation. |
| `route` | string | Optional | `/api/v1/products/{id}/stock` | Templated route; do not put identifiers in the path value. |
| `http_status` | integer | Optional | `503` | HTTP result when a response is produced. |
| `dependency_service` | string | Optional | `inventory-service` | CP1 service dependency involved in a call or timeout. |
| `product_id` | UUID string | Optional | `550e8400-e29b-41d4-a716-446655440001` | Product identifier from the documented Product/Stock contracts; include only when needed for diagnosis and permitted by access policy. |
| `quantity` | integer | Optional | `3` | Requested sale quantity or a Stock quantity; clarify by event name. |
| `available_quantity` | integer | Optional | `2` | Current quantity returned by Product for stock validation. |
| `sale_id` | UUID string | Optional | `850e8400-e29b-41d4-a716-446655440000` | Persisted Sale identifier; include only after a Sale exists. Do not invent one for a rejected, unpersisted sale. |
| `event_id` | UUID string | Optional | `950e8400-e29b-41d4-a716-446655440000` | Event-envelope ID for a published or consumed event. |
| `event_type` | string | Optional | `SaleRegistered` | Documented event type, when recording event processing. |
| `channel` | string | Optional | `sales.sale-registered` | Documented RabbitMQ channel/topic, when recording event processing. |
| `reason` | string | Optional | `SALE` | Documented `StockUpdated` sale example; do not create new enum values. |

Use an allow-list: emit only the required fields and operation-specific fields needed for diagnosis. Do not serialize request/response objects, exception messages from untrusted input, or arbitrary user-supplied strings.

## 2. Inventory and sale domain fields

The domain fields above are grounded in the project contracts; they are optional because not every record represents a stock operation.

- `product_id`: the Product identity used by Inventory and Product. Include on product lookups, stock checks, and stock processing; omit it where the record is only about the whole HTTP request.
- `quantity`: the integer requested for a sale item or a stock operation. Include when validating or applying a quantity; distinguish the value's role in the event/message.
- `available_quantity`: current `Stock.quantity` returned by Product. Include for availability checks and insufficient-stock decisions; do not substitute Inventory's immutable `initialStock`.
- `sale_id`: identifies a persisted Sale and appears in `SaleRegistered`. Include only after persistence; omit when validation rejects the request before a Sale is created.
- `event_id`, `event_type`, and `channel`: use the documented event envelope and `SaleRegistered` / `StockUpdated` contracts when processing RabbitMQ messages. Do not emit them for a request that never publishes an event.
- `reason`: include on `StockUpdated`; `SALE` is the documented sale example. No other reason values are assumed.

Relevant project elements include `Sale` and `SaleItem`; `Product` and its owned `Stock`; and the documented `SaleRegistered` and `StockUpdated` events.

## 3. Sensitive data handling

Never log passwords, login credentials, broker credentials, API keys, private keys, database connection strings, bearer tokens, JWTs, `Authorization` headers, cookies, or secrets from environment/configuration. Omit these fields entirely; do not hash or partially mask a credential and then retain it.

Do not log customer names, phone numbers, email addresses, addresses, or other direct personal data. Avoid logging `customer_id` unless an approved operational need and access policy explicitly allow it; prefer omission or a separately governed pseudonymous value. Do not log raw sale requests, payment details, arbitrary notes, or unneeded prices/totals. Prefer allow-listed operational fields; if a sensitive field is strictly required by an approved policy, mask it before the logging boundary and document that policy separately.

All examples in this file use fictitious values. The sample product IDs are illustrative identifiers, not customer or production data.

## 4. Seven-record failure example

This is the proposed parallel-validation case described above. Inventory is required for an active-product check; Product stock is checked for the same requested item. The log records the stock rule outcome and the dependency timeout in one trace. Since the Inventory lookup is required and unavailable, the example selects `503` for the overall request; the source does not define precedence when both findings occur. Validation failure means Sales does not persist the Sale and does not publish `SaleRegistered`, so Product does not receive this request and no `StockUpdated` is claimed.

```jsonl
{"timestamp":"2026-10-20T20:00:00.000Z","level":"INFO","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000001","correlation_id":"corr-sample-001","event":"sales.request.started","message":"Sale registration request received.","duration_ms":0,"http_method":"POST","route":"/api/v1/sales"}
{"timestamp":"2026-10-20T20:00:00.008Z","level":"INFO","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000002","correlation_id":"corr-sample-001","event":"sales.customer.validation_succeeded","message":"Customer validation completed.","duration_ms":8,"dependency_service":"customer-service","http_method":"GET","route":"/api/v1/customers/{id}","http_status":200}
{"timestamp":"2026-10-20T20:00:00.030Z","level":"INFO","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000004","correlation_id":"corr-sample-001","event":"sales.stock.lookup_succeeded","message":"Product returned current stock for a sale item.","duration_ms":22,"dependency_service":"product-service","http_method":"GET","route":"/api/v1/products/{id}/stock","http_status":200,"product_id":"550e8400-e29b-41d4-a716-446655440002","quantity":3,"available_quantity":2}
{"timestamp":"2026-10-20T20:00:05.005Z","level":"ERROR","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000003","correlation_id":"corr-sample-001","event":"sales.inventory.lookup_timeout","message":"Required Inventory product lookup timed out.","duration_ms":5000,"dependency_service":"inventory-service","http_method":"GET","route":"/api/v1/inventory/products/{id}","product_id":"550e8400-e29b-41d4-a716-446655440002"}
{"timestamp":"2026-10-20T20:00:05.008Z","level":"WARN","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000005","correlation_id":"corr-sample-001","event":"sales.stock.validation_rejected","message":"Requested quantity exceeds available stock.","duration_ms":1,"product_id":"550e8400-e29b-41d4-a716-446655440002","quantity":3,"available_quantity":2}
{"timestamp":"2026-10-20T20:00:05.009Z","level":"WARN","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000001","correlation_id":"corr-sample-001","event":"sales.sale.not_registered","message":"Sale was not persisted because required validation failed.","duration_ms":5009}
{"timestamp":"2026-10-20T20:00:05.011Z","level":"ERROR","service":"sales-service","version":"sample-build","environment":"development","trace_id":"11111111111111111111111111111111","span_id":"0000000000000001","correlation_id":"corr-sample-001","event":"sales.request.failed","message":"Sale registration failed because required validation was unavailable.","duration_ms":5011,"http_method":"POST","route":"/api/v1/sales","http_status":503}
```

The seven lines are chronological, use the CP1 Sales root and its customer, Inventory, Product, and validation child spans, and retain one fictional `trace_id`. The proposed independent lookups explain why a stock-rule rejection can be observed even though the required Inventory lookup for that same product times out. This scenario adds no retry, no fallback, no sale persistence, and no broker event. Without the proposed independent validation, the sequential CP1 flow cannot be claimed to produce both failures in one request.

## 5. Severity criteria

- **ERROR** — An operation failed or a required dependency was unavailable. Example: the required Inventory product lookup times out and the request cannot complete validation.
- **WARN** — A valid business rule rejects an operation without indicating a service defect. Example: requested sale quantity exceeds `available_quantity`; Sales does not register the Sale.
- **INFO** — A meaningful lifecycle event or successful operation. Example: a stock lookup succeeds, or a Sale is registered after validation.
- **DEBUG** — Extra diagnostic detail useful during development or targeted investigation. Example: record the validated `product_id` and quantity comparison, only where policy permits; never log the raw request or customer data.

## Project references

- [CP1 journey](./01-journey.md): endpoint sequence, spans, event flow, and documented propagation gaps.
- [Sales Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/README.md): validation order, sale persistence boundary, `201`/`422`/`503` behavior, and pending asynchronous completion.
- [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md): Stock ownership, integer quantity rules, and deductions exceeding available stock.
- [Sales event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/events.md) and [Product event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/events.md): SaleRegistered/StockUpdated fields, channels, and correlation metadata.
- [System observability guide](https://github.com/code-corhuila/bysellens-docs/blob/main/13-operations/observability.md): structured log concepts and a generic HTTP trace-context example; it does not establish this journey's runtime logging implementation.
