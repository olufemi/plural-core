package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record FxPeerOfferUpdateRequest(
        BigDecimal rate,
        BigDecimal availableAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String status,
        Boolean active,
        String sourceCurrency,
        String targetCurrency,
        String reason,
        Map<String, Object> metadata
) {
    public FxPeerOfferUpdateRequest {
        if (metadata == null) {
            metadata = new LinkedHashMap<>();
        }
    }

    @JsonAnySetter
    public void addMetadata(String key, Object value) {
        if (key != null && value != null) {
            metadata.put(key, value);
        }
    }

    public Map<String, Object> toDownstreamPayload(String offerId) {
        requireReason();
        Map<String, Object> body = new LinkedHashMap<>();
        putIfNotNull(body, "offerId", offerId);
        putIfNotNull(body, "rate", rate);
        putIfNotNull(body, "availableAmount", availableAmount);
        putIfNotNull(body, "minAmount", minAmount);
        putIfNotNull(body, "maxAmount", maxAmount);
        putIfNotBlank(body, "status", status);
        putIfNotNull(body, "active", active);
        putIfNotBlank(body, "sourceCurrency", sourceCurrency);
        putIfNotBlank(body, "targetCurrency", targetCurrency);
        putIfNotBlank(body, "reason", reason);
        body.putAll(metadata);
        return body;
    }

    public Map<String, Object> toDownstreamPayload() {
        return toDownstreamPayload(null);
    }

    private void requireReason() {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
    }

    private void putIfNotNull(Map<String, Object> body, String key, Object value) {
        if (value != null) {
            body.put(key, value);
        }
    }

    private void putIfNotBlank(Map<String, Object> body, String key, String value) {
        if (value != null && !value.isBlank()) {
            body.put(key, value.trim());
        }
    }
}
