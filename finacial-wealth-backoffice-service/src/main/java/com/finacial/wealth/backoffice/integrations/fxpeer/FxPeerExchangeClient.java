package com.finacial.wealth.backoffice.integrations.fxpeer;

import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
import com.finacial.wealth.backoffice.model.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.http.MediaType;

@FeignClient(name = "fxpeer-exchange-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface FxPeerExchangeClient {

    @PostMapping("/investment/offers/update-offer")
    Map<String, Object> updateOffer(@RequestBody Map<String, Object> request);

    @GetMapping(value = "/investments/get-products", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getInvestmentProducts(@RequestHeader("authorization") String auth);

    @PostMapping(value = "/investments/create-product", consumes = "application/json")
    Map<String, Object> createInvestmentProduct(@RequestBody InvestmentProductUpsertRequest request);

    @PutMapping(value = "/investments/update-product/{productCode}", consumes = "application/json")
    Map<String, Object> updateInvestmentProduct(
            @PathVariable("productCode") String productCode,
            @RequestBody InvestmentProductUpsertRequest request
    );

    @GetMapping("/investments/orders/all-liquidation-settled")
    Map<String, Object> allLiquidationSettled(@RequestBody Map<String, Object> request);

    @GetMapping("/investments/orders/processing")
    Map<String, Object> allLiquidationProcessing(@RequestBody Map<String, Object> request);

    @PostMapping(
            value = "/investments/orders/liquidation/approve",
            consumes = "application/json"
    )
    Map<String, Object> approveLiquidation(@RequestBody LiquidationApprovalRequest request);

    @PostMapping(
            value = "/investments/orders/liquidation/cancel",
            consumes = "application/json"
    )
    Map<String, Object> cancelLiquidation(@RequestBody LiquidationApprovalRequest request);

}
