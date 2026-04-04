/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.service.profiling.bo.ApiResponse;
import com.finacial.wealth.api.profiling.service.profiling.bo.BlockUserRequest;
import com.finacial.wealth.api.profiling.service.profiling.bo.RegWalletInfoBackofficeResponse;

import com.finacial.wealth.api.profiling.service.profiling.bo.RegWalletInfoBackofficeServiceImpl;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class RegWalletInfoBackofficeController {

    private final RegWalletInfoBackofficeServiceImpl service;

    @GetMapping
    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAll(Pageable pageable) {
        return new ApiResponse<Page<RegWalletInfoBackofficeResponse>>(
                "00",
                "Records fetched successfully",
                service.getAll(pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<RegWalletInfoBackofficeResponse> getById(@PathVariable Long id) {
        return new ApiResponse<RegWalletInfoBackofficeResponse>(
                "00",
                "Record fetched successfully",
                service.getById(id)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<RegWalletInfoBackofficeResponse> getByCustomerId(@PathVariable String customerId) {
        return new ApiResponse<RegWalletInfoBackofficeResponse>(
                "00",
                "Record fetched successfully",
                service.getByCustomerId(customerId)
        );
    }

    @GetMapping("/uuid/{uuid}")
    public ApiResponse<RegWalletInfoBackofficeResponse> getByUuid(@PathVariable String uuid) {
        return new ApiResponse<RegWalletInfoBackofficeResponse>(
                "00",
                "Record fetched successfully",
                service.getByUuid(uuid)
        );
    }

    @GetMapping("/filter")
    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> filter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String isUserBlocked,
            Pageable pageable
    ) {
        return new ApiResponse<Page<RegWalletInfoBackofficeResponse>>(
                "00",
                "Records fetched successfully",
                service.filter(keyword, customerId, email, phoneNumber, accountNumber, isUserBlocked, pageable)
        );
    }

    @PatchMapping("/{id}/block")
    public ApiResponse<RegWalletInfoBackofficeResponse> blockUser(
            @PathVariable Long id,
            @RequestBody(required = false) BlockUserRequest request
    ) {
        return new ApiResponse<RegWalletInfoBackofficeResponse>(
                "00",
                "Customer blocked successfully",
                service.blockUser(id, request)
        );
    }

    @PatchMapping("/{id}/unblock")
    public ApiResponse<RegWalletInfoBackofficeResponse> unblockUser(
            @PathVariable Long id,
            @RequestBody(required = false) BlockUserRequest request
    ) {
        return new ApiResponse<RegWalletInfoBackofficeResponse>(
                "00",
                "Customer unblocked successfully",
                service.unblockUser(id, request)
        );
    }
}
