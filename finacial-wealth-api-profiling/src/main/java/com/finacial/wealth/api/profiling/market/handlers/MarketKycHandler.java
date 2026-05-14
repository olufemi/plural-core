package com.finacial.wealth.api.profiling.market.handlers;

import com.finacial.wealth.api.profiling.market.dto.KycResolutionResult;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;

public interface MarketKycHandler {

    boolean supports(String marketCode);

    KycResolutionResult resolveKyc(MarketReadinessRequest request);
}
