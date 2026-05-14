package com.finacial.wealth.api.profiling.market.handlers;

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;
import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import com.finacial.wealth.api.profiling.market.enums.KycStatus;
import com.finacial.wealth.api.profiling.market.enums.MarketProfileStatus;
import com.finacial.wealth.api.profiling.market.enums.NextAction;
import com.finacial.wealth.api.profiling.market.enums.ProvisionStatus;
import com.finacial.wealth.api.profiling.market.service.CustomerMarketProfileService;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanadaMarketOnboardingHandler implements MarketOnboardingHandler {

    private static final String MARKET_CODE = "CA_RETAIL";

    private final RegWalletInfoRepository regWalletInfoRepository;
    private final CustomerMarketProfileService customerMarketProfileService;

    public CanadaMarketOnboardingHandler(RegWalletInfoRepository regWalletInfoRepository,
            CustomerMarketProfileService customerMarketProfileService) {
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.customerMarketProfileService = customerMarketProfileService;
    }

    @Override
    public boolean supports(String marketCode) {
        return MARKET_CODE.equalsIgnoreCase(marketCode);
    }

    @Override
    public MarketReadinessResponse ensureCustomerReady(MarketReadinessRequest request) {
        CustomerMarketProfile profile = customerMarketProfileService.getOrCreate(request.getCustomerId(), MARKET_CODE);
        hydrateProfile(profile, request);

        Optional<RegWalletInfo> walletInfo = resolveWalletInfo(request);
        if (walletInfo.isPresent()) {
            RegWalletInfo regWalletInfo = walletInfo.get();
            profile.setStatus(MarketProfileStatus.ACTIVE.name());
            profile.setKycStatus(KycStatus.VALIDATED.name());
            profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
            profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
            profile.setWalletId(regWalletInfo.getWalletId());
            profile.setLocalAccountNumber(regWalletInfo.getAccountNumber());
            profile.setEmailAddress(regWalletInfo.getEmail());
            customerMarketProfileService.save(profile);
            return activeResponse(request, regWalletInfo);
        }

        profile.setStatus(MarketProfileStatus.PENDING_KYC.name());
        profile.setKycStatus(KycStatus.PENDING.name());
        customerMarketProfileService.save(profile);
        return pendingResponse(request);
    }

    private Optional<RegWalletInfo> resolveWalletInfo(MarketReadinessRequest request) {
        if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
            Optional<RegWalletInfo> byCustomerId = regWalletInfoRepository.findByCustomerId(request.getCustomerId());
            if (byCustomerId.isPresent()) {
                return byCustomerId;
            }
        }
        if (request.getEmailAddress() != null && !request.getEmailAddress().trim().isEmpty()) {
            Optional<RegWalletInfo> byEmail = regWalletInfoRepository.findByEmail(request.getEmailAddress());
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            return regWalletInfoRepository.findByPhoneNumber(request.getPhoneNumber());
        }
        return Optional.empty();
    }

    private void hydrateProfile(CustomerMarketProfile profile, MarketReadinessRequest request) {
        profile.setCountryCode(request.getCountryCode());
        profile.setCurrencyCode(request.getCurrencyCode());
        if (request.getEmailAddress() != null) {
            profile.setEmailAddress(request.getEmailAddress());
        }
    }

    private MarketReadinessResponse activeResponse(MarketReadinessRequest request, RegWalletInfo regWalletInfo) {
        MarketReadinessResponse response = new MarketReadinessResponse();
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(request.getCountryCode());
        response.setCurrencyCode(request.getCurrencyCode());
        response.setStatus(MarketProfileStatus.ACTIVE.name());
        response.setNextAction(NextAction.NONE.name());
        response.setMessage("Market account ready");
        response.setAccountNumber(regWalletInfo.getAccountNumber());
        response.setWalletId(regWalletInfo.getWalletId());
        response.setProviderReference(regWalletInfo.getCustomerId());
        response.setActive(Boolean.TRUE);
        response.setMetadata(new HashMap<>());
        return response;
    }

    private MarketReadinessResponse pendingResponse(MarketReadinessRequest request) {
        MarketReadinessResponse response = new MarketReadinessResponse();
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(request.getCountryCode());
        response.setCurrencyCode(request.getCurrencyCode());
        response.setStatus(MarketProfileStatus.PENDING_KYC.name());
        response.setNextAction(NextAction.COMPLETE_SDK_ONBOARDING.name());
        response.setMessage("SDK onboarding completion required");
        response.setActive(Boolean.FALSE);
        response.setMetadata(new HashMap<>());
        return response;
    }
}
