/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.repo.CreateQuoteResLogRepo;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 *
 * @author olufemioshin
 */
@Service
public class QuoteLookupService {

    private final CreateQuoteResLogRepo repo;

    public QuoteLookupService(CreateQuoteResLogRepo repo) {
        this.repo = repo;
    }

    // Thread-safe formatter for: "Mon Sep 22 15:24:52 UTC 2025"
    private static final DateTimeFormatter LEGACY_DATE_FMT
            = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

    private static Long toEpochMillis(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            // parse respecting the zone (e.g., UTC)
            return ZonedDateTime.parse(s, LEGACY_DATE_FMT).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            // optional fallbacks: ISO-8601 or already-a-number
            try {
                return Instant.parse(s).toEpochMilli();
            } catch (Exception ignore) {
            }
            try {
                return Long.valueOf(s);
            } catch (Exception ignore) {
            }
            return null; // or throw if you prefer strictness
        }
    }
    //@Transactional(readOnly = true)

    public BaseResponse findAllPendingAcceptedForUserByEmail(String auth, int page, int size) throws UnsupportedEncodingException {
        int statusCode = 400;
        DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
        Page<CreateQuoteResLog> result = repo
                .findByEmailIgnoreCaseAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
                        getDecoded.emailAddress, "PENDING", "1", PageRequest.of(page, size)
                );

        List<Map<String, Object>> items = result.getContent().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("quoteId", row.getQuoteId());
            m.put("status", row.getStatus());
            m.put("isAccepted", row.getIsAccepted());
            m.put("createdAt", toEpochMillis(row.getCreatedAt()));
            m.put("validUntil", toEpochMillis(row.getValidUntil()));
            m.put("amount", row.getAmount());
            m.put("currencyCode", row.getCurrencyCode());
            m.put("countryCode", row.getCountryCode());
            m.put("paymentType", row.getPaymentType());
            // include blob if you want
            m.put("createQuoteResponse", row.getCreateQuoteResponse());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", getDecoded.emailAddress);
        data.put("page", result.getNumber());
        data.put("size", result.getSize());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("quotes", items);

        BaseResponse br = new BaseResponse(200, items.isEmpty() ? "No matching quotes" : "Success");
        br.setData(data);
        return br;
    }

    public BaseResponse findPendingAcceptedQuote(String quoteId) {
        Optional<CreateQuoteResLog> opt = repo
                .findFirstByQuoteIdAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
                        quoteId, "PENDING", "1"
                );

        if (!opt.isPresent()) {
            return new BaseResponse(404, "No matching quote found or conditions not met");
        }

        CreateQuoteResLog row = opt.get();

        // Build response payload (only what you want to expose)
        Map<String, Object> payload = new HashMap<>();
        payload.put("quoteId", row.getQuoteId());
        payload.put("status", row.getStatus());
        payload.put("isAccepted", row.getIsAccepted());
        payload.put("createdAt", row.getCreatedAt());
        payload.put("validUntil", row.getValidUntil());
        payload.put("amount", row.getAmount());
        payload.put("currencyCode", row.getCurrencyCode());
        payload.put("countryCode", row.getCountryCode());
        payload.put("paymentType", row.getPaymentType());

        // If you want to include the raw JSON string:
        payload.put("createQuoteResponse", row.getCreateQuoteResponse());

        BaseResponse br = new BaseResponse(200, "Success");
        br.setData(payload);
        return br;
    }
}
