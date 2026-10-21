package com.bysellens.productservice.instrumentation;

import java.util.UUID;

final class TraceContext {
    private static final ThreadLocal<ServerSpan> CURRENT = new ThreadLocal<>();

    private TraceContext() { }

    static void set(ServerSpan span) {
        CURRENT.set(span);
    }

    static ServerSpan current() {
        ServerSpan span = CURRENT.get();
        if (span == null) {
            throw new IllegalStateException("No server span is active for this request.");
        }
        return span;
    }

    static void clear() {
        CURRENT.remove();
    }

    static ClientSpan startClientSpan() {
        ServerSpan parent = current();
        String spanId;
        do {
            spanId = randomHex(16);
        } while (spanId.equals(parent.spanId()) || spanId.equals(parent.parentSpanId()));
        return new ClientSpan(parent.traceId(), spanId, parent.spanId(), parent.flags());
    }

    private static String randomHex(int length) {
        String value;
        do {
            value = UUID.randomUUID().toString().replace("-", "").substring(0, length);
        } while (value.matches("0{" + length + "}"));
        return value;
    }

    record ServerSpan(String traceId, String spanId, String parentSpanId, String flags, String correlationId) { }

    record ClientSpan(String traceId, String spanId, String parentSpanId, String flags) {
        String traceparent() {
            return "00-" + traceId + "-" + spanId + "-" + flags;
        }
    }
}
