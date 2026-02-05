package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backoffice/fxpeer/offers")
@RequiredArgsConstructor
public class BoFxPeerOfferController {

  private final FxPeerExchangeClient fxPeerClient;

  @PostMapping("/update")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
  @Audited(action = "UPDATE_FXPEER_OFFER", entityType = "FXPEER_OFFER")
  public Map<String, Object> updateOffer(@RequestBody Map<String, Object> request) {
    return fxPeerClient.updateOffer(request);
  }
}
