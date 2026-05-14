package com.finacial.wealth.api.profiling.market.service;

import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;

public interface CustomerMarketProfileService {

    CustomerMarketProfile getOrCreate(String customerId, String marketCode);

    CustomerMarketProfile save(CustomerMarketProfile customerMarketProfile);

    void markPendingKyc(String customerId, String marketCode, String kycStatus, String metadataJson);

    void markAccountProvisioned(String customerId, String marketCode, String accountNumber,
            String virtualAccountNumber, String providerReference, String metadataJson);

    void markWalletProvisioned(String customerId, String marketCode, String walletId, String metadataJson);

    void markActive(String customerId, String marketCode, String metadataJson);

    void markFailed(String customerId, String marketCode, String reason, String metadataJson);
}
