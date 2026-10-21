# BySellens Observability Instrumentation Harness

## Scope and limitation

This standalone Java 17 / Spring Boot harness demonstrates the CP1 `sales-service` to `product-service` HTTP hop:

- `POST /api/v1/sales` is a caller probe. It reads the first item's documented `productId` and makes one HTTP GET to the configured Product Service.
- The outbound route is the documented `GET /api/v1/products/{id}/stock`.
- The retained Product stock route in this harness still responds `501 Not Implemented`.
- The caller probe also responds `501` after making the downstream call. It does not validate a sale, read real Stock, persist data, or publish events, regardless of the dependency response.

The workshop repository does not contain the real BySellens microservice implementations. Therefore the harness is not the inventory system and its results are not production measurements. The automated test starts a local JDK HTTP server as a simulated Product Service dependency; it captures the actual HTTP client's request and returns `501`.

## Requirements and build

- Java 17 or newer
- Maven 3.9 or newer

From this directory:

```powershell
mvn clean verify
```

The integration tests require no Docker daemon. They start the Spring Boot application on a random port and the local simulated dependency on an ephemeral port. Tests verify the known incoming trace ID on the real outbound HTTP call, distinct parent/child span IDs, and both exported metric families with recorded request samples.

## Configure and run

Set the Product Service base URL externally. The local default is `http://localhost:8081`; point it at a running implementation if available:

```powershell
$env:PRODUCT_SERVICE_BASE_URL = "http://localhost:8081"
mvn spring-boot:run
```

The harness listens on `http://localhost:8080`. Spring framework logs are disabled; completed journey-route requests are printed as one CP2-compatible JSON line to standard output. If the configured dependency is unreachable, the HTTP client error is not converted into a successful response.

### Send the documented sale request with a trace context

In another PowerShell window:

```powershell
$body = '{"items":[{"productId":"550e8400-e29b-41d4-a716-446655440001"}]}'
curl.exe -i -X POST `
  -H "Content-Type: application/json" `
  -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" `
  --data $body http://localhost:8080/api/v1/sales
```

The incoming HTTP server span preserves the trace ID and creates its own span ID. The outbound client span is a child of that server span, has a different span ID, and is sent in the dependency request's `traceparent` header. The response header contains the harness's server span context. This probe uses only the first item and deliberately returns `501`; it does not represent full sale processing.

### Check the existing Product stock stub without a trace context

```powershell
curl.exe -i http://localhost:8080/api/v1/products/550e8400-e29b-41d4-a716-446655440001/stock
```

This retained route generates a new context when no valid incoming header is supplied and returns `501`. It does not query Stock.

## Metrics

The Prometheus text exposition is available at `http://localhost:8080/metrics`:

```powershell
curl.exe http://localhost:8080/metrics |
  Select-String "http_server_requests_total|http_server_request_duration_seconds"
```

- `http_server_requests_total` is a counter partitioned by bounded `service`, `method`, `route`, and `status_class` (`2xx`, `4xx`, `5xx`) labels.
- `http_server_request_duration_seconds` is a histogram in seconds with finite buckets `0.05`, `0.1`, `0.25`, `0.5`, `1`, `2.5`, and `5`, plus the Prometheus `+Inf` bucket, `_sum`, and `_count`.
- Trace IDs and product IDs are not metric labels. `/metrics` itself and unmatched routes are excluded from the journey metric families.
- These are harness measurements only; a `501` response is counted as a `5xx` result and says nothing about real inventory performance or saturation.

## Automated test

Run just the integration test with:

```powershell
mvn -Dtest=TracePropagationMetricsTest test
```

`TracePropagationMetricsTest` sends a known W3C `traceparent` to the harness, uses the actual `RestClient` outbound call, and captures the received header and route on a local HTTP server. It checks trace ID preservation, distinct span IDs, the server-to-client span parent relationship, and metric exposition/count samples for the `501` request. The stub's `501` is intentional and is never treated as a successful inventory operation.

## Evidence from the verified run

`mvn clean verify` completed with `BUILD SUCCESS`; Surefire reported `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`. One actual CP2 JSON line emitted by that test run was:

```json
{"timestamp":"2026-10-21T05:09:54.012392900Z","level":"ERROR","service":"sales-service","version":"0.1.0","environment":"local","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"2a8e78bb07d14ba2","correlation_id":"831f3cd7-4406-459f-976f-63e3fed749c9","event":"http.request.completed","message":"HTTP request completed.","duration_ms":150,"http_method":"POST","route":"/api/v1/sales","http_status":501}
```

The timestamp and generated span/correlation IDs above are captured test output, not stable values. No real Product Service was available or called during the test.
