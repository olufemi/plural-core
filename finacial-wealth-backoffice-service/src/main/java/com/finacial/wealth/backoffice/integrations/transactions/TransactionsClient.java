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

  @GetMapping("/api/transactions/admin/reversals/summary")
  Map<String, Object> getReversalSummary();

  @GetMapping("/api/transactions/admin/reversals")
  Map<String, Object> getReversalCases(@RequestParam(required = false) String status);

  @PostMapping("/api/transactions/admin/reversals/{transactionId}/retry")
  Map<String, Object> retryReversal(@PathVariable("transactionId") String transactionId);

  @GetMapping("/api/transactions/admin/group-savings/contribution-payout-monitoring")
  Map<String, Object> getContributionPayoutMonitoring(
      @RequestParam(required = false) String period,
      @RequestParam(required = false) String fromDate,
      @RequestParam(required = false) String toDate,
      @RequestParam(required = false) Long groupId
  );

  @GetMapping("/api/transactions/admin/group-savings/slot-assignment-tracking")
  Map<String, Object> getSlotAssignmentTracking(
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) String status
  );
}
