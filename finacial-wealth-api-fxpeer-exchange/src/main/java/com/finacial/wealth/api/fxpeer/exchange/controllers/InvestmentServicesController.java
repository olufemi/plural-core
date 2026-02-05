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
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentOrderQueryService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentOrderService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentProductService;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentValuationScheduler;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.LiquidationActionService;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProducts;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/investments")
public class InvestmentServicesController {

    private final InvestmentOrderService investmentOrderService;
    private final InvestmentValuationScheduler investmentValuationScheduler;
    private final LiquidationActionService liquidationActionService;
    private final InvestmentProductService productService;

    private final InvestmentOrderQueryService queryService;

    public InvestmentServicesController(InvestmentOrderService investmentOrderService,
            InvestmentValuationScheduler investmentValuationScheduler,
            InvestmentOrderQueryService queryService,
            LiquidationActionService liquidationActionService,
            InvestmentProductService productService) {
        this.investmentOrderService = investmentOrderService;
        this.investmentValuationScheduler = investmentValuationScheduler;
        this.queryService = queryService;
        this.liquidationActionService = liquidationActionService;
        this.productService = productService;

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
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getProducts(
            @RequestHeader(name = "authorization", required = true) String auth
    ) {
        return investmentOrderService.getProducts();
    }

    @PostMapping(
            path = "/create-subscription",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse createSubscription(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid CreateSubscriptionReq rq
    ) throws IOException {
        return investmentOrderService.createSubscriptionCaller(rq, auth);
    }

    @PostMapping(
            path = "/request-liquidation",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse reqLiquidation(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid LiquidateInvestmentRequest rq
    ) throws IOException {
        return investmentOrderService.requestLiquidation(rq, auth);
    }

    @GetMapping(
            path = "/get-customer-history",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getHistory(
            @RequestHeader(name = "authorization", required = true) String auth
    ) throws IOException {
        return investmentValuationScheduler.getHistory(auth);
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
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse reqLiquidation(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid InvestmentTopupRequestCaller rq
    ) throws IOException {
        return investmentOrderService.createTopUpCaller(rq, auth);
    }

}
