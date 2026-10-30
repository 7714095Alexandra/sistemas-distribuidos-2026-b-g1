package demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DemoAuthorizationTest {
    private static final byte[] SECRET = "cp8-demo-only-secret".getBytes(StandardCharsets.UTF_8);
    private static final Map<String, String> PRODUCTS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/inventory/products", DemoAuthorizationTest::registerProduct);
        server.start();
        try {
            runCases(server.getAddress().getPort());
        } finally {
            server.stop(0);
        }
    }

    private static void runCases(int port) throws Exception {
        String code = "cp8-demo-" + UUID.randomUUID();
        String payload = "{\"code\":\"" + code + "\",\"name\":\"Demo product\",\"category\":\"security\","
                + "\"purchasePrice\":10,\"salePrice\":20,\"initialStock\":1}";
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> denied = send(client, port, "non-admin", payload);
        require(denied.statusCode() == 403, "non-ADMIN request", denied.statusCode());
        require(!PRODUCTS.containsKey(code), "rejected request must not persist");

        HttpResponse<String> allowed = send(client, port, "admin", payload);
        require(allowed.statusCode() == 201, "ADMIN control request", allowed.statusCode());
        require(PRODUCTS.containsKey(code), "ADMIN request must persist");
        System.out.println("PASS: non-ADMIN=403, no persistence; ADMIN=201, one persistence.");
    }

    private static HttpResponse<String> send(HttpClient client, int port, String role, String payload)
            throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/inventory/products"))
                .header("Authorization", "Bearer " + token(role))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void registerProduct(HttpExchange exchange) throws IOException {
        try {
            String role = verifiedRole(exchange.getRequestHeaders().getFirst("Authorization"));
            if (!"ADMIN".equals(role)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = body.replaceFirst(".*\"code\":\"([^\"]+)\".*", "$1");
            PRODUCTS.putIfAbsent(code, body);
            exchange.sendResponseHeaders(201, -1);
        } catch (SecurityException error) {
            exchange.sendResponseHeaders(401, -1);
        } finally {
            exchange.close();
        }
    }

    private static String verifiedRole(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("missing bearer token");
        }
        String[] parts = header.substring(7).split("\\.");
        if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]),
                decode(parts[2]))) {
            throw new SecurityException("invalid signature");
        }
        String claims = new String(decode(parts[1]), StandardCharsets.UTF_8);
        if (!claims.contains("\"exp\":" + Instant.now().getEpochSecond())
                && !claims.matches(".*\"exp\":" + "(1[7-9]|[2-9][0-9])[0-9]{8}.*")) {
            throw new SecurityException("expired token");
        }
        return claims.contains("\"role\":\"ADMIN\"") ? "ADMIN" : "USER";
    }

    private static String token(String role) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String claims = encode("{\"sub\":\"cp8-demo\",\"role\":\""
                + ("admin".equals(role) ? "ADMIN" : "USER") + "\",\"exp\":9999999999}");
        String input = header + "." + claims;
        return input + "." + encode(sign(input));
    }

    private static byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static void require(boolean condition, String caseName) {
        if (!condition) {
            throw new AssertionError(caseName + " failed");
        }
    }

    private static void require(boolean condition, String caseName, int status) {
        if (!condition) {
            throw new AssertionError(caseName + " failed with HTTP " + status);
        }
    }
}
