/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

import com.finacial.wealth.api.fxpeer.exchange.escrow.Escrow;
import com.finacial.wealth.api.fxpeer.exchange.escrow.EscrowService;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final EscrowService escrowService;

    public OrderController(OrderService orderService, EscrowService escrowService) {
        this.orderService = orderService;
        this.escrowService = escrowService;
    }

    @GetMapping("/get-all-customer-transactions")
    public ResponseEntity<ApiResponseModel> getAllOffers(
            @RequestHeader(value = "authorization", required = true) String auth
    //,@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        ResponseEntity<ApiResponseModel> baseResponse = orderService.getUserTransactionsHistory(auth);
        return baseResponse;

    }

    @PostMapping("/buy-offer-now")
    public ResponseEntity<ApiResponseModel> createOfferCaller(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid BuyOfferNow rq) {

        ResponseEntity<ApiResponseModel> baseResponse = orderService.createOrderCaller(rq, auth);
        return baseResponse;
    }

    @Operation(summary = "Buy Now: create Order and reserve offer qty")
    @PostMapping("/offers/{offerId}/buy-now")
    public ResponseEntity<Order> buyNow(@RequestHeader("X-User-Id") long buyerId,
            @PathVariable long offerId,
            @RequestBody @Valid BuyNowRq rq) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Order ord = orderService.buyNow(offerId, rq.amount(), buyerId, rq.lockTtlSeconds());
        Order ord = orderService.buyNow("","",offerId, rq.amount(), String.valueOf(buyerId), 600, "", "", "", "", "");

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
