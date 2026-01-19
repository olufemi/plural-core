package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bo/group-savings")
@RequiredArgsConstructor
public class BoGroupSavingsController {

  private final TransactionsClient transactionsClient;

  @PostMapping("/delete")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  @Audited(action = "DELETE_GROUP_SAVING", entityType = "GROUP_SAVINGS")
  public Map<String, Object> deleteGroupSaving(@RequestBody Map<String, Object> request) {
    return transactionsClient.deleteGroupSaving(request);
  }
}
