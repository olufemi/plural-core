package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import com.finacial.wealth.backoffice.model.InterbankNameEnquiryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backoffice/interbank")
@RequiredArgsConstructor
public class BoInterbankController {

  private final TransactionsClient transactionsClient;

  @PostMapping("/name-enquiry")
  @PreAuthorize("hasAnyAuthority('interbank.nameEnquiry.execute','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
  public Map<String, Object> nameEnquiry(@RequestBody InterbankNameEnquiryRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request body is required");
    }
    return transactionsClient.interbankNameEnquiry(request.toDownstreamPayload());
  }

  @GetMapping("/reversals/summary")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
  public Map<String, Object> reversalSummary() {
    return transactionsClient.getReversalSummary();
  }

  @GetMapping("/reversals")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
  public Map<String, Object> reversalCases(@RequestParam(required = false) String status) {
    return transactionsClient.getReversalCases(status);
  }

}
