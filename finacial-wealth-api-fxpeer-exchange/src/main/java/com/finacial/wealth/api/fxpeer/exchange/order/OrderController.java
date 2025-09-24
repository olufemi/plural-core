/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

import com.finacial.wealth.api.fxpeer.exchange.escrow.Escrow;
import com.finacial.wealth.api.fxpeer.exchange.escrow.EscrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final EscrowService escrowService;

    public OrderController(OrderService orderService, EscrowService escrowService) {
        this.orderService = orderService;
        this.escrowService = escrowService;
    }

    @Operation(summary = "Buy Now: create Order and reserve offer qty")
    @PostMapping("/offers/{offerId}/buy-now")
    public ResponseEntity<Order> buyNow(@RequestHeader("X-User-Id") long buyerId,
            @PathVariable long offerId,
            @RequestBody @Valid BuyNowRq rq) {
        Order ord = orderService.buyNow(offerId, rq.amount(), buyerId, rq.lockTtlSeconds());
        return ResponseEntity.ok(ord);
    }

    @Operation(summary = "Init Escrow for an Order (build buyer/seller legs)")
    @PostMapping("/orders/{orderId}/escrow/init")
    public ResponseEntity<Escrow> initEscrow(@PathVariable long orderId) {
        return ResponseEntity.ok(escrowService.initEscrow(orderId, 15 * 60)); // 15 min default TTL
    }
}

record BuyNowRq(@NotNull
        @DecimalMin("0.01")
        BigDecimal amount,
        @Parameter(description = "Rate lock in seconds")
        long lockTtlSeconds) {

}
