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

  @PatchMapping("/{offerId}")
  @PreAuthorize("hasAnyAuthority('fx.offer.update','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS')")
  @Audited(action = "UPDATE_FXPEER_OFFER", entityType = "FXPEER_OFFER")
  public Map<String, Object> patchOffer(@PathVariable String offerId, @RequestBody Map<String, Object> request) {
    Map<String, Object> body = new java.util.LinkedHashMap<>();
    if (request != null) {
      body.putAll(request);
    }
    body.put("offerId", offerId);
    return fxPeerClient.updateOffer(body);
  }

  @PostMapping("/update")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
  @Audited(action = "UPDATE_FXPEER_OFFER", entityType = "FXPEER_OFFER")
  public Map<String, Object> updateOffer(@RequestBody Map<String, Object> request) {
    return fxPeerClient.updateOffer(request);
  }
}
