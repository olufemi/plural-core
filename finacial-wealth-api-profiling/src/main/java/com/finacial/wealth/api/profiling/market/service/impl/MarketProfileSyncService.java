package com.finacial.wealth.api.profiling.market.service.impl;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import com.finacial.wealth.api.profiling.market.enums.KycStatus;
import com.finacial.wealth.api.profiling.market.enums.MarketProfileStatus;
import com.finacial.wealth.api.profiling.market.enums.ProvisionStatus;
import com.finacial.wealth.api.profiling.market.service.CustomerMarketProfileService;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketProfileSyncService {

    private static final String CA_MARKET = "CA_RETAIL";
    private static final String NG_MARKET = "NG_RETAIL";

    private final CustomerMarketProfileService customerMarketProfileService;
    private final Gson gson = new Gson();

    public MarketProfileSyncService(CustomerMarketProfileService customerMarketProfileService) {
        this.customerMarketProfileService = customerMarketProfileService;
    }

    public void syncCanadaOnboarding(RegWalletInfo regWalletInfo) {
        if (regWalletInfo == null) {
            return;
        }
        CustomerMarketProfile profile = customerMarketProfileService.getOrCreate(resolveCustomerIdentifier(regWalletInfo), CA_MARKET);
        profile.setEmailAddress(regWalletInfo.getEmail());
        profile.setCountryCode("CA");
        profile.setCurrencyCode("CAD");
        profile.setStatus(MarketProfileStatus.ACTIVE.name());
        profile.setKycStatus(KycStatus.VALIDATED.name());
        profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletId(regWalletInfo.getWalletId());
        profile.setLocalAccountNumber(regWalletInfo.getAccountNumber());
        profile.setPrimaryForCustomer(Boolean.TRUE);
        profile.setMetadataJson(buildMetadata(mapOf("source", "CANADA_ONBOARDING")));
        customerMarketProfileService.save(profile);
    }

    public void syncNigeriaKycPending(RegWalletInfo regWalletInfo, String verificationType, String requestId, String processId) {
        if (regWalletInfo == null) {
            return;
        }
        CustomerMarketProfile profile = customerMarketProfileService.getOrCreate(resolveCustomerIdentifier(regWalletInfo), NG_MARKET);
        profile.setEmailAddress(regWalletInfo.getEmail());
        profile.setCountryCode("NG");
        profile.setCurrencyCode("NGN");
        profile.setStatus(MarketProfileStatus.PENDING_KYC.name());
        profile.setKycStatus(KycStatus.PENDING.name());
        profile.setWalletId(regWalletInfo.getWalletId());
        Map<String, Object> metadata = mapOf(
                "verificationType", verificationType,
                "requestId", requestId,
                "processId", processId,
                "source", "NIGERIA_KYC"
        );
        profile.setMetadataJson(buildMetadata(metadata));
        customerMarketProfileService.save(profile);
    }

    public void syncNigeriaAccountProvisioned(RegWalletInfo regWalletInfo, AddAccountDetails addAccountDetails) {
        if (regWalletInfo == null || addAccountDetails == null) {
            return;
        }
        CustomerMarketProfile profile = customerMarketProfileService.getOrCreate(resolveCustomerIdentifier(regWalletInfo), NG_MARKET);
        profile.setEmailAddress(addAccountDetails.getEmailAddress() != null ? addAccountDetails.getEmailAddress() : regWalletInfo.getEmail());
        profile.setCountryCode("NG");
        profile.setCurrencyCode("NGN");
        profile.setStatus(MarketProfileStatus.ACTIVE.name());
        profile.setKycStatus(KycStatus.VALIDATED.name());
        profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletId(addAccountDetails.getWalletId());
        profile.setLocalAccountNumber(addAccountDetails.getAccountNumber());
        profile.setVirtualAccountNumber(addAccountDetails.getVirtualAccountNumber());
        Map<String, Object> metadata = mapOf(
                "verificationType", "BVN",
                "virtualAccountName", addAccountDetails.getVirtualAccountName(),
                "source", "NIGERIA_ACCOUNT_PROVISION"
        );
        profile.setMetadataJson(buildMetadata(metadata));
        customerMarketProfileService.save(profile);
    }

    private String resolveCustomerIdentifier(RegWalletInfo regWalletInfo) {
        if (notBlank(regWalletInfo.getCustomerId())) {
            return regWalletInfo.getCustomerId();
        }
        if (notBlank(regWalletInfo.getWalletId())) {
            return regWalletInfo.getWalletId();
        }
        if (notBlank(regWalletInfo.getEmail())) {
            return regWalletInfo.getEmail();
        }
        return regWalletInfo.getPhoneNumber();
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String buildMetadata(Map<String, Object> metadata) {
        return gson.toJson(metadata);
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            metadata.put(String.valueOf(values[i]), values[i + 1]);
        }
        return metadata;
    }
}
