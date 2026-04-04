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
import com.finacial.wealth.backoffice.model.PagedResponse;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackofficeCustomerService {

    private final ProfilingClient profilingClient;

    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAllCustomers(
            int page,
            int size,
            String sort
    ) {
        return profilingClient.getAllCustomers(page, size, sort);
    }

    public ApiResponse<RegWalletInfoBackofficeResponse> getCustomerById(String id) {
        return profilingClient.getByCustomerId(id);
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
}
