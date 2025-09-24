/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.NegotiationStatus;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/negs")
public class NegotiationController {

    private final NegotiationRepository repo;
    private final OrderService orders;

    @Value("${negotiation.max-active-per-user:5}")
    private int maxActive;

    public NegotiationController(NegotiationRepository repo, OrderService orders) {
        this.repo = repo;
        this.orders = orders;
    }

    @Operation(summary = "Start a negotiation for an offer")
    @PostMapping
    public ResponseEntity<Negotiation> start(@RequestHeader("X-User-Id") long buyerId, @RequestBody StartRq rq) {
        if (repo.countByBuyerUserIdAndStatus(buyerId, NegotiationStatus.OPEN) >= maxActive) {
            return ResponseEntity.status(429).build();
        }
        Negotiation n = new Negotiation();
        n.setOfferId(rq.offerId());
        n.setBuyerUserId(buyerId);
        n.setProposedRate(rq.rate());
        n.setProposedAmount(rq.amount());
        n.setExpiresAt(Instant.now().plusSeconds(rq.ttlSeconds()));
        n.setStatus(NegotiationStatus.OPEN);
        return ResponseEntity.ok(repo.save(n));
    }

    @Operation(summary = "Accept a negotiation → creates an Order (Buy Now with counter terms)")
    @PostMapping("/{id}/accept")
    public ResponseEntity<Order> accept(@RequestHeader("X-User-Id") long buyerId, @PathVariable long id) {
        Negotiation n = repo.findById(id).orElse(null);
        if (n == null || n.getStatus() != NegotiationStatus.OPEN || n.getBuyerUserId() != buyerId) {
            return ResponseEntity.notFound().build();
        }
        if (n.getExpiresAt().isBefore(Instant.now())) {
            n.setStatus(NegotiationStatus.EXPIRED);
            repo.save(n);
            return ResponseEntity.status(410).build();
        }
        n.setStatus(NegotiationStatus.ACCEPTED);
        repo.save(n);
        Order ord = orders.buyNow(n.getOfferId(), n.getProposedAmount(), buyerId, 600);
        return ResponseEntity.ok(ord);
    }

    @Operation(summary = "Decline a negotiation")
    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> decline(@RequestHeader("X-User-Id") long buyerId, @PathVariable long id) {
        Negotiation n = repo.findById(id).orElse(null);
        if (n == null || n.getBuyerUserId() != buyerId) {
            return ResponseEntity.notFound().build();
        }
        n.setStatus(NegotiationStatus.DECLINED);
        repo.save(n);
        return ResponseEntity.noContent().build();
    }
}
/*@NotNull
@DecimalMin("0.000001") BigDecimal rate, long ttlSeconds) { 

}*/
