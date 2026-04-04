package com.finacial.wealth.backoffice.integrations.profiling;

import com.finacial.wealth.backoffice.config.FeignConfig;
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.data.domain.Page;

@FeignClient(
        name = "profiling-service",
        contextId = "profilingClient",
        path = "/profiles",
        configuration = FeignConfig.class
)
public interface ProfilingClient {

    @GetMapping
    ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAllCustomers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sort", required = false) String sort
    );

    @GetMapping("/{id}")
    ApiResponse<RegWalletInfoBackofficeResponse> getById(@PathVariable("id") Long id);

    @GetMapping("/customer/{customerId}")
    ApiResponse<RegWalletInfoBackofficeResponse> getByCustomerId(@PathVariable("customerId") String customerId);

    @GetMapping("/uuid/{uuid}")
    ApiResponse<RegWalletInfoBackofficeResponse> getByUuid(@PathVariable("uuid") String uuid);

    @PatchMapping("/{id}/block")
    ApiResponse<RegWalletInfoBackofficeResponse> blockUser(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    );

    @PatchMapping("/{id}/unblock")
    ApiResponse<RegWalletInfoBackofficeResponse> unblockUser(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    );

//    @GetMapping("/api/profiling/customers")
//    Map<String, Object> searchCustomers(
//            @RequestParam(required = false) String email,
//            @RequestParam(required = false) String phone,
//            @RequestParam(required = false) String walletNo
//    );
//
//    @GetMapping("/api/profiling/customers/{customerId}")
//    Map<String, Object> getCustomer(@PathVariable String customerId);
//
//    @PostMapping("/api/profiling/customers/{customerId}/freeze")
//    Map<String, Object> freezeCustomer(@PathVariable String customerId);
//
//    @PostMapping("/api/profiling/customers/{customerId}/unfreeze")
//    Map<String, Object> unfreezeCustomer(@PathVariable String customerId);
}
