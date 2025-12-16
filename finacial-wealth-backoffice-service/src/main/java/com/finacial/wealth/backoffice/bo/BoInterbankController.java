package com.finacial.wealth.backoffice.bo;

import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bo/interbank")
@RequiredArgsConstructor
public class BoInterbankController {

  private final TransactionsClient transactionsClient;

  @PostMapping("/name-enquiry")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
  public Map<String, Object> nameEnquiry(@RequestBody Map<String, Object> request) {
    return transactionsClient.interbankNameEnquiry(request);
  }
}
