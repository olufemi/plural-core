package com.finacial.wealth.api.profiling.market.handlers;

import com.finacial.wealth.api.profiling.market.dto.AccountProvisionRequest;
import com.finacial.wealth.api.profiling.market.dto.AccountProvisionResult;

public interface MarketAccountProvisioner {

    boolean supports(String marketCode);

    AccountProvisionResult provision(AccountProvisionRequest request);
}
