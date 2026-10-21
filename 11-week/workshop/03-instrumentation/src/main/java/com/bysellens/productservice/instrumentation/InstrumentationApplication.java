package com.bysellens.productservice.instrumentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class InstrumentationApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstrumentationApplication.class, args);
    }

    @Bean
    FilterRegistrationBean<TraceFilter> traceFilter(ObjectMapper json, MeterRegistry metrics,
                                                     @Value("${app.environment:local}") String environment) {
        return new FilterRegistrationBean<>(new TraceFilter(json, metrics, environment));
    }

    @RestController
    static class StockProbe {
        @GetMapping("/api/v1/products/{id}/stock")
        void stock(HttpServletResponse response) {
            response.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        }
    }

    static final class TraceFilter extends OncePerRequestFilter {
        private static final Pattern TRACEPARENT = Pattern.compile(
                "^00-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})$");
        private static final Pattern STOCK_ROUTE = Pattern.compile("/api/v1/products/[^/]+/stock");
        private final ObjectMapper json;
        private final MeterRegistry metrics;
        private final String environment;

        TraceFilter(ObjectMapper json, MeterRegistry metrics, String environment) {
            this.json = json;
            this.metrics = metrics;
            this.environment = environment;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            var trace = createTrace(request);
            var started = System.nanoTime();
            TraceContext.set(trace);
            response.setHeader("traceparent", "00-" + trace.traceId() + "-" + trace.spanId() + "-" + trace.flags());
            try {
                chain.doFilter(request, response);
            } finally {
                long elapsedNanos = System.nanoTime() - started;
                try {
                    recordMetrics(metrics, request, response, elapsedNanos);
                    if (routeFor(request.getRequestURI()) != null) {
                        logRequest(json, environment, request, response, trace, elapsedNanos / 1_000_000);
                    }
                } finally {
                    TraceContext.clear();
                }
            }
        }

        private static TraceContext.ServerSpan createTrace(HttpServletRequest request) {
            Enumeration<String> headers = request.getHeaders("traceparent");
            String value = headers != null && headers.hasMoreElements() ? headers.nextElement() : null;
            boolean duplicate = headers != null && headers.hasMoreElements();
            if (value != null && !duplicate) {
                Matcher match = TRACEPARENT.matcher(value);
                if (match.matches() && !match.group(1).matches("0{32}") && !match.group(2).matches("0{16}")) {
                    String spanId;
                    do {
                        spanId = randomHex(16);
                    } while (spanId.equals(match.group(2)));
                    return new TraceContext.ServerSpan(match.group(1), spanId, match.group(2), match.group(3),
                            UUID.randomUUID().toString());
                }
            }
            return new TraceContext.ServerSpan(randomHex(32), randomHex(16), null, "00",
                    UUID.randomUUID().toString());
        }

        private static String randomHex(int length) {
            String value;
            do {
                value = UUID.randomUUID().toString().replace("-", "");
            } while (value.substring(0, length).matches("0{" + length + "}"));
            return value.substring(0, length);
        }

        private static void recordMetrics(MeterRegistry metrics, HttpServletRequest request,
                                          HttpServletResponse response, long elapsedNanos) {
            Route route = routeFor(request.getRequestURI());
            if (route == null) {
                return;
            }
            String method = request.getMethod();
            String statusClass = (response.getStatus() / 100) + "xx";
            Tags tags = Tags.of("service", route.service(), "method", method,
                    "route", route.template(), "status_class", statusClass);
            Counter.builder("http.server.requests").tags(tags).register(metrics).increment();
            Timer.builder("http.server.request.duration")
                    .tags(tags)
                    .serviceLevelObjectives(Duration.ofMillis(50), Duration.ofMillis(100),
                            Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofSeconds(1),
                            Duration.ofMillis(2500), Duration.ofSeconds(5))
                    .register(metrics)
                    .record(elapsedNanos, TimeUnit.NANOSECONDS);
        }

        private static Route routeFor(String uri) {
            if ("/api/v1/sales".equals(uri)) {
                return new Route("sales-service", "/api/v1/sales");
            }
            if (STOCK_ROUTE.matcher(uri).matches()) {
                return new Route("product-service", "/api/v1/products/{id}/stock");
            }
            return null;
        }

        private static void logRequest(ObjectMapper json, String environment, HttpServletRequest request,
                                       HttpServletResponse response, TraceContext.ServerSpan trace,
                                       long durationMs) throws IOException {
            Route route = routeFor(request.getRequestURI());
            var record = new LinkedHashMap<String, Object>();
            record.put("timestamp", Instant.now().toString());
            record.put("level", response.getStatus() >= 500 ? "ERROR" : "INFO");
            record.put("service", route == null ? "product-service" : route.service());
            record.put("version", "0.1.0");
            record.put("environment", environment);
            record.put("trace_id", trace.traceId());
            record.put("span_id", trace.spanId());
            record.put("correlation_id", trace.correlationId());
            record.put("event", "http.request.completed");
            record.put("message", "HTTP request completed.");
            record.put("duration_ms", durationMs);
            record.put("http_method", request.getMethod());
            record.put("route", route == null ? "unmatched" : route.template());
            record.put("http_status", response.getStatus());
            System.out.println(json.writeValueAsString(record));
        }

        private record Route(String service, String template) { }
    }
}
