package com.finacial.wealth.api.profiling.market.service.impl;

import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;
import com.finacial.wealth.api.profiling.market.entities.MarketDefinition;
import com.finacial.wealth.api.profiling.market.handlers.MarketOnboardingHandler;
import com.finacial.wealth.api.profiling.market.service.MarketDefinitionService;
import com.finacial.wealth.api.profiling.market.service.MarketOrchestrationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketOrchestrationServiceImpl implements MarketOrchestrationService {

    private final MarketDefinitionService marketDefinitionService;
    private final List<MarketOnboardingHandler> marketOnboardingHandlers;

    public MarketOrchestrationServiceImpl(MarketDefinitionService marketDefinitionService,
            List<MarketOnboardingHandler> marketOnboardingHandlers) {
        this.marketDefinitionService = marketDefinitionService;
        this.marketOnboardingHandlers = marketOnboardingHandlers;
    }

    @Override
    public MarketReadinessResponse ensureReady(MarketReadinessRequest request) {
        MarketDefinition marketDefinition = resolveMarketDefinition(request);
        MarketOnboardingHandler handler = marketOnboardingHandlers.stream()
                .filter(candidate -> candidate.supports(marketDefinition.getMarketCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                "No market onboarding handler configured for market: " + marketDefinition.getMarketCode()));

        MarketReadinessRequest normalizedRequest = enrichRequest(request, marketDefinition);
        return handler.ensureCustomerReady(normalizedRequest);
    }

    private MarketDefinition resolveMarketDefinition(MarketReadinessRequest request) {
        if (request.getMarketCode() != null && !request.getMarketCode().trim().isEmpty()) {
            return marketDefinitionService.getRequiredMarket(request.getMarketCode());
        }
        return marketDefinitionService.findByCountryAndCurrency(request.getCountryCode(), request.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported market for countryCode=" + request.getCountryCode()
                + " currencyCode=" + request.getCurrencyCode()));
    }

    private MarketReadinessRequest enrichRequest(MarketReadinessRequest request, MarketDefinition marketDefinition) {
        if (request.getCountryCode() == null || request.getCountryCode().trim().isEmpty()) {
            request.setCountryCode(marketDefinition.getCountryCode());
        }
        if (request.getCurrencyCode() == null || request.getCurrencyCode().trim().isEmpty()) {
            request.setCurrencyCode(marketDefinition.getDefaultCurrencyCode());
        }
        return request;
    }
}
