package com.bysellens.productservice.instrumentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
final class ProductStockClient {
    private final RestClient restClient;

    ProductStockClient(RestClient.Builder builder,
                       @Value("${app.dependencies.product-service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    int getStock(UUID productId) {
        TraceContext.ClientSpan clientSpan = TraceContext.startClientSpan();
        return restClient.get()
                .uri("/api/v1/products/{id}/stock", productId)
                .header("traceparent", clientSpan.traceparent())
                .exchange((request, response) -> response.getStatusCode().value());
    }
}
