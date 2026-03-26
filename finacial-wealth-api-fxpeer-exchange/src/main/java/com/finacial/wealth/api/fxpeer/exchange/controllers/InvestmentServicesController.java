/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;

import com.finacial.wealth.api.fxpeer.exchange.investment.record.CreateSubscriptionReq;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductUpsertRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentTopupRequestCaller;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.LiquidateInvestmentRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.LiquidationApprovalRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.RedemptionInvestmentRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentOrderQueryService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentOrderService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentProductService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentValuationScheduler;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.LiquidationActionService;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProducts;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentVerificationCoordinator;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.CreateSubscriptionPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.InvestmentTopupPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher.LiquidateInvestmentPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.hasher.raw.DefaultRawConsentPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/investments")
public class InvestmentServicesController {

    @Value("${allow.crypto.graphy.for.pin}")
    private String allowCryptoGraphyForPin;

    private final InvestmentOrderService investmentOrderService;
    private final InvestmentValuationScheduler investmentValuationScheduler;
    private final LiquidationActionService liquidationActionService;
    private final InvestmentProductService productService;
    private final UttilityMethods uttilityMethods;
    private final ConsentVerificationCoordinator consentVerificationCoordinator;
    private final CreateSubscriptionPayloadHasher createSubscriptionPayloadHasher;
    private final LiquidateInvestmentPayloadHasher liquidateInvestmentPayloadHasher;
    private final InvestmentTopupPayloadHasher investmentTopupPayloadHasher;

    private final InvestmentOrderQueryService queryService;
    private final DefaultRawConsentPayloadHasher defaultRawConsentPayloadHasher;

    public InvestmentServicesController(InvestmentOrderService investmentOrderService,
            InvestmentValuationScheduler investmentValuationScheduler,
            InvestmentOrderQueryService queryService,
            LiquidationActionService liquidationActionService,
            InvestmentProductService productService,
            UttilityMethods uttilityMethods,
            ConsentVerificationCoordinator consentVerificationCoordinator,
            CreateSubscriptionPayloadHasher createSubscriptionPayloadHasher,
            LiquidateInvestmentPayloadHasher liquidateInvestmentPayloadHasher,
            InvestmentTopupPayloadHasher investmentTopupPayloadHasher,
            DefaultRawConsentPayloadHasher defaultRawConsentPayloadHasher) {
        this.investmentOrderService = investmentOrderService;
        this.investmentValuationScheduler = investmentValuationScheduler;
        this.queryService = queryService;
        this.liquidationActionService = liquidationActionService;
        this.productService = productService;
        this.uttilityMethods = uttilityMethods;
        this.consentVerificationCoordinator = consentVerificationCoordinator;
        this.createSubscriptionPayloadHasher = createSubscriptionPayloadHasher;
        this.liquidateInvestmentPayloadHasher = liquidateInvestmentPayloadHasher;
        this.investmentTopupPayloadHasher = investmentTopupPayloadHasher;
        this.defaultRawConsentPayloadHasher = defaultRawConsentPayloadHasher;

    }

    @PostMapping("/get-redemptions")
    public ResponseEntity<BaseResponse> getInvestmentRedemptions(
            @RequestBody RedemptionInvestmentRequest rq,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {

        BaseResponse res = investmentOrderService.getInvestmentRedemption(rq, auth);

        HttpStatus httpStatus = HttpStatus.resolve(res.getStatusCode());
        if (httpStatus == null) {
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(res, httpStatus);
    }

    @PostMapping("/create-product")
    public Map<String, Object> create(@RequestBody InvestmentProductUpsertRequest req) {
        return productService.create(req);
    }

    @PutMapping("/update-product/{productCode}")
    public Map<String, Object> update(
            @PathVariable String productCode,
            @RequestBody InvestmentProductUpsertRequest req
    ) {
        req.setProductCode(productCode); // path wins
        return productService.update(req);
    }

    @GetMapping("/orders/all-liquidation-settled")
    public ResponseEntity<ApiResponseModel> getAllSettled( //   @RequestHeader("Authorization") String auth
            ) {
        return queryService.getOrdersByStatusForCustomer(InvestmentOrderStatus.SETTLED);
    }

    @GetMapping("/orders/all-liquidation-processing")
    public ResponseEntity<ApiResponseModel> getAllLiquidationProcessing( //  @RequestHeader("Authorization") String auth
            ) {
        return queryService.getOrdersByStatusForCustomer(InvestmentOrderStatus.LIQUIDATION_PROCESSING);
    }

    @PostMapping("/orders/liquidation/approve")
    public ResponseEntity<BaseResponse> approveLiquidation(
            @RequestBody LiquidationApprovalRequest request
    ) {
        return ResponseEntity.ok(
                liquidationActionService.approveAndSettleLiquidation(
                        request.getOrderRef()
                )
        );
    }

    @PostMapping("/orders/liquidation/cancel")
    public ResponseEntity<BaseResponse> cancelLiquidation(
            @RequestBody LiquidationApprovalRequest request
    ) {
        return ResponseEntity.ok(
                liquidationActionService.cancelLiquidation(
                        request.getOrderRef()
                )
        );
    }

    @GetMapping(
            path = "/get-products",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getProducts(
            @RequestHeader(name = "authorization", required = true) String auth
    ) {
        return investmentOrderService.getProducts();
    }

    @PostMapping(
            path = "/create-subscription",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse createSubscription(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid CreateSubscriptionReq rq,
            HttpServletRequest http
    ) throws IOException {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.getProcessId(),
                    userId,
                    //rq,
                    //createSubscriptionPayloadHasher
                    defaultRawConsentPayloadHasher
            );

            if (consentRes.getStatusCode() != 200) {
                return consentRes;
            }
        }

        return investmentOrderService.createSubscriptionCaller(rq, auth);
    }

    @PostMapping(
            path = "/request-liquidation",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse reqLiquidation(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid LiquidateInvestmentRequest rq,
            HttpServletRequest http
    ) throws IOException {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.processId(),
                    userId,
                    // rq,
                    // liquidateInvestmentPayloadHasher
                    defaultRawConsentPayloadHasher
            );

            if (consentRes.getStatusCode() != 200) {
                return consentRes;
            }
        }

        return investmentOrderService.requestLiquidation(rq, auth);
    }

    @GetMapping(
            path = "/get-customer-history",
            //     consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getHistory(
            @RequestHeader(name = "authorization", required = true) String auth
    ) throws IOException {
        return investmentValuationScheduler.getHistory(auth);
    }

    @GetMapping(
            path = "/get-customer-investment-position",
            //     consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getInvestmentPositions(
            @RequestHeader(name = "authorization", required = true) String auth
    ) throws IOException {
        return investmentValuationScheduler.getInvestmentPositions(auth);
    }

    /**
     * Simple probe to confirm you’re hitting THIS build & service. curl -s
     * http://127.0.0.1:7007/fxothers/__ping
     */
    @GetMapping(path = "/__ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "investments:ok";
    }

    @PostMapping(
            path = "/request-top-up",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse requestTopup(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid InvestmentTopupRequestCaller rq,
            HttpServletRequest http
    ) throws IOException {
        if (allowCryptoGraphyForPin.equals("1")) {
            String userId = uttilityMethods.getClaimFromJwt(auth, "emailAddress");

            BaseResponse consentRes = consentVerificationCoordinator.requireConsentUsingRawBody(
                    http,
                    "POST",
                    rq.getProcessId(),
                    userId,
                    //  rq,
                    // investmentTopupPayloadHasher
                    defaultRawConsentPayloadHasher
            );

            if (consentRes.getStatusCode() != 200) {
                return consentRes;
            }
        }

        return investmentOrderService.createTopUpCaller(rq, auth);
    }

}
