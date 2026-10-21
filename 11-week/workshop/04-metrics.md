# CP4 — Golden Signals and Metric Catalog

## Scope and implementation status

This catalog follows the CP1 sale journey and covers its four backend services: `sales-service`, `customer-service`, `inventory-service`, and `product-service`. React and RabbitMQ are journey participants, but are not application services in these tables.

**All metrics and PromQL below are proposed dashboard instrumentation, not implemented or measured.** CP3's isolated `product-service` harness emits structured request logs and returns `501`; it has no Prometheus registry/exporter or scrape endpoint, explicit meter instrumentation, Stock data, database, or production service capacity. The BySellens service documents describe target designs whose backend implementation and runtime verification are pending. No Prometheus server or scrape data is available here, so none of these PromQL queries was executed.

## Histogram and label policy

Use these proposed finite latency bucket boundaries, in seconds: `0.05`, `0.1`, `0.25`, `0.5`, `1`, `2.5`, `5`. They put several boundaries around the response times an administrator should be able to distinguish while consulting products, checking a customer, and registering a sale; the 5-second boundary makes slower or stalled calls visible. **The sources define no latency SLO or measured response times. These are proposed objectives for initial dashboards, not observations or approved SLOs.** Review them against real measurements and product expectations before setting alerts.

For the CP1 HTTP request histogram, use bounded labels `service`, `method`, `route`, and `status_class`, where `route` is a registered route template (never the raw URL) and `status_class` is one of `2xx`, `4xx`, `5xx`. Each service has one CP1 route and method in this catalog; allow three status classes. Thus there are at most 3 label sets per service. Seven finite buckets plus `+Inf`, `_sum`, and `_count` produce 10 time series per label set, or **30 latency series per service**.

Never use unbounded labels such as `user_id`, `product_id`, `trace_id`, email, customer name, or raw URL. Put trace identifiers in logs/traces, not Prometheus labels. Use bounded alternatives: route templates, HTTP method, status class, a fixed operation name, and controlled result/reason enums. Keep `service` values to the four names above. Proposed domain rejection reasons are limited to documented cases such as `insufficient_stock`, `customer_not_found`, and `product_not_found`; do not put free-form exception text in labels.

## `sales-service`

CP1 operation: `POST /api/v1/sales`. Sales validates customer, active product, and stock, persists `Sale`/`SaleItem` after successful validation, publishes `SaleRegistered`, and consumes `StockUpdated`.

| Golden signal | Metric | Purpose | Type | Unit | Labels and series estimate | Dashboard PromQL (proposed) |
|---|---|---|---|---|---|---|
| Latency | `http_server_request_duration_seconds` | P95 sale-registration request latency | Histogram | seconds | `service`=1, `method`=1 (`POST`), `route`=1, `status_class`=3; 30 series incl. buckets, `_sum`, `_count` | `histogram_quantile(0.95, sum by (le) (rate(http_server_request_duration_seconds_bucket{service="sales-service",route="/api/v1/sales"}[5m])))` |
| Traffic | `http_server_requests_total` | Incoming sale requests per second | Counter | requests/second via `rate` | `service`=1, `method`=1, `route`=1, `status_class`=3; 3 series | `sum(rate(http_server_requests_total{service="sales-service",route="/api/v1/sales"}[5m]))` |
| Errors | `service_errors_total` | Fraction of requests failing for technical reasons; business rejections are excluded | Counter | errors/second via `rate`; ratio is unitless | `service`=1, `operation`=1 (`register_sale`), `error_type`=3 (`dependency_timeout`, `dependency_unavailable`, `internal`); 3 series. Separate `business_rejections_total` has up to 3 documented reason values; 3 additional series. | `sum(rate(service_errors_total{service="sales-service",operation="register_sale"}[5m])) / clamp_min(sum(rate(http_server_requests_total{service="sales-service",route="/api/v1/sales"}[5m])), 1e-9)` |
| Saturation | `service_resource_utilization_ratio` | Highest utilization of configured JVM heap, HTTP worker, or DB connection capacity | Gauge | ratio, 0–1 | `service`=1, `resource`=3 (`jvm_heap`, `http_workers`, `db_connections`); 3 series | `max by (resource) (service_resource_utilization_ratio{service="sales-service"})` |

For business rejections, separately query `sum(rate(business_rejections_total{service="sales-service",reason="insufficient_stock"}[5m]))`. Do not count a documented stock insufficiency rejection as a technical failure.

## `customer-service`

CP1 operation: `GET /api/v1/customers/{id}` validates an active business customer for Sales.

| Golden signal | Metric | Purpose | Type | Unit | Labels and series estimate | Dashboard PromQL (proposed) |
|---|---|---|---|---|---|---|
| Latency | `http_server_request_duration_seconds` | P95 customer lookup latency | Histogram | seconds | `service`=1, `method`=1 (`GET`), `route`=1, `status_class`=3; 30 series incl. buckets, `_sum`, `_count` | `histogram_quantile(0.95, sum by (le) (rate(http_server_request_duration_seconds_bucket{service="customer-service",route="/api/v1/customers/{id}"}[5m])))` |
| Traffic | `http_server_requests_total` | Customer lookup requests per second | Counter | requests/second via `rate` | `service`=1, `method`=1, `route`=1, `status_class`=3; 3 series | `sum(rate(http_server_requests_total{service="customer-service",route="/api/v1/customers/{id}"}[5m]))` |
| Errors | `service_errors_total` | Fraction of lookups failing for technical reasons; 404 for an absent/deleted customer is a business outcome | Counter | errors/second via `rate`; ratio is unitless | `service`=1, `operation`=1 (`get_customer`), `error_type`=3; 3 series. Separate `business_rejections_total` has 1 documented `customer_not_found` reason; 1 additional series. | `sum(rate(service_errors_total{service="customer-service",operation="get_customer"}[5m])) / clamp_min(sum(rate(http_server_requests_total{service="customer-service",route="/api/v1/customers/{id}"}[5m])), 1e-9)` |
| Saturation | `service_resource_utilization_ratio` | Highest utilization of configured JVM heap, HTTP worker, or DB connection capacity | Gauge | ratio, 0–1 | `service`=1, `resource`=3 (`jvm_heap`, `http_workers`, `db_connections`); 3 series | `max by (resource) (service_resource_utilization_ratio{service="customer-service"})` |

Track expected `customer_not_found` outcomes separately with `sum(rate(business_rejections_total{service="customer-service",reason="customer_not_found"}[5m]))`; do not include them in the technical error numerator.

## `inventory-service`

CP1 operation: `GET /api/v1/inventory/products/{id}` returns authoritative active Product information to Sales. Inventory owns Product identity and immutable `initialStock`, not current Stock.

| Golden signal | Metric | Purpose | Type | Unit | Labels and series estimate | Dashboard PromQL (proposed) |
|---|---|---|---|---|---|---|
| Latency | `http_server_request_duration_seconds` | P95 active-product lookup latency | Histogram | seconds | `service`=1, `method`=1 (`GET`), `route`=1, `status_class`=3; 30 series incl. buckets, `_sum`, `_count` | `histogram_quantile(0.95, sum by (le) (rate(http_server_request_duration_seconds_bucket{service="inventory-service",route="/api/v1/inventory/products/{id}"}[5m])))` |
| Traffic | `http_server_requests_total` | Active-product lookup requests per second | Counter | requests/second via `rate` | `service`=1, `method`=1, `route`=1, `status_class`=3; 3 series | `sum(rate(http_server_requests_total{service="inventory-service",route="/api/v1/inventory/products/{id}"}[5m]))` |
| Errors | `service_errors_total` | Fraction of product lookups failing for technical reasons; 404 for absent/deleted Product is a business outcome | Counter | errors/second via `rate`; ratio is unitless | `service`=1, `operation`=1 (`get_inventory_product`), `error_type`=3; 3 series. Separate `business_rejections_total` has 1 documented `product_not_found` reason; 1 additional series. | `sum(rate(service_errors_total{service="inventory-service",operation="get_inventory_product"}[5m])) / clamp_min(sum(rate(http_server_requests_total{service="inventory-service",route="/api/v1/inventory/products/{id}"}[5m])), 1e-9)` |
| Saturation | `service_resource_utilization_ratio` | Highest utilization of configured JVM heap, HTTP worker, or DB connection capacity | Gauge | ratio, 0–1 | `service`=1, `resource`=3 (`jvm_heap`, `http_workers`, `db_connections`); 3 series | `max by (resource) (service_resource_utilization_ratio{service="inventory-service"})` |

Track absent/deleted Product responses separately with `sum(rate(business_rejections_total{service="inventory-service",reason="product_not_found"}[5m]))`. `initialStock` is not a current-stock metric.

## `product-service`

CP1 operation: `GET /api/v1/products/{id}/stock` returns current Stock owned by Product. Product also consumes `SaleRegistered` and publishes `StockUpdated`; event processing is documented design, not verified runtime behavior.

| Golden signal | Metric | Purpose | Type | Unit | Labels and series estimate | Dashboard PromQL (proposed) |
|---|---|---|---|---|---|---|
| Latency | `http_server_request_duration_seconds` | P95 Stock lookup latency | Histogram | seconds | `service`=1, `method`=1 (`GET`), `route`=1, `status_class`=3; 30 series incl. buckets, `_sum`, `_count` | `histogram_quantile(0.95, sum by (le) (rate(http_server_request_duration_seconds_bucket{service="product-service",route="/api/v1/products/{id}/stock"}[5m])))` |
| Traffic | `http_server_requests_total` | Stock lookup requests per second | Counter | requests/second via `rate` | `service`=1, `method`=1, `route`=1, `status_class`=3; 3 series | `sum(rate(http_server_requests_total{service="product-service",route="/api/v1/products/{id}/stock"}[5m]))` |
| Errors | `service_errors_total` | Fraction of Stock lookups failing for technical reasons; missing initialization/unavailable Stock is not quantity zero | Counter | errors/second via `rate`; ratio is unitless | `service`=1, `operation`=1 (`get_product_stock`), `error_type`=3; 3 series. Separate `business_rejections_total` has 1 documented `insufficient_stock` deduction reason; 1 additional series. | `sum(rate(service_errors_total{service="product-service",operation="get_product_stock"}[5m])) / clamp_min(sum(rate(http_server_requests_total{service="product-service",route="/api/v1/products/{id}/stock"}[5m])), 1e-9)` |
| Saturation | `service_resource_utilization_ratio` | Highest utilization of configured JVM heap, HTTP worker, or DB connection capacity | Gauge | ratio, 0–1 | `service`=1, `resource`=3 (`jvm_heap`, `http_workers`, `db_connections`); 3 series | `max by (resource) (service_resource_utilization_ratio{service="product-service"})` |

Track rejected stock deductions separately with `sum(rate(business_rejections_total{service="product-service",reason="insufficient_stock"}[5m]))`. Do not label these series with `product_id`; the documented `StockUpdated` event has a `reason`, but metrics should use only a bounded approved reason set.

## Cardinality estimate and metric semantics

The maximum for the CP1 route/method set is **162 series** across these four services: 120 latency (4 × 30), 12 traffic (4 × 3), 12 technical-error (4 × 3), 12 saturation (4 × 3), and 6 business-rejection (Sales 3, Customer 1, Inventory 1, Product 1). This estimates the listed metric families only; it excludes exporter/runtime metrics and any labels not specified here. Enforce the label enums and route templates at instrumentation time.

`http_server_requests_total` counts every completed HTTP request, including 4xx and 5xx. `service_errors_total` increments once for a request that fails technically, with a bounded `error_type`; do not increment it for a valid business rejection. `business_rejections_total` records rule outcomes separately with bounded operation/reason values. A Sales `422` for insufficient stock is a rejected sale, not proof of infrastructure failure. A `503` due to unavailable required product/stock information, timeout, or unavailable dependency is a technical failure. A `404` for an absent/deleted customer or Product is a domain result; whether product policy classifies it as a rejection or a not-found outcome must remain consistent across services.

The saturation gauge is a proposed normalized utilization of a resource's observed use divided by its configured capacity. Its bounds and actual capacities are not documented; export it only when both values are available, and do not interpret `0` as evidence of unused real infrastructure until instrumented. RabbitMQ queue depth/capacity is not included because concrete queues, bindings, and their capacity limits remain TBD.

## References

- [CP1 journey](./01-journey.md): exact service names, endpoints, ownership, and event flow.
- [CP2 log contract](./02-log-contract.md): technical errors versus business rejections and bounded inventory fields.
- [CP3 instrumentation harness](./03-instrumentation/src/main/java/com/bysellens/productservice/instrumentation/InstrumentationApplication.java) and [module POM](./03-instrumentation/pom.xml): confirms it logs requests and declares no Prometheus exporter or metric instrumentation.
- [Sales Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/README.md): sale validation, `/api/v1/sales`, and `SaleRegistered`/`StockUpdated` processing.
- [Customer Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/05-customer-service/README.md): active customer lookup, `404` outcomes, and PostgreSQL/Spring Boot target.
- [Inventory Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/04-inventory-service/README.md): Product ownership, `/api/v1/inventory/products/{id}`, and immutable `initialStock`.
- [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md): current Stock ownership, `/api/v1/products/{id}/stock`, and insufficient-stock rule.
- [System observability guide](https://github.com/code-corhuila/bysellens-docs/blob/main/13-operations/observability.md): observability concepts and proposed metrics direction; not evidence that these metric families have been implemented.
