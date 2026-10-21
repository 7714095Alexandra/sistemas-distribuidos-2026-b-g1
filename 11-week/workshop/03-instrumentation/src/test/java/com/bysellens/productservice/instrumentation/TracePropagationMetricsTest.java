package com.bysellens.productservice.instrumentation;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TracePropagationMetricsTest {
    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String INCOMING_SPAN_ID = "00f067aa0ba902b7";
    private static final String TRACEPARENT =
            "00-" + TRACE_ID + "-" + INCOMING_SPAN_ID + "-01";
    private static final AtomicInteger downstreamCalls = new AtomicInteger();
    private static final AtomicReference<String> downstreamTraceparent = new AtomicReference<>();
    private static final AtomicReference<String> downstreamPath = new AtomicReference<>();
    private static HttpServer dependencyServer;

    @Autowired
    private TestRestTemplate http;

    @DynamicPropertySource
    static void configureDependency(DynamicPropertyRegistry properties) {
        try {
            dependencyServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            dependencyServer.createContext("/", exchange -> {
                downstreamCalls.incrementAndGet();
                downstreamTraceparent.set(exchange.getRequestHeaders().getFirst("traceparent"));
                downstreamPath.set(exchange.getRequestURI().getPath());
                exchange.sendResponseHeaders(501, -1);
                exchange.close();
            });
            dependencyServer.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start the local Product Service test dependency.", exception);
        }
        properties.add("app.dependencies.product-service.base-url",
                () -> "http://localhost:" + dependencyServer.getAddress().getPort());
    }

    @AfterAll
    static void stopDependency() {
        if (dependencyServer != null) {
            dependencyServer.stop(0);
        }
    }

    @Test
    void forwardsTraceContextToRealHttpClientAndExposesRequestMetrics() {
        downstreamTraceparent.set(null);
        downstreamPath.set(null);
        downstreamCalls.set(0);
        HttpHeaders headers = new HttpHeaders();
        headers.set("traceparent", TRACEPARENT);
        Map<String, Object> request = Map.of("items",
                List.of(Map.of("productId", "550e8400-e29b-41d4-a716-446655440001")));

        ResponseEntity<Void> response = http.exchange("/api/v1/sales", HttpMethod.POST,
                new HttpEntity<>(request, headers), Void.class);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
        assertEquals("/api/v1/products/550e8400-e29b-41d4-a716-446655440001/stock", downstreamPath.get());
        assertEquals(1, downstreamCalls.get());
        String outbound = downstreamTraceparent.get();
        assertNotNull(outbound, "The local dependency must capture the actual HTTP client's traceparent.");
        String[] inboundParts = TRACEPARENT.split("-");
        String[] outboundParts = outbound.split("-");
        String[] serverParts = response.getHeaders().getFirst("traceparent").split("-");
        assertEquals(TRACE_ID, outboundParts[1]);
        assertEquals(TRACE_ID, serverParts[1]);
        assertNotEquals(inboundParts[2], serverParts[2]);
        assertNotEquals(serverParts[2], outboundParts[2]);
        assertNotEquals(inboundParts[2], outboundParts[2]);
        assertEquals("01", outboundParts[3]);
        assertEquals("01", serverParts[3]);

        ResponseEntity<String> metrics = http.getForEntity("/metrics", String.class);
        assertEquals(HttpStatus.OK, metrics.getStatusCode(), metrics.getBody());
        String exposition = metrics.getBody();
        assertNotNull(exposition);
        assertTrue(exposition.contains("# TYPE http_server_requests_total counter"), exposition);
        assertTrue(exposition.contains("# TYPE http_server_request_duration_seconds histogram"), exposition);
        assertMetricSampleAtLeastOne(exposition, "http_server_requests_total",
                "service=\"sales-service\"", "method=\"POST\"", "route=\"/api/v1/sales\"",
                "status_class=\"5xx\"");
        assertMetricSampleAtLeastOne(exposition, "http_server_request_duration_seconds_count",
                "service=\"sales-service\"", "method=\"POST\"", "route=\"/api/v1/sales\"",
                "status_class=\"5xx\"");
        assertFalse(exposition.lines()
                .filter(line -> line.startsWith("http_server_requests_total{")
                        || line.startsWith("http_server_request_duration_seconds_"))
                .anyMatch(line -> line.contains("trace_id=") || line.contains("product_id=")));
    }

    @Test
    void clientSpanUsesActiveServerSpanAsParent() {
        TraceContext.set(new TraceContext.ServerSpan(TRACE_ID, INCOMING_SPAN_ID, "1122334455667788",
                "01", "test-correlation"));
        try {
            TraceContext.ClientSpan child = TraceContext.startClientSpan();
            assertEquals(TRACE_ID, child.traceId());
            assertEquals(INCOMING_SPAN_ID, child.parentSpanId());
            assertNotEquals(child.parentSpanId(), child.spanId());
        } finally {
            TraceContext.clear();
        }
    }

    private static void assertMetricSampleAtLeastOne(String exposition, String metricName, String... labels) {
        Pattern sample = Pattern.compile(Pattern.quote(metricName) + "\\{([^}]*)}\\s+([0-9.]+)");
        Matcher matcher = sample.matcher(exposition);
        while (matcher.find()) {
            String actualLabels = matcher.group(1);
            boolean matches = List.of(labels).stream().allMatch(actualLabels::contains);
            if (matches && Double.parseDouble(matcher.group(2)) >= 1.0) {
                return;
            }
        }
        fail("No " + metricName + " sample >= 1.0 with labels " + List.of(labels));
    }
}
