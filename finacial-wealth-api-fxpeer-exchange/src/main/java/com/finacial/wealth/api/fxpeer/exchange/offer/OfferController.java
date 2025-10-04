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
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @GetMapping("/get-all-other-offers")
    public ResponseEntity<ApiResponseModel> getAllOffersExceptLoggedInUser(
            @RequestHeader(value = "authorization", required = true) String auth,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        ResponseEntity<ApiResponseModel> baseResponse = service.getAllOffersExceptLoggedInUserCaller(auth, pageable);
        return baseResponse;

    }

    @GetMapping("/get-all-my-offers")
    public ResponseEntity<ApiResponseModel> getMyOffers(
            @RequestHeader(value = "authorization", required = true) String auth,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        ResponseEntity<ApiResponseModel> baseResponse = service.getMyOffersCaller(auth, pageable);
        return baseResponse;

    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOne(
            @RequestHeader("X-User-Id") long sellerId,
            @PathVariable long id) {
        return ResponseEntity.ok(service.getOffer(id, sellerId));
    }

    @GetMapping
    public ResponseEntity<Page<Offer>> listMine(
            @RequestHeader("X-User-Id") long sellerId,
            @RequestParam(value = "status", required = false) OfferStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Offer> page = service.getMyOffers(sellerId, status, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<Offer> create(@RequestHeader("X-User-Id") long sellerId,
            @RequestBody @Valid CreateOfferRq rq) {
        return ResponseEntity.ok(service.createOffer(rq, sellerId, BigDecimal.ZERO, BigDecimal.ZERO, false, ""));
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
