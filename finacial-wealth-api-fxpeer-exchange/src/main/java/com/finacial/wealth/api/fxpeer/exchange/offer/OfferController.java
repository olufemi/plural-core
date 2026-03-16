/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;

import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProducts;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentVerificationCoordinator;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.CancelOfferPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.CreateOfferPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.UpdateOfferPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.hasher.raw.DefaultRawConsentPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/offers")
public class OfferController {

    @Value("${allow.crypto.graphy.for.pin}")
    private String allowCryptoGraphyForPin;

    private final OfferService service;
    private final CreateOfferPayloadHasher createOfferPayloadHasher;
    private final UttilityMethods uttilityMethods;
    private final ConsentVerificationCoordinator consentVerificationCoordinator;
    private final UpdateOfferPayloadHasher updateOfferPayloadHasher;
    private final CancelOfferPayloadHasher cancelOfferPayloadHasher;

    private final DefaultRawConsentPayloadHasher defaultRawConsentPayloadHasher;

    public OfferController(OfferService service, CreateOfferPayloadHasher createOfferPayloadHasher,
            UttilityMethods uttilityMethods,
            ConsentVerificationCoordinator consentVerificationCoordinator,
            UpdateOfferPayloadHasher updateOfferPayloadHasher,
            CancelOfferPayloadHasher cancelOfferPayloadHasher,
            DefaultRawConsentPayloadHasher defaultRawConsentPayloadHasher
    // ProcSochitelServices procSochitelServices
    ) {
        this.service = service;
        this.createOfferPayloadHasher = createOfferPayloadHasher;
        this.uttilityMethods = uttilityMethods;
        this.consentVerificationCoordinator = consentVerificationCoordinator;
        this.updateOfferPayloadHasher = updateOfferPayloadHasher;
        this.cancelOfferPayloadHasher = cancelOfferPayloadHasher;
        this.defaultRawConsentPayloadHasher = defaultRawConsentPayloadHasher;
        //  this.procSochitelServices = procSochitelServices;
    }

    @GetMapping("/get-all-live-offers")
    public ResponseEntity<ApiResponseModel> getAllOffers(
            @RequestHeader(value = "authorization", required = true) String auth,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        ResponseEntity<ApiResponseModel> baseResponse = service.getAllOffersCaller(auth, pageable);
        return baseResponse;

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

    @PostMapping("/create-offer")
    public ResponseEntity<ApiResponseModel> createOfferCaller(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CreateOfferCaller rq,
            HttpServletRequest http) {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.getExpiredAt(), // or use a better reference if available
                    userId,
                    //rq,
                    // createOfferPayloadHasher
                    defaultRawConsentPayloadHasher
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
        }

        return service.createOfferCaller(rq, auth);
    }

    @PostMapping("/update-offer")
    public ResponseEntity<ApiResponseModel> updateOfferCaller(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid UpdateOfferCallerReq rq,
            HttpServletRequest http) {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.getCorrelationId(),
                    userId,
                    // rq,
                    // updateOfferPayloadHasher
                    defaultRawConsentPayloadHasher
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
        }

        return service.updateOfferCaller(rq, auth);
    }

    @PostMapping("/cancel-offer")
    public ResponseEntity<ApiResponseModel> cancelOffer(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CancelOfferCallerReq rq,
            HttpServletRequest http) {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.getCorrelationId(),
                    userId,
                    // rq,
                    // cancelOfferPayloadHasher
                    defaultRawConsentPayloadHasher
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
        }

        return service.cancelOfferCaller(rq, auth);
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

    /*@PostMapping
    public ResponseEntity<Offer> create(@RequestHeader("X-User-Id") long sellerId,
            @RequestBody @Valid CreateOfferRq rq) {
        return ResponseEntity.ok(service.createOffer(rq, sellerId, BigDecimal.ZERO, BigDecimal.ZERO, false, "",""));
    }*/
    @PatchMapping("/{id}/rate")
    public ResponseEntity<Offer> updateRate(@RequestHeader("X-User-Id") long sellerId,
            @PathVariable long id,
            @RequestParam @DecimalMin("0.000001") BigDecimal rate) {
        return ResponseEntity.ok(service.updateRate(id, rate, sellerId, ""));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@RequestHeader("X-User-Id") long sellerId,
            @PathVariable long id) {
        service.cancel(id, sellerId, "");
        return ResponseEntity.noContent().build();
    }

}
