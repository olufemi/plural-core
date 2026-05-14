package com.finacial.wealth.api.profiling.market.service.impl;

import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import com.finacial.wealth.api.profiling.market.enums.KycStatus;
import com.finacial.wealth.api.profiling.market.enums.MarketProfileStatus;
import com.finacial.wealth.api.profiling.market.enums.ProvisionStatus;
import com.finacial.wealth.api.profiling.market.repo.CustomerMarketProfileRepo;
import com.finacial.wealth.api.profiling.market.service.CustomerMarketProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerMarketProfileServiceImpl implements CustomerMarketProfileService {

    private final CustomerMarketProfileRepo customerMarketProfileRepo;

    public CustomerMarketProfileServiceImpl(CustomerMarketProfileRepo customerMarketProfileRepo) {
        this.customerMarketProfileRepo = customerMarketProfileRepo;
    }

    @Override
    public CustomerMarketProfile getOrCreate(String customerId, String marketCode) {
        return customerMarketProfileRepo.findByCustomerIdAndMarketCode(customerId, marketCode)
                .orElseGet(() -> {
                    CustomerMarketProfile profile = new CustomerMarketProfile();
                    profile.setCustomerId(customerId);
                    profile.setMarketCode(marketCode);
                    profile.setStatus(MarketProfileStatus.NOT_STARTED.name());
                    profile.setKycStatus(KycStatus.NOT_REQUIRED.name());
                    profile.setAccountProvisionStatus(ProvisionStatus.NOT_STARTED.name());
                    profile.setWalletProvisionStatus(ProvisionStatus.NOT_STARTED.name());
                    return customerMarketProfileRepo.save(profile);
                });
    }

    @Override
    public CustomerMarketProfile save(CustomerMarketProfile customerMarketProfile) {
        return customerMarketProfileRepo.save(customerMarketProfile);
    }

    @Override
    public void markPendingKyc(String customerId, String marketCode, String kycStatus, String metadataJson) {
        CustomerMarketProfile profile = getOrCreate(customerId, marketCode);
        profile.setStatus(MarketProfileStatus.PENDING_KYC.name());
        profile.setKycStatus(kycStatus);
        profile.setMetadataJson(metadataJson);
        customerMarketProfileRepo.save(profile);
    }

    @Override
    public void markAccountProvisioned(String customerId, String marketCode, String accountNumber,
            String virtualAccountNumber, String providerReference, String metadataJson) {
        CustomerMarketProfile profile = getOrCreate(customerId, marketCode);
        profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setLocalAccountNumber(accountNumber);
        profile.setVirtualAccountNumber(virtualAccountNumber);
        profile.setExternalProviderReference(providerReference);
        profile.setMetadataJson(metadataJson);
        customerMarketProfileRepo.save(profile);
    }

    @Override
    public void markWalletProvisioned(String customerId, String marketCode, String walletId, String metadataJson) {
        CustomerMarketProfile profile = getOrCreate(customerId, marketCode);
        profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletId(walletId);
        profile.setMetadataJson(metadataJson);
        customerMarketProfileRepo.save(profile);
    }

    @Override
    public void markActive(String customerId, String marketCode, String metadataJson) {
        CustomerMarketProfile profile = getOrCreate(customerId, marketCode);
        profile.setStatus(MarketProfileStatus.ACTIVE.name());
        profile.setMetadataJson(metadataJson);
        customerMarketProfileRepo.save(profile);
    }

    @Override
    public void markFailed(String customerId, String marketCode, String reason, String metadataJson) {
        CustomerMarketProfile profile = getOrCreate(customerId, marketCode);
        profile.setStatus(MarketProfileStatus.FAILED.name());
        profile.setMetadataJson(metadataJson == null ? reason : metadataJson);
        customerMarketProfileRepo.save(profile);
    }
}
