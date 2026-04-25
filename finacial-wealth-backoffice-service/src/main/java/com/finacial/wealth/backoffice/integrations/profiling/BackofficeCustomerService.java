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
}
