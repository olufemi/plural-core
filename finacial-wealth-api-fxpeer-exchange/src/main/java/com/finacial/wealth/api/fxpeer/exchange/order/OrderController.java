/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

import com.finacial.wealth.api.fxpeer.exchange.escrow.Escrow;
import com.finacial.wealth.api.fxpeer.exchange.escrow.EscrowService;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentVerificationCoordinator;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.BuyOfferNowPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UttilityMethods uttilityMethods;
    private final ConsentVerificationCoordinator consentVerificationCoordinator;
    private final BuyOfferNowPayloadHasher buyOfferNowPayloadHasher;

    public OrderController(OrderService orderService, EscrowService escrowService,
            UttilityMethods uttilityMethods,
            ConsentVerificationCoordinator consentVerificationCoordinator,
            BuyOfferNowPayloadHasher buyOfferNowPayloadHasher) {
        this.orderService = orderService;
        this.escrowService = escrowService;
        this.uttilityMethods = uttilityMethods;
        this.consentVerificationCoordinator = consentVerificationCoordinator;
        this.buyOfferNowPayloadHasher = buyOfferNowPayloadHasher;
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
            @RequestBody @Valid BuyOfferNow rq,
            HttpServletRequest http) {

        String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

        BaseResponse consentRes = consentVerificationCoordinator.requireConsent(
                http,
                "POST",
                rq.getOfferCorrelationId(),
                userId,
                rq,
                buyOfferNowPayloadHasher
        );

        if (consentRes.getStatusCode() != 200) {
            ApiResponseModel errorResponse = new ApiResponseModel();
            errorResponse.setStatusCode(consentRes.getStatusCode());
            errorResponse.setDescription(consentRes.getDescription());
            errorResponse.setData(consentRes.getData());

            return ResponseEntity
                    .status(consentRes.getStatusCode())
                    .body(errorResponse);
        }

        return orderService.createOrderCaller(rq, auth);
    }

    @Operation(summary = "Buy Now: create Order and reserve offer qty")
    @PostMapping("/offers/{offerId}/buy-now")
    public ResponseEntity<Order> buyNow(@RequestHeader("X-User-Id") long buyerId,
            @PathVariable long offerId,
            @RequestBody @Valid BuyNowRq rq) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Order ord = orderService.buyNow(offerId, rq.amount(), buyerId, rq.lockTtlSeconds());
        Order ord = orderService.buyNow("", "", offerId, rq.amount(), String.valueOf(buyerId), 600, "", "", "", "", "");

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
