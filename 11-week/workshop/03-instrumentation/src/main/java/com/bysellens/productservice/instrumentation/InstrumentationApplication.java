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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

@SpringBootApplication
public class InstrumentationApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstrumentationApplication.class, args);
    }

    @Bean
    FilterRegistrationBean<TraceFilter> traceFilter(ObjectMapper json, @Value("${app.environment:local}") String environment) {
        return new FilterRegistrationBean<>(new TraceFilter(json, environment));
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
        private static final ThreadLocal<Trace> CURRENT = new ThreadLocal<>();
        private final ObjectMapper json;
        private final String environment;

        TraceFilter(ObjectMapper json, String environment) {
            this.json = json;
            this.environment = environment;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            var trace = createTrace(request);
            var started = System.nanoTime();
            CURRENT.set(trace);
            response.setHeader("traceparent", "00-" + trace.traceId + "-" + trace.spanId + "-" + trace.flags);
            try {
                chain.doFilter(request, response);
            } finally {
                logRequest(json, environment, request, response, trace, (System.nanoTime() - started) / 1_000_000);
                CURRENT.remove();
            }
        }

        private static Trace createTrace(HttpServletRequest request) {
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
                    return new Trace(match.group(1), spanId, match.group(3), UUID.randomUUID().toString());
                }
            }
            return new Trace(randomHex(32), randomHex(16), "00", UUID.randomUUID().toString());
        }

        private static String randomHex(int length) {
            String value;
            do {
                value = UUID.randomUUID().toString().replace("-", "");
            } while (value.substring(0, length).matches("0{" + length + "}"));
            return value.substring(0, length);
        }

        private static void logRequest(ObjectMapper json, String environment, HttpServletRequest request,
                                       HttpServletResponse response, Trace trace, long durationMs) throws IOException {
            String route = STOCK_ROUTE.matcher(request.getRequestURI()).matches()
                    ? "/api/v1/products/{id}/stock" : "unmatched";
            var record = new LinkedHashMap<String, Object>();
            record.put("timestamp", Instant.now().toString());
            record.put("level", response.getStatus() >= 500 ? "ERROR" : "INFO");
            record.put("service", "product-service");
            record.put("version", "0.1.0");
            record.put("environment", environment);
            record.put("trace_id", trace.traceId);
            record.put("span_id", trace.spanId);
            record.put("correlation_id", trace.correlationId);
            record.put("event", "http.request.completed");
            record.put("message", "HTTP request completed.");
            record.put("duration_ms", durationMs);
            record.put("http_method", request.getMethod());
            record.put("route", route);
            record.put("http_status", response.getStatus());
            System.out.println(json.writeValueAsString(record));
        }

        private record Trace(String traceId, String spanId, String flags, String correlationId) { }
    }
}
