/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/market")
public class MarketplaceController {

    private final OfferSearchService service;
    private final MarketFacade facade;

    public MarketplaceController(OfferSearchService service, MarketFacade facade) {
        this.service = service;
        this.facade = facade;
    }

    @Operation(summary = "Browse offers with filters + seller stats")
    @GetMapping("/offers")
    public ResponseEntity<Page<OfferView>> browse(@RequestParam(required = false) CurrencyCode ccySell,
            @RequestParam(required = false) CurrencyCode ccyRecv,
            @RequestParam(required = false) BigDecimal rateMin,
            @RequestParam(required = false) BigDecimal rateMax,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "bestRate") String sort) {
        return ResponseEntity.ok(facade.browse(Optional.ofNullable(ccySell), Optional.ofNullable(ccyRecv),
                Optional.ofNullable(rateMin), Optional.ofNullable(rateMax), Optional.ofNullable(amountMin),
                page, size, sort));
    }
}
