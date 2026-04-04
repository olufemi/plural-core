/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.profiling.BackofficeCustomerService;
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/backoffice/profiling")
@RequiredArgsConstructor
public class ProfilingManagementController {

    private final BackofficeCustomerService backofficeCustomerService;

    @GetMapping
    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        return backofficeCustomerService.getAllCustomers(page, size, sort);
    }

    @GetMapping("/{customerId}")
    public ApiResponse<RegWalletInfoBackofficeResponse> getCustomerById(
            @PathVariable("customerId") String customerId
    ) {
        return backofficeCustomerService.getCustomerById(customerId);
    }

    @PatchMapping("/{id}/block")
    public ApiResponse<RegWalletInfoBackofficeResponse> blockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    ) {
        return backofficeCustomerService.blockCustomer(id, request);
    }

    @PatchMapping("/{id}/unblock")
    public ApiResponse<RegWalletInfoBackofficeResponse> unblockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    ) {
        return backofficeCustomerService.unblockCustomer(id, request);
    }
}
