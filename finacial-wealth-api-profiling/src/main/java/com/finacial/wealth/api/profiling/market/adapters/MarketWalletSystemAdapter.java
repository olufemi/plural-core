package com.finacial.wealth.api.profiling.market.adapters;

import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.WalletSystemProvisionResult;

public interface MarketWalletSystemAdapter {

    boolean supports(String marketCode);

    WalletSystemProvisionResult ensureWalletIdentity(MarketReadinessRequest request);
}
