package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.FxPeerOfferUpdateRequest;
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
  public Map<String, Object> patchOffer(@PathVariable String offerId, @RequestBody FxPeerOfferUpdateRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request body is required");
    }
    return fxPeerClient.updateOffer(request.toDownstreamPayload(offerId));
  }

  @PostMapping("/update")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
  @Audited(action = "UPDATE_FXPEER_OFFER", entityType = "FXPEER_OFFER")
  public Map<String, Object> updateOffer(@RequestBody FxPeerOfferUpdateRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request body is required");
    }
    return fxPeerClient.updateOffer(request.toDownstreamPayload());
  }
}
