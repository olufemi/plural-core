package com.finacial.wealth.api.profiling.market.handlers;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessRequest;
import com.finacial.wealth.api.profiling.market.dto.MarketReadinessResponse;
import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import com.finacial.wealth.api.profiling.market.enums.KycStatus;
import com.finacial.wealth.api.profiling.market.enums.MarketProfileStatus;
import com.finacial.wealth.api.profiling.market.enums.NextAction;
import com.finacial.wealth.api.profiling.market.enums.ProvisionStatus;
import com.finacial.wealth.api.profiling.market.service.CustomerMarketProfileService;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NigeriaMarketOnboardingHandler implements MarketOnboardingHandler {

    private static final String MARKET_CODE = "NG_RETAIL";
    private static final String COUNTRY_CODE = "NG";

    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final CustomerMarketProfileService customerMarketProfileService;

    public NigeriaMarketOnboardingHandler(AddAccountDetailsRepo addAccountDetailsRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            CustomerMarketProfileService customerMarketProfileService) {
        this.addAccountDetailsRepo = addAccountDetailsRepo;
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

        Optional<RegWalletInfo> baseWallet = resolveBaseWallet(request);
        if (!baseWallet.isPresent()) {
            profile.setStatus(MarketProfileStatus.PENDING_KYC.name());
            profile.setKycStatus(KycStatus.PENDING.name());
            customerMarketProfileService.save(profile);
            return pendingSdkResponse(request);
        }

        Optional<AddAccountDetails> nigeriaAccount = resolveNigeriaAccount(request, baseWallet.get());
        if (nigeriaAccount.isPresent()) {
            AddAccountDetails details = nigeriaAccount.get();
            profile.setStatus(MarketProfileStatus.ACTIVE.name());
            profile.setKycStatus(KycStatus.VALIDATED.name());
            profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
            profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
            profile.setWalletId(details.getWalletId());
            profile.setLocalAccountNumber(details.getAccountNumber());
            profile.setVirtualAccountNumber(details.getVirtualAccountNumber());
            profile.setEmailAddress(details.getEmailAddress());
            customerMarketProfileService.save(profile);
            return activeResponse(request, details);
        }

        profile.setStatus(MarketProfileStatus.PENDING_KYC.name());
        profile.setKycStatus(KycStatus.PENDING.name());
        customerMarketProfileService.save(profile);
        return pendingMarketKycResponse(request, baseWallet.get());
    }

    private Optional<RegWalletInfo> resolveBaseWallet(MarketReadinessRequest request) {
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

    private Optional<AddAccountDetails> resolveNigeriaAccount(MarketReadinessRequest request, RegWalletInfo baseWallet) {
        String email = request.getEmailAddress() != null ? request.getEmailAddress() : baseWallet.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        List<AddAccountDetails> accounts = addAccountDetailsRepo.findByCountryCodeByEmailAddress(COUNTRY_CODE, email);
        return accounts.stream().findFirst();
    }

    private void hydrateProfile(CustomerMarketProfile profile, MarketReadinessRequest request) {
        profile.setCountryCode(request.getCountryCode());
        profile.setCurrencyCode(request.getCurrencyCode());
        if (request.getEmailAddress() != null) {
            profile.setEmailAddress(request.getEmailAddress());
        }
    }

    private MarketReadinessResponse activeResponse(MarketReadinessRequest request, AddAccountDetails details) {
        MarketReadinessResponse response = new MarketReadinessResponse();
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(request.getCountryCode());
        response.setCurrencyCode(request.getCurrencyCode());
        response.setStatus(MarketProfileStatus.ACTIVE.name());
        response.setNextAction(NextAction.NONE.name());
        response.setMessage("Market account ready");
        response.setAccountNumber(details.getAccountNumber());
        response.setWalletId(details.getWalletId());
        response.setProviderReference(details.getVirtualAccountNumber());
        response.setActive(Boolean.TRUE);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("accountCountryCode", details.getCountryCode());
        metadata.put("virtualAccountNumber", details.getVirtualAccountNumber());
        response.setMetadata(metadata);
        return response;
    }

    private MarketReadinessResponse pendingSdkResponse(MarketReadinessRequest request) {
        MarketReadinessResponse response = new MarketReadinessResponse();
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(request.getCountryCode());
        response.setCurrencyCode(request.getCurrencyCode());
        response.setStatus(MarketProfileStatus.PENDING_KYC.name());
        response.setNextAction(NextAction.COMPLETE_SDK_ONBOARDING.name());
        response.setMessage("Primary onboarding completion required");
        response.setActive(Boolean.FALSE);
        response.setMetadata(new HashMap<>());
        return response;
    }

    private MarketReadinessResponse pendingMarketKycResponse(MarketReadinessRequest request, RegWalletInfo baseWallet) {
        MarketReadinessResponse response = new MarketReadinessResponse();
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(request.getCountryCode());
        response.setCurrencyCode(request.getCurrencyCode());
        response.setStatus(MarketProfileStatus.PENDING_KYC.name());
        response.setNextAction(NextAction.COMPLETE_MARKET_KYC.name());
        response.setMessage("Market KYC required");
        response.setWalletId(baseWallet.getWalletId());
        response.setActive(Boolean.FALSE);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("verificationType", "BVN");
        metadata.put("countryCode", COUNTRY_CODE);
        response.setMetadata(metadata);
        return response;
    }
}
