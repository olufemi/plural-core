package com.finacial.wealth.backoffice.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record InterbankNameEnquiryRequest(
        String bankCode,
        String accountNumber,
        String country
) {
    public Map<String, Object> toDownstreamPayload() {
        require(bankCode, "bankCode");
        require(accountNumber, "accountNumber");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bankCode", bankCode.trim());
        body.put("accountNumber", accountNumber.trim());
        if (country != null && !country.isBlank()) {
            body.put("country", country.trim().toUpperCase());
        }
        return body;
    }

    private void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
