package com.finacial.wealth.api.profiling.market.handlers;

import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;

public interface MarketOnboardingHandler {

    boolean supports(String marketCode);

    MarketReadinessResponse ensureCustomerReady(MarketReadinessRequest request);
}
