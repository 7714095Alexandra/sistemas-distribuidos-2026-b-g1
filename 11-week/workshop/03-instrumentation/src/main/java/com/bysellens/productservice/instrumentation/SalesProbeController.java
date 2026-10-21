package com.bysellens.productservice.instrumentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
final class SalesProbeController {
    private final ProductStockClient productStockClient;

    SalesProbeController(ProductStockClient productStockClient) {
        this.productStockClient = productStockClient;
    }

    @PostMapping("/api/v1/sales")
    ResponseEntity<Void> registerSale(@RequestBody SaleRequest request) {
        if (request.items() != null && !request.items().isEmpty()) {
            productStockClient.getStock(request.items().get(0).productId());
        }
        // The harness performs no sale validation or persistence, even when the dependency replies successfully.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    record SaleRequest(List<SaleItem> items) { }

    record SaleItem(UUID productId) { }
}
