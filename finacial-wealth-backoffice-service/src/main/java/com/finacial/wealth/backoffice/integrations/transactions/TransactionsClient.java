package com.finacial.wealth.backoffice.integrations.transactions;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "transactions-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface TransactionsClient {

  @PostMapping("/api/transactions/group-savings/delete-group-saving")
  Map<String, Object> deleteGroupSaving(@RequestBody Map<String, Object> request);

  @PostMapping("/api/transactions/interbank/name-enquiry")
  Map<String, Object> interbankNameEnquiry(@RequestBody Map<String, Object> request);
}
