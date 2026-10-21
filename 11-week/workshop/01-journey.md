# CP1 — Critical Journey and Trace Propagation

## 1. Journey

**Name:** Register a sale and deduct stock  
**Objective:** Register a sale, validate its customer, product, and available stock, then follow the asynchronous stock deduction and its notification back to Sales.  
**Boundaries:** The journey starts when the React administrative frontend submits `POST /api/v1/sales` and ends when Sales receives `StockUpdated`. It includes synchronous customer, Inventory, and Product lookups, the sale registration, and the RabbitMQ events. It does not include product registration, manual stock adjustment, payment processing, or a guarantee that the asynchronous update has completed when the sale returns `201`.

This journey is documented as a target design, not verified running behavior. The named components and event contracts cross more than three components: React, `sales-service`, `customer-service`, `inventory-service`, `product-service`, and RabbitMQ. Current stock is owned by `product-service`; `inventory-service` owns product information and initial stock.

## 2. Hops

| Step | Sender | Receiver | Protocol | Endpoint or event | Mode | Data used |
|---|---|---|---|---|---|---|
| 1 | React administrative frontend | `sales-service` | HTTP/REST | `POST /api/v1/sales` | Synchronous | `customerId`, `paymentMethod`, and `items` with `productId`, `quantity`, and `unitPrice` |
| 2 | `sales-service` | `customer-service` | HTTP/REST | `GET /api/v1/customers/{id}` | Synchronous | Customer ID; confirm the customer is active |
| 3 | `sales-service` | `inventory-service` | HTTP/REST | `GET /api/v1/inventory/products/{id}` | Synchronous | Product ID; confirm an active product and read product information |
| 4 | `sales-service` | `product-service` | HTTP/REST | `GET /api/v1/products/{id}/stock` | Synchronous | Product ID; read current available quantity |
| 5 | `sales-service` | `sales-service` | Local persistence transaction | Save Sale and SaleItem | Synchronous | Validated customer and items; calculate subtotals and total |
| 6 | `sales-service` | RabbitMQ | RabbitMQ messaging; wire protocol unspecified | Publish `SaleRegistered` on `sales.sale-registered` | Asynchronous event flow | `saleId`, `customerId`, items (`productId`, `quantity`, `unitPrice`), `paymentMethod`, and `total` |
| 7 | RabbitMQ | `product-service` | RabbitMQ messaging; wire protocol unspecified | Deliver `SaleRegistered` from `sales.sale-registered` | Asynchronous | The event payload; deduplicate by `eventId` and deduct the sold quantities |
| 8 | `product-service` | `product-service` | Local stock persistence operation | Apply the stock deduction | Synchronous within the consumer | Product ID, requested quantity, and current Stock; reject a deduction above available quantity |
| 9 | `product-service` | RabbitMQ | RabbitMQ messaging; wire protocol unspecified | Publish `StockUpdated` on `products.stock-updated` | Asynchronous event flow | `productId`, `previousQuantity`, `newQuantity`, and `reason` (`SALE` in the documented sale example) |
| 10 | RabbitMQ | `sales-service` | RabbitMQ messaging; wire protocol unspecified | Deliver `StockUpdated` from `products.stock-updated` | Asynchronous | The event payload and its documented envelope; Sales tracks related processing |

The event contracts document `metadata.correlationId` and `causationId`; they do not define a RabbitMQ `traceparent` header. Correlation metadata is not treated here as a substitute for W3C trace context. The observability guide shows a generic HTTP `traceparent` example, but does not define its use on these service calls or on RabbitMQ messages. Trace propagation below is therefore an instrumentation proposal, not a claim about deployed behavior.

## 3. Sequence diagram

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Web as "React Admin Frontend"
    participant Sales as "sales-service"
    participant Customer as "customer-service"
    participant Inventory as "inventory-service"
    participant Product as "product-service"
    participant Broker as "RabbitMQ"

    Web->>Sales: HTTP POST /api/v1/sales; proposed traceparent header is unconfirmed
    Sales->>Customer: HTTP GET /api/v1/customers/{id}; proposed traceparent header is unconfirmed
    Customer-->>Sales: HTTP customer result; response trace context handling is unspecified
    Sales->>Inventory: HTTP GET /api/v1/inventory/products/{id}; proposed traceparent header is unconfirmed
    Inventory-->>Sales: HTTP product result; response trace context handling is unspecified
    Sales->>Product: HTTP GET /api/v1/products/{id}/stock; proposed traceparent header is unconfirmed
    Product-->>Sales: HTTP stock result; response trace context handling is unspecified
    Sales->>Sales: Persist Sale and SaleItem; in-process child span
    Sales-)Broker: Publish SaleRegistered; proposed traceparent message header is unconfirmed
    par HTTP response
        Sales-->>Web: HTTP 201 Sale; timing relative to stock processing is unspecified
    and Asynchronous stock processing
        Broker-)Product: Deliver SaleRegistered; proposed traceparent message header is unconfirmed
        Product->>Product: Deduct Stock; in-process child span
        Product-)Broker: Publish StockUpdated; proposed traceparent message header is unconfirmed
        Broker-)Sales: Deliver StockUpdated; proposed traceparent message header is unconfirmed
        Sales->>Sales: Track processing; in-process child span; completion rules pending
    end
```

The gateway is omitted because its adoption is pending. The HTTP and message `traceparent` labels describe an unconfirmed gap; if implemented, propagate the W3C context on HTTP and in message headers. Keep the existing event `correlationId` and `causationId` as separate business metadata. The parallel branch makes no claim about the exact response timing, which the sequence source leaves undecided.

## 4. Span tree

Illustrative target trace only. The trace ID and span IDs below are placeholders, not observed telemetry. The example starts a root at the Sales HTTP server span if no valid incoming context is available; the project sources do not specify root creation or propagation for this endpoint.

- `trace_id=11111111111111111111111111111111`
  - `span_id=0000000000000001` — `sales-service` server span: `POST /api/v1/sales` (root in this example)
    - `span_id=0000000000000002` — HTTP client span: validate active customer
    - `span_id=0000000000000003` — HTTP client span: read active product from Inventory
    - `span_id=0000000000000004` — HTTP client span: read Product stock
    - `span_id=0000000000000005` — local span: persist Sale and SaleItem
    - `span_id=0000000000000006` — producer span: publish `SaleRegistered`
      - **Asynchronous boundary:** RabbitMQ delivery carries the producer context in a `traceparent` message header only in the proposed instrumentation; the event docs do not specify it.
      - `span_id=0000000000000007` — `product-service` consumer span: process `SaleRegistered`
        - `span_id=0000000000000008` — local span: deduct Stock
        - `span_id=0000000000000009` — producer span: publish `StockUpdated`
          - **Asynchronous boundary:** RabbitMQ delivery carries the producer context in a `traceparent` message header only in the proposed instrumentation.
          - `span_id=000000000000000a` — `sales-service` consumer span: process `StockUpdated`

All spans in this proposed trace retain the same `trace_id` and use distinct `span_id` values. Without the proposed message-header propagation, the sources do not establish that the consumer spans can be joined to the same distributed trace. The documented `correlationId`/`causationId` envelope metadata can support business correlation but does not establish `traceparent` propagation.

## 5. Project references

- [Product vision](https://github.com/code-corhuila/bysellens-docs/blob/main/03-product/vision.md) and [problem framing](https://github.com/code-corhuila/bysellens-docs/blob/main/03-product/problem-framing.md): the administrator's product, inventory, customer, and sales scope and the need to keep stock updated.
- [Architecture overview](https://github.com/code-corhuila/bysellens-docs/blob/main/05-architecture/overview.md): React, the microservices, HTTP, RabbitMQ, and the documented sale flow.
- [Sales Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/README.md): ownership, validation sequence, `POST /api/v1/sales`, and the asynchronous processing limitation.
- [Product Service design](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/README.md) and [Product event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/03-product-service/events.md): stock ownership, SaleRegistered consumption, StockUpdated publication, event fields, and pending implementation.
- [Sales event contracts](https://github.com/code-corhuila/bysellens-docs/blob/main/09-microservices/services/06-sales-service/events.md): event topics, payloads, at-least-once delivery, and correlation metadata.
- [Sale sequence source](https://github.com/code-corhuila/bysellens-docs/blob/main/08-uml/diagrams/source/seq-register-sale.puml): planned cross-service flow and unresolved response timing, consistency, and recovery.
- [Observability guide](https://github.com/code-corhuila/bysellens-docs/blob/main/13-operations/observability.md): generic HTTP `traceparent` example; it does not specify propagation for this journey or RabbitMQ headers.

**Documentation discrepancy:** The architecture overview's service catalog lists Product as REST-only, while the more specific Product and Sales event contracts and the sale sequence document `SaleRegistered` consumption by Product and `StockUpdated` consumption by Sales. This checkpoint follows those explicit event contracts and flags the mismatch; it does not claim runtime implementation.
