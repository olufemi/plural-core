package com.finacial.wealth.backoffice.integrations.profiling;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "profiling-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface ProfilingClient {

  @GetMapping("/api/profiling/customers")
  Map<String, Object> searchCustomers(
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String walletNo
  );

  @GetMapping("/api/profiling/customers/{customerId}")
  Map<String, Object> getCustomer(@PathVariable String customerId);

  @PostMapping("/api/profiling/customers/{customerId}/freeze")
  Map<String, Object> freezeCustomer(@PathVariable String customerId);

  @PostMapping("/api/profiling/customers/{customerId}/unfreeze")
  Map<String, Object> unfreezeCustomer(@PathVariable String customerId);
}
