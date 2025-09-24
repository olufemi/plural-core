/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Offer> create(@RequestHeader("X-User-Id") long sellerId,
            @RequestBody @Valid CreateOfferRq rq) {
        return ResponseEntity.ok(service.createOffer(rq, sellerId));
    }

    @PatchMapping("/{id}/rate")
    public ResponseEntity<Offer> updateRate(@RequestHeader("X-User-Id") long sellerId,
            @PathVariable long id,
            @RequestParam @DecimalMin("0.000001") BigDecimal rate) {
        return ResponseEntity.ok(service.updateRate(id, rate, sellerId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@RequestHeader("X-User-Id") long sellerId,
            @PathVariable long id) {
        service.cancel(id, sellerId);
        return ResponseEntity.noContent().build();
    }
}

record CreateOfferRq(@NotNull
        CurrencyCode currencySell,
        @NotNull
        CurrencyCode currencyReceive,
        @NotNull
        @DecimalMin("0.000001")
        BigDecimal rate,
        @NotNull
        @DecimalMin("0.01")
        BigDecimal qtyTotal) {

}
