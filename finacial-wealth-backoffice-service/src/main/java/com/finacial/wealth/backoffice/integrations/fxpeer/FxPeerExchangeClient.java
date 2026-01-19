package com.finacial.wealth.backoffice.integrations.fxpeer;

import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
import com.finacial.wealth.backoffice.model.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "fxpeer-exchange-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface FxPeerExchangeClient {

    @PostMapping("/investment/offers/update-offer")
    Map<String, Object> updateOffer(@RequestBody Map<String, Object> request);

    @GetMapping("/investments/investments/get-products")
    Map<String, Object> getInvestmentProducts();

    @PostMapping(value = "/investments/investments/create-product", consumes = "application/json")
    Map<String, Object> createInvestmentProduct(@RequestBody Map<String, Object> request);

    @PutMapping(value = "/investments/investments/update-product/{productCode}", consumes = "application/json")
    Map<String, Object> updateInvestmentProduct(
            @PathVariable("productCode") String productCode,
            @RequestBody Map<String, Object> request
    );

    @GetMapping("/investments/orders/all-liquidation-settled")
    Map<String, Object> allLiquidationSettled(@RequestBody Map<String, Object> request);

    @GetMapping("/investments/orders/processing")
    Map<String, Object> allLiquidationProcessing(@RequestBody Map<String, Object> request);

    @PostMapping(
            value = "/orders/liquidation/approve",
            consumes = "application/json"
    )
    BaseResponse approveLiquidation(
            @RequestBody LiquidationApprovalRequest request
    );

    @PostMapping(
            value = "/orders/liquidation/cancel",
            consumes = "application/json"
    )
    BaseResponse cncelLiquidation(
            @RequestBody LiquidationApprovalRequest request
    );
}
