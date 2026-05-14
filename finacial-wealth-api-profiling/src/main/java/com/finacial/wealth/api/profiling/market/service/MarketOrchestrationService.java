package com.finacial.wealth.api.profiling.market.service;

import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;

public interface MarketOrchestrationService {

    MarketReadinessResponse ensureReady(MarketReadinessRequest request);
}
