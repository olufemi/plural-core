package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;

public class SochitelSignedClient {

    private static final Logger log = LoggerFactory.getLogger(SochitelSignedClient.class);

    private static final String SIGNED_HEADERS_LIST = "(request-target) host date nonce digest";
    // RFC-2822 style with explicit +0000 offset (vendor examples use +0000, not GMT)
    private static final DateTimeFormatter RFC2822_0000 = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss +0000", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    private final HttpClient http;
    private final URI baseUri;                   // e.g. https://clientportal.sochitel.com or .../api
    private final String keyId;                  // Sochitel UserID
    private final PrivateKey privateKey;         // RSA (PKCS#8)

    /**
     * Spec requires Authorization: Signature ... by default
     */
    public SochitelSignedClient(URI baseUri, String keyId, PrivateKey privateKey) {
        this.http = HttpClient.newHttpClient();
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
    }

    /* ===================== Public API ===================== */
    /**
     * GET {base}{pathAndQuery} — includes Digest (empty body) per spec
     */
    public HttpResponse<String> get(String pathAndQuery) throws IOException, InterruptedException {
        final String methodLower = "get";
        final String payload = ""; // GET has empty body

        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        final String date = RFC2822_0000.format(now);
        final String nonce = generateNonce(now);
        final String digest = digestHeaderValue(payload); // ALWAYS include digest

        URI target = resolveTarget(pathAndQuery);
        String hostValue = computeHostValue(target);
        String requestTarget = buildRequestTarget(methodLower, target);

        String signingString = canonicalToSign(requestTarget, hostValue, date, nonce, digest);
        String signatureB64 = signRsaSha256(signingString, privateKey);
        String authorizationHeader = buildAuthorizationHeader(keyId, SIGNED_HEADERS_LIST, signatureB64);

        // Logs (safe: no secrets)
        log.info("[Sochitel] GET {}", target);
        log.debug("[Sochitel] host='{}' request-target='{}'", hostValue, requestTarget);
        log.debug("[Sochitel] signingString:\n{}", signingString);

        HttpRequest req = HttpRequest.newBuilder(target)
                .header("Accept", "application/json")
                .header("Date", date)
                .header("Nonce", nonce)
                .header("Digest", digest)
                .header("Authorization", authorizationHeader)
                .GET()
                .build();

        // Never set "Host" manually; JDK sets it from URI
        if (req.headers().firstValue("Host").isPresent()) {
            throw new IllegalStateException("Do not set the Host header manually.");
        }

        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        log.info("[Sochitel] status={} bodyLength={}", resp.statusCode(),
                resp.body() == null ? 0 : resp.body().length());
        return resp;
    }

    /**
     * POST JSON to {base}{pathAndQuery}
     */
    public HttpResponse<String> postJson(String pathAndQuery, String jsonBody) throws IOException, InterruptedException {
        final String methodLower = "post";
        final String payload = (jsonBody == null) ? "" : jsonBody;

        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        final String date = RFC2822_0000.format(now);
        final String nonce = generateNonce(now);
        final String digest = digestHeaderValue(payload); // digest of body

        URI target = resolveTarget(pathAndQuery);
        String hostValue = computeHostValue(target);
        String requestTarget = buildRequestTarget(methodLower, target);

        String signingString = canonicalToSign(requestTarget, hostValue, date, nonce, digest);
        String signatureB64 = signRsaSha256(signingString, privateKey);
        String authorizationHeader = buildAuthorizationHeader(keyId, SIGNED_HEADERS_LIST, signatureB64);

        log.info("[Sochitel] POST {}", target);
        log.debug("[Sochitel] host='{}' request-target='{}'", hostValue, requestTarget);
        log.debug("[Sochitel] signingString:\n{}", signingString);

        HttpRequest req = HttpRequest.newBuilder(target)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Date", date)
                .header("Nonce", nonce)
                .header("Digest", digest)
                .header("Authorization", authorizationHeader)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        if (req.headers().firstValue("Host").isPresent()) {
            throw new IllegalStateException("Do not set the Host header manually.");
        }

        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        log.info("[Sochitel] status={} bodyLength={}", resp.statusCode(),
                resp.body() == null ? 0 : resp.body().length());
        return resp;
    }

    /**
     * Convenience GET with query params (auto-encodes)
     */
    public HttpResponse<String> get(String path, Map<String, String> query) throws IOException, InterruptedException {
        return get(pathWithQuery(path, query));
    }

    /**
     * Convenience POST JSON with query params (auto-encodes)
     */
    public HttpResponse<String> postJson(String path, Map<String, String> query, String jsonBody) throws IOException, InterruptedException {
        return postJson(pathWithQuery(path, query), jsonBody);
    }

    /* ===================== Helpers ===================== */
    /**
     * Robust resolver: accepts absolute URLs; else joins base + relative path
     * without dropping base path prefix (e.g., /api)
     */
    private URI resolveTarget(String pathAndQuery) {
        if (pathAndQuery == null || pathAndQuery.isEmpty()) {
            return baseUri;
        }
        try {
            URI maybeAbs = URI.create(pathAndQuery);
            if (maybeAbs.isAbsolute()) {
                return maybeAbs;
            }
        } catch (IllegalArgumentException ignore) {
            /* continue join */ }
        String b = baseUri.toString().replaceAll("/+$", "");
        String p = pathAndQuery.replaceAll("^/+", "");
        return URI.create(b + "/" + p);
    }

    /**
     * (request-target) = "<lowercase-method> <path[?query]>"
     */
    private static String buildRequestTarget(String methodLower, URI uri) {
        String path = (uri.getRawPath() == null || uri.getRawPath().isEmpty()) ? "/" : uri.getRawPath();
        String query = (uri.getRawQuery() == null || uri.getRawQuery().isEmpty()) ? "" : "?" + uri.getRawQuery();
        return methodLower + " " + path + query;
    }

    /**
     * host[:port] if non-default
     */
    private static String computeHostValue(URI uri) {
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        int port = uri.getPort();
        boolean includePort = (port != -1) && !((https && port == 443) || (!https && port == 80));
        return includePort ? (uri.getHost() + ":" + port) : uri.getHost();
    }

    /**
     * SHA-256 of payload -> "SHA-256=<base64>"
     */
    private static String digestHeaderValue(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            return "SHA-256=" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Digest calculation failed", e);
        }
    }

    /**
     * Canonical string to sign; order must match headers="(request-target) host
     * date nonce digest" and **NO trailing newline**
     */
    private static String canonicalToSign(String requestTarget, String host, String date, String nonce, String digest) {
        // Important: NO trailing newline at the end.
        return "(request-target): " + requestTarget + "\n"
                + "host: " + host + "\n"
                + "date: " + date + "\n"
                + "nonce: " + nonce + "\n"
                + "digest: " + digest;
    }

    /**
     * RSA PKCS#1 v1.5 with SHA-256, as required by "rsa-sha256"
     */
    private static String signRsaSha256(String data, PrivateKey key) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA"); // PKCS#1 v1.5
            sig.initSign(key);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }

    /**
     * Authorization header per spec
     */
    private static String buildAuthorizationHeader(String keyId, String headersList, String signatureB64) {
        return "Signature keyId=\"" + escape(keyId) + "\","
                + " algorithm=\"rsa-sha256\","
                + " headers=\"" + headersList + "\","
                + " signature=\"" + signatureB64 + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Build path?a=b&c=d with proper URL encoding
     */
    private static String pathWithQuery(String path, Map<String, String> q) {
        String p = (path == null || path.isEmpty()) ? "/" : path.replaceAll("^/+", "");
        String base = "/" + p;
        if (q == null || q.isEmpty()) {
            return base;
        }
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> e : q.entrySet()) {
            String k = url(e.getKey());
            String v = url(e.getValue());
            sj.add(k + "=" + v);
        }
        return base + "?" + sj;
    }

    private static String url(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    /* ===================== Nonce & Key Loader ===================== */
    /**
     * 18-digit nonce: weekday (Mon=1…Sun=7) + 17 random digits
     */
    private static String generateNonce(ZonedDateTime nowUtc) {
        int day = nowUtc.getDayOfWeek().getValue(); // Monday=1 … Sunday=7
        StringBuilder sb = new StringBuilder(18);
        sb.append(day);
        Random r = new Random();
        for (int i = 0; i < 17; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Load PKCS#8 RSA private key from PEM (-----BEGIN PRIVATE KEY----- ... )
     */
    public static PrivateKey loadPkcs8PrivateKeyFromPem(String pem) {
        try {
            String sanitized = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(sanitized);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse PKCS#8 private key PEM", e);
        }
    }

    /* ===================== Quick demo (optional) ===================== */
    public static void main(String[] args) throws Exception {
        String pem = """
                -----BEGIN PRIVATE KEY-----
                YOUR_PKCS8_PRIVATE_KEY_BASE64_LINES_HERE
                -----END PRIVATE KEY-----
                """;
        String keyId = "123456789";
        // Example base:
        // URI base = URI.create("https://clientportal.sochitel.com");
        // then call get("/api/operators") OR get("api/operators");
        URI base = URI.create("https://test.example.com/api/");

        PrivateKey key = loadPkcs8PrivateKeyFromPem(pem);
        SochitelSignedClient client = new SochitelSignedClient(base, keyId, key);

        HttpResponse<String> getResp = client.get("operators");
        System.out.println("GET status: " + getResp.statusCode());
        System.out.println(getResp.body());
    }
}
