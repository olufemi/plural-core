package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping({"/backoffice/p2p-fx", "/bo/backoffice/p2p-fx"})
@RequiredArgsConstructor
public class BoP2pFxOpsController {

    private final FxPeerExchangeClient fxPeerClient;

    @GetMapping("/market/offers")
    @PreAuthorize("hasAnyAuthority('fx.offer.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> browseMarketOffers(
            @RequestParam(required = false) String ccySell,
            @RequestParam(required = false) String ccyRecv,
            @RequestParam(required = false) BigDecimal rateMin,
            @RequestParam(required = false) BigDecimal rateMax,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "bestRate") String sort
    ) {
        return fxPeerClient.browseMarketOffers(ccySell, ccyRecv, rateMin, rateMax, amountMin, page, size, sort);
    }

    @GetMapping("/seller-offers")
    @PreAuthorize("hasAnyAuthority('fx.offer.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> listSellerOffers(
            @RequestParam Long sellerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return fxPeerClient.listSellerOffers(sellerId, status, page, size);
    }

    @GetMapping("/seller-offers/{offerId}")
    @PreAuthorize("hasAnyAuthority('fx.offer.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getSellerOffer(
            @RequestParam Long sellerId,
            @PathVariable Long offerId
    ) {
        return fxPeerClient.getOffer(sellerId, offerId);
    }

    @PatchMapping("/seller-offers/{offerId}/rate")
    @PreAuthorize("hasAnyAuthority('fx.offer.update','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS')")
    @Audited(action = "UPDATE_P2P_FX_OFFER_RATE", entityType = "P2P_FX_OFFER")
    public Map<String, Object> updateOfferRate(
            @RequestParam Long sellerId,
            @PathVariable Long offerId,
            @RequestParam BigDecimal rate,
            @RequestParam String reason
    ) {
        requireReason(reason);
        return fxPeerClient.updateOfferRate(sellerId, offerId, rate);
    }

    @PostMapping("/seller-offers/{offerId}/cancel")
    @PreAuthorize("hasAnyAuthority('fx.offer.update','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS')")
    @Audited(action = "CANCEL_P2P_FX_OFFER", entityType = "P2P_FX_OFFER")
    public Map<String, Object> cancelOffer(
            @RequestParam Long sellerId,
            @PathVariable Long offerId,
            @RequestParam String reason
    ) {
        requireReason(reason);
        return fxPeerClient.cancelOffer(sellerId, offerId);
    }

    @PostMapping("/orders/{orderId}/escrow/init")
    @PreAuthorize("hasAnyAuthority('fx.escrow.manage','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    @Audited(action = "INIT_P2P_FX_ESCROW", entityType = "P2P_FX_ESCROW")
    public Map<String, Object> initEscrow(@PathVariable Long orderId, @RequestParam String reason) {
        requireReason(reason);
        return fxPeerClient.initEscrow(orderId);
    }

    @GetMapping("/escrows/{escrowId}")
    @PreAuthorize("hasAnyAuthority('fx.escrow.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getEscrow(@PathVariable Long escrowId) {
        return fxPeerClient.getEscrow(escrowId);
    }

    @PostMapping("/escrows/{escrowId}/fund/buyer")
    @PreAuthorize("hasAnyAuthority('fx.escrow.manage','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
    @Audited(action = "FUND_P2P_FX_ESCROW_BUYER", entityType = "P2P_FX_ESCROW")
    public Map<String, Object> fundBuyer(
            @PathVariable Long escrowId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam String reason
    ) {
        requireReason(reason);
        fxPeerClient.fundEscrowBuyer(escrowId, idempotencyKey);
        return success("fundBuyer", escrowId);
    }

    @PostMapping("/escrows/{escrowId}/fund/seller")
    @PreAuthorize("hasAnyAuthority('fx.escrow.manage','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
    @Audited(action = "FUND_P2P_FX_ESCROW_SELLER", entityType = "P2P_FX_ESCROW")
    public Map<String, Object> fundSeller(
            @PathVariable Long escrowId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam String reason
    ) {
        requireReason(reason);
        fxPeerClient.fundEscrowSeller(escrowId, idempotencyKey);
        return success("fundSeller", escrowId);
    }

    @PostMapping("/escrows/{escrowId}/release/buyer")
    @PreAuthorize("hasAnyAuthority('fx.escrow.manage','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
    @Audited(action = "RELEASE_P2P_FX_ESCROW_BUYER", entityType = "P2P_FX_ESCROW")
    public Map<String, Object> releaseBuyer(@PathVariable Long escrowId, @RequestParam String reason) {
        requireReason(reason);
        fxPeerClient.releaseEscrowBuyer(escrowId);
        return success("releaseBuyer", escrowId);
    }

    @PostMapping("/escrows/{escrowId}/release/seller")
    @PreAuthorize("hasAnyAuthority('fx.escrow.manage','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
    @Audited(action = "RELEASE_P2P_FX_ESCROW_SELLER", entityType = "P2P_FX_ESCROW")
    public Map<String, Object> releaseSeller(@PathVariable Long escrowId, @RequestParam String reason) {
        requireReason(reason);
        fxPeerClient.releaseEscrowSeller(escrowId);
        return success("releaseSeller", escrowId);
    }

    @GetMapping("/sellers/{sellerId}/ratings")
    @PreAuthorize("hasAnyAuthority('fx.offer.view','customer.profile.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getSellerRatings(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return fxPeerClient.getSellerRatings(sellerId, page, size);
    }

    @GetMapping("/sellers/{sellerId}/stats")
    @PreAuthorize("hasAnyAuthority('fx.offer.view','customer.profile.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getSellerStats(@PathVariable Long sellerId) {
        return fxPeerClient.getSellerStats(sellerId);
    }

    @GetMapping(value = "/orders/{orderId}/receipt/buyer", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyAuthority('fx.offer.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public String getBuyerReceiptHtml(@PathVariable Long orderId) {
        return fxPeerClient.getBuyerReceiptHtml(orderId);
    }

    @GetMapping(value = "/orders/{orderId}/receipt/seller", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyAuthority('fx.offer.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public String getSellerReceiptHtml(@PathVariable Long orderId) {
        return fxPeerClient.getSellerReceiptHtml(orderId);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }
    }

    private Map<String, Object> success(String action, Long escrowId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("action", action);
        response.put("escrowId", escrowId);
        return response;
    }
}
