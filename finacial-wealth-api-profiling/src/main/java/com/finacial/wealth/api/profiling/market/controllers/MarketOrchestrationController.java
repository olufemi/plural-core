package com.finacial.wealth.api.profiling.market.controllers;

import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;
import com.finacial.wealth.api.profiling.market.service.MarketOrchestrationService;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/walletmgt/markets")
@RequiredArgsConstructor
@Validated
public class MarketOrchestrationController {

    private final MarketOrchestrationService marketOrchestrationService;

    @PostMapping("/ensure-ready")
    public ResponseEntity<BaseResponse> ensureReady(@RequestBody @Valid MarketReadinessRequest request) {
        MarketReadinessResponse readinessResponse = marketOrchestrationService.ensureReady(request);

        BaseResponse response = new BaseResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setDescription(readinessResponse.getMessage() == null
                ? "Market readiness resolved successfully"
                : readinessResponse.getMessage());
        response.setData(toMap(readinessResponse));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Map<String, Object> toMap(MarketReadinessResponse readinessResponse) {
        Map<String, Object> data = new HashMap<>();
        data.put("marketCode", readinessResponse.getMarketCode());
        data.put("countryCode", readinessResponse.getCountryCode());
        data.put("currencyCode", readinessResponse.getCurrencyCode());
        data.put("status", readinessResponse.getStatus());
        data.put("nextAction", readinessResponse.getNextAction());
        data.put("message", readinessResponse.getMessage());
        data.put("accountNumber", readinessResponse.getAccountNumber());
        data.put("walletId", readinessResponse.getWalletId());
        data.put("providerReference", readinessResponse.getProviderReference());
        data.put("active", readinessResponse.getActive());
        data.put("metadata", readinessResponse.getMetadata());
        return data;
    }
}
