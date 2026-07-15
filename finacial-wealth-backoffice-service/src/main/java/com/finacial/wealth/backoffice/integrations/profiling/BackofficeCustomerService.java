/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.profiling;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BackofficeCustomerService {

    private final ProfilingClient profilingClient;
    private final FxPeerExchangeClient fxPeerExchangeClient;

    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAllCustomers(
            int page,
            int size,
            String sort
    ) {
        return profilingClient.getAllCustomers(page, size, sort);
    }

    public ApiResponse<RegWalletInfoBackofficeResponse> getCustomerById(Long id) {
        return profilingClient.getById(id);
    }

    public ApiResponse<RegWalletInfoBackofficeResponse> blockCustomer(
            Long id,
            BlockUserRequest request
    ) {
        return profilingClient.blockUser(id, request);
    }

    public ApiResponse<RegWalletInfoBackofficeResponse> unblockCustomer(
            Long id,
            BlockUserRequest request
    ) {
        return profilingClient.unblockUser(id, request);
    }

    public Map<String, Object> getCustomerInvestmentSummary(Long id) {
        RegWalletInfoBackofficeResponse customer = resolveCustomer(id);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("customer", customer);
        summary.put("orders", fxPeerExchangeClient.getCustomerOrders(customer.getEmail(), null, null, 0, 20));
        summary.put("liquidations", fxPeerExchangeClient.getCustomerLiquidations(customer.getEmail(), null, 0, 20));
        summary.put("positions", fxPeerExchangeClient.getCustomerPositions(customer.getEmail(), 0, 20));
        return summary;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCustomer360(Long id) {
        RegWalletInfoBackofficeResponse customer = resolveCustomer(id);
        ApiResponse<Map<String, Object>> sourceResponse = profilingClient.getCustomer360(id);

        Map<String, Object> customer360 = new LinkedHashMap<>();
        if (sourceResponse != null && sourceResponse.getData() != null) {
            customer360.putAll(sourceResponse.getData());
        } else {
            customer360.put("profile", customer);
        }

        Map<String, Object> investment = new LinkedHashMap<>();
        investment.put("orders", safeInvestmentSection(
                "orders",
                () -> fxPeerExchangeClient.getCustomerOrders(customer.getEmail(), null, null, 0, 10)
        ));
        investment.put("liquidations", safeInvestmentSection(
                "liquidations",
                () -> fxPeerExchangeClient.getCustomerLiquidations(customer.getEmail(), null, 0, 10)
        ));
        investment.put("positions", safeInvestmentSection(
                "positions",
                () -> fxPeerExchangeClient.getCustomerPositions(customer.getEmail(), 0, 10)
        ));
        customer360.put("investment", investment);

        Map<String, Object> coverage = customer360.get("coverage") instanceof Map
                ? (Map<String, Object>) customer360.get("coverage")
                : new LinkedHashMap<>();
        coverage.put("backofficeAggregation", "profile, accounts, KYC summary, device summary, referral summary, access summary, and investment summary");
        customer360.put("coverage", coverage);
        return customer360;
    }

    public Map<String, Object> getCustomerInvestmentOrders(
            Long id,
            String type,
            String status,
            Integer page,
            Integer size
    ) {
        RegWalletInfoBackofficeResponse customer = resolveCustomer(id);
        return fxPeerExchangeClient.getCustomerOrders(customer.getEmail(), type, status, safePage(page), safeSize(size));
    }

    public Map<String, Object> getCustomerLiquidations(
            Long id,
            String status,
            Integer page,
            Integer size
    ) {
        RegWalletInfoBackofficeResponse customer = resolveCustomer(id);
        return fxPeerExchangeClient.getCustomerLiquidations(customer.getEmail(), status, safePage(page), safeSize(size));
    }

    public Map<String, Object> getCustomerInvestmentPositions(
            Long id,
            Integer page,
            Integer size
    ) {
        RegWalletInfoBackofficeResponse customer = resolveCustomer(id);
        return fxPeerExchangeClient.getCustomerPositions(customer.getEmail(), safePage(page), safeSize(size));
    }

    private RegWalletInfoBackofficeResponse resolveCustomer(Long id) {
        ApiResponse<RegWalletInfoBackofficeResponse> response = profilingClient.getById(id);
        if (response == null || response.getData() == null) {
            throw new IllegalArgumentException("Customer not found for id: " + id);
        }
        RegWalletInfoBackofficeResponse customer = response.getData();
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Customer profile does not have an email address.");
        }
        return customer;
    }

    private int safePage(Integer page) {
        return page == null ? 0 : Math.max(page, 0);
    }

    private int safeSize(Integer size) {
        return size == null ? 20 : Math.max(size, 1);
    }

    private Map<String, Object> safeInvestmentSection(String section, InvestmentCall call) {
        try {
            return call.execute();
        } catch (Exception ex) {
            Map<String, Object> unavailable = new LinkedHashMap<>();
            unavailable.put("available", false);
            unavailable.put("section", section);
            unavailable.put("message", "Investment downstream data is currently unavailable.");
            return unavailable;
        }
    }

    @FunctionalInterface
    private interface InvestmentCall {
        Map<String, Object> execute();
    }
}
