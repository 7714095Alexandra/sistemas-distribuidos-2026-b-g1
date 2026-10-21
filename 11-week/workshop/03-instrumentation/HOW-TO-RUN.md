# Product Service Instrumentation — First CP3 Part

## Scope and limitation

This standalone Java 17 / Spring Boot module demonstrates the W3C `traceparent` middleware and CP2 JSON log contract for the documented `product-service` stock route:

`GET /api/v1/products/{id}/stock`

The local workshop repository does not contain the BySellens service implementation. This module is an instrumentation harness, not a replacement or integration with the real Product Service. The route deliberately returns `501 Not Implemented`; it does not read Stock, connect to a database, or implement inventory behavior. `product-service` and the route are documented names from the CP1 journey.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Install and run

From this directory:

```powershell
mvn clean package
mvn spring-boot:run
```

The application listens on `http://localhost:8080`. Spring's banner and framework logs are disabled so the request record is emitted as one JSON line on standard output.

## Verify a valid incoming trace context

In another PowerShell window, send the documented Product stock route with a valid W3C version 00 context:

```powershell
curl.exe -i -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440001/stock
```

Expected behavior: the harness returns `501`, preserves the incoming trace ID and flags, creates a different service span ID, returns that context in the response `traceparent` header, and emits a JSON request log. These are expectations of this sample; consult actual console and HTTP output when running it.

## Verify a request without trace context

```powershell
curl.exe -i http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440001/stock
```

Expected behavior: the harness returns `501`, generates a new trace ID and service span ID, returns the generated context in the response header, and emits a JSON request log. Use the actual output from your run to confirm.

## Trace context and logging details

- Accepts a single lowercase W3C `traceparent` version `00` value with non-zero trace and parent IDs. Missing, malformed, unsupported, or duplicate headers start a new trace.
- Keeps a valid incoming trace ID and flags, generates a new span ID distinct from the incoming parent, and returns `00-{trace_id}-{service_span_id}-{flags}`.
- Creates a fresh `correlation_id` for each request. It is not treated as the W3C trace ID.
- Emits the CP2 required fields on each completed request. The route is normalized to `/api/v1/products/{id}/stock`; the raw URI and product identifier are not logged.
- No Stock data is available in this harness, so it does not fabricate `product_id`, `quantity`, `available_quantity`, or stock events.
- Do not send credentials or personal data for testing. The logger uses fixed messages and an allow-list of fields; it does not serialize request bodies or headers.

This is only the first CP3 part. It does not implement outbound context propagation, metrics, or propagation tests. No live BySellens behavior is asserted.
