package com.finacial.wealth.backoffice.integrations.fxpeer;

import com.finacial.wealth.backoffice.integrations.fxpeer.model.FeaturedServicesConfigRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@FeignClient(name = "fxpeer-exchange-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface FxPeerExchangeClient {

    @PostMapping("/investment/offers/update-offer")
    Map<String, Object> updateOffer(@RequestBody Map<String, Object> request);

    @GetMapping(value = "/investments/admin/products", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getInvestmentProducts(@RequestHeader("authorization") String auth);

    @GetMapping(value = "/fxothers/services/featured", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getFeaturedServices(@RequestHeader("authorization") String auth);

    @GetMapping(value = "/fxothers/admin/featured-services-config", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getFeaturedServicesConfig(@RequestHeader("authorization") String auth);

    @PostMapping(value = "/fxothers/admin/featured-services-config", consumes = "application/json")
    Map<String, Object> saveFeaturedServicesConfig(
            @RequestHeader("authorization") String auth,
            @RequestBody FeaturedServicesConfigRequest request
    );

    @GetMapping(value = "/fxothers/admin/airtime-reversals/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getAirtimeReversalSummary(@RequestHeader("authorization") String auth);

    @GetMapping(value = "/fxothers/admin/airtime-reversals", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getAirtimeReversalCases(
            @RequestHeader("authorization") String auth,
            @RequestParam(required = false) String status
    );

    @PostMapping(value = "/fxothers/admin/airtime-reversals/{processId}/retry", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> retryAirtimeReversal(
            @RequestHeader("authorization") String auth,
            @PathVariable("processId") String processId
    );

    @PostMapping(value = "/investments/create-product", consumes = "application/json")
    Map<String, Object> createInvestmentProduct(@RequestBody InvestmentProductUpsertRequest request);

    @PutMapping(value = "/investments/update-product/{productCode}", consumes = "application/json")
    Map<String, Object> updateInvestmentProduct(
            @PathVariable("productCode") String productCode,
            @RequestBody InvestmentProductUpsertRequest request
    );

    @GetMapping("/investments/orders/all-liquidation-settled")
    Map<String, Object> allLiquidationSettled(@RequestBody Map<String, Object> request);

    @GetMapping("/investments/orders/all-liquidation-processing")
    Map<String, Object> allLiquidationProcessing(@RequestBody Map<String, Object> request);

    @GetMapping("/investments/admin/liquidations")
    Map<String, Object> getAdminLiquidations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

    @GetMapping("/investments/admin/liquidations/history")
    Map<String, Object> getAdminLiquidationHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

    @GetMapping("/investments/admin/orders")
    Map<String, Object> getAdminOrders(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String cutoffBucket,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

    @GetMapping("/investments/admin/performance")
    Map<String, Object> getAdminPerformance(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    );


    @GetMapping("/investments/admin/oversight")
    Map<String, Object> getAdminOversight(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer size
    );

    @GetMapping("/investments/admin/customers/orders")
    Map<String, Object> getCustomerOrders(
            @RequestParam String email,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

    @GetMapping("/investments/admin/customers/liquidations")
    Map<String, Object> getCustomerLiquidations(
            @RequestParam String email,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

    @GetMapping("/investments/admin/customers/positions")
    Map<String, Object> getCustomerPositions(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    );

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
