package com.finacial.wealth.api.profiling.testonboarding;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import com.finacial.wealth.api.profiling.market.enums.KycStatus;
import com.finacial.wealth.api.profiling.market.enums.MarketProfileStatus;
import com.finacial.wealth.api.profiling.market.enums.ProvisionStatus;
import com.finacial.wealth.api.profiling.market.service.CustomerMarketProfileService;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.services.UniqueIdService;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TestCadOnboardingService {

    private static final String MARKET_CODE = "CA_RETAIL";
    private static final String COUNTRY_CODE = "CA";
    private static final String COUNTRY_NAME = "Canada";
    private static final String CURRENCY_CODE = "CAD";
    private static final String CURRENCY_NAME = "Canadian Dollar";

    private final RegWalletInfoRepository regWalletInfoRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final UniqueIdService uniqueIdService;
    private final CustomerMarketProfileService customerMarketProfileService;
    private final UttilityMethods utilityMethods;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    public TestCadOnboardingService(RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            UniqueIdService uniqueIdService,
            CustomerMarketProfileService customerMarketProfileService,
            UttilityMethods utilityMethods) {
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.uniqueIdService = uniqueIdService;
        this.customerMarketProfileService = customerMarketProfileService;
        this.utilityMethods = utilityMethods;
    }

    @Transactional
    public TestCadOnboardingResponse createCadUser(TestCadOnboardingRequest request) {
        validate(request);

        String email = clean(request.getEmailAddress()).toLowerCase();
        String phone = clean(request.getPhoneNumber());
        String fullName = fullName(request.getFirstName(), request.getMiddleName(), request.getLastName());

        Optional<RegWalletInfo> existing = regWalletInfoRepository.findByEmail(email);
        if (!existing.isPresent()) {
            existing = regWalletInfoRepository.findByPhoneNumber(phone);
        }

        boolean createdCustomer = !existing.isPresent();
        RegWalletInfo walletInfo = existing.orElseGet(RegWalletInfo::new);
        populateWalletInfo(walletInfo, request, email, phone, fullName, createdCustomer);
        walletInfo = regWalletInfoRepository.save(walletInfo);

        AddAccountDetails accountDetails = findCadAccount(email);
        boolean createdCadAccount = accountDetails == null;
        if (createdCadAccount) {
            accountDetails = new AddAccountDetails();
        }
        populateCadAccount(accountDetails, walletInfo, email, phone, fullName);
        accountDetails = addAccountDetailsRepo.save(accountDetails);

        syncWalletIdentity(walletInfo, accountDetails);
        walletInfo = regWalletInfoRepository.save(walletInfo);

        syncMarketProfile(walletInfo, accountDetails, email);

        TestCadOnboardingResponse response = new TestCadOnboardingResponse();
        response.setCustomerId(walletInfo.getCustomerId());
        response.setEmailAddress(email);
        response.setPhoneNumber(phone);
        response.setFullName(fullName);
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(COUNTRY_CODE);
        response.setCurrencyCode(CURRENCY_CODE);
        response.setAccountNumber(accountDetails.getAccountNumber());
        response.setWalletId(accountDetails.getWalletId());
        response.setCreatedCustomer(createdCustomer);
        response.setCreatedCadAccount(createdCadAccount);
        return response;
    }

    private void validate(TestCadOnboardingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        require(request.getEmailAddress(), "emailAddress");
        require(request.getPhoneNumber(), "phoneNumber");
        require(request.getFirstName(), "firstName");
        require(request.getLastName(), "lastName");
        require(request.getPassword(), "password");
        if (StringUtils.hasText(request.getAccountNumber())
                && regWalletInfoRepository.findByAccountNumber(clean(request.getAccountNumber())).isPresent()) {
            throw new IllegalArgumentException("accountNumber already belongs to an existing customer");
        }
    }

    private void populateWalletInfo(RegWalletInfo walletInfo, TestCadOnboardingRequest request, String email,
            String phone, String fullName, boolean createdCustomer) {
        walletInfo.setEmail(email);
        walletInfo.setPhoneNumber(phone);
        walletInfo.setFirstName(clean(request.getFirstName()));
        walletInfo.setMiddleName(clean(request.getMiddleName()));
        walletInfo.setLastName(clean(request.getLastName()));
        walletInfo.setFullName(fullName);
        walletInfo.setAccountName(fullName);
        walletInfo.setUserName(email);
        walletInfo.setCustomerId(resolveCustomerId(request, walletInfo));
        walletInfo.setUuid(resolveUuid(request, walletInfo));
        walletInfo.setWalletId(resolveWalletId(request, walletInfo));
        walletInfo.setAccountNumber(resolveAccountNumber(request, walletInfo));
        walletInfo.setCompleted(true);
        walletInfo.setActivation(false);
        walletInfo.setIsOnboarded("1");
        walletInfo.setEmailVerification(true);
        walletInfo.setEmailCreation("1");
        walletInfo.setPhoneVerification("1");
        walletInfo.setLivePhotoUpload("0");
        walletInfo.setUerDeviceCustomer("1");
        walletInfo.setWalletTier("Tier 2");
        walletInfo.setIsUserBlocked("0");
        walletInfo.setAccountBankCode("");
        walletInfo.setBvnNumber(defaultIfBlank(walletInfo.getBvnNumber(), "TEST_BYPASS"));
        walletInfo.setPersonId(defaultIfBlank(walletInfo.getPersonId(), "TEST_BYPASS"));
        walletInfo.setClient(defaultIfBlank(walletInfo.getClient(), "TEST_BYPASS"));
        walletInfo.setReferralCode(defaultIfBlank(walletInfo.getReferralCode(), utilityMethods.generateReferralCode("Customer-Onboarding")));
        walletInfo.setReferralCodeLink(defaultIfBlank(walletInfo.getReferralCodeLink(), "TEST-BYPASS-" + walletInfo.getReferralCode()));
        if (createdCustomer || Boolean.TRUE.equals(request.getResetPassword()) || !StringUtils.hasText(walletInfo.getPassword())) {
            walletInfo.setPassword(encryptPassword(request.getPassword()));
        }
        if (createdCustomer) {
            walletInfo.setCreatedBy("TEST_BYPASS");
            walletInfo.setCreatedDate(new Date().toInstant());
        }
        walletInfo.setLastModifiedBy("TEST_BYPASS");
        walletInfo.setLastModifiedDate(new Date().toInstant());
    }

    private void populateCadAccount(AddAccountDetails accountDetails, RegWalletInfo walletInfo, String email,
            String phone, String fullName) {
        accountDetails.setAccountNumber(resolveAccountNumberFromWallet(walletInfo));
        accountDetails.setCountryCode(COUNTRY_CODE);
        accountDetails.setCountryName(COUNTRY_NAME);
        accountDetails.setCurrencyCode(CURRENCY_CODE);
        accountDetails.setCurrencyName(CURRENCY_NAME);
        accountDetails.setEmailAddress(email);
        accountDetails.setWalletId(resolveWalletIdFromWallet(walletInfo));
        accountDetails.setVirtualAccountNumber(resolveAccountNumberFromWallet(walletInfo));
        accountDetails.setVirtualAccountName(fullName);
        accountDetails.setPhoneNumber(phone);
        accountDetails.setLastModifiedDate(new Date().toInstant());
        if (accountDetails.getCreatedDate() == null) {
            accountDetails.setCreatedDate(new Date().toInstant());
        }
    }

    private void syncWalletIdentity(RegWalletInfo walletInfo, AddAccountDetails accountDetails) {
        walletInfo.setWalletId(accountDetails.getWalletId());
        walletInfo.setAccountNumber(accountDetails.getAccountNumber());
    }

    private void syncMarketProfile(RegWalletInfo walletInfo, AddAccountDetails accountDetails, String email) {
        CustomerMarketProfile profile = customerMarketProfileService.getOrCreate(walletInfo.getCustomerId(), MARKET_CODE);
        profile.setEmailAddress(email);
        profile.setCountryCode(COUNTRY_CODE);
        profile.setCurrencyCode(CURRENCY_CODE);
        profile.setStatus(MarketProfileStatus.ACTIVE.name());
        profile.setKycStatus(KycStatus.VALIDATED.name());
        profile.setAccountProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setWalletProvisionStatus(ProvisionStatus.PROVISIONED.name());
        profile.setExternalProviderReference("TEST_BYPASS");
        profile.setWalletId(accountDetails.getWalletId());
        profile.setLocalAccountNumber(accountDetails.getAccountNumber());
        profile.setVirtualAccountNumber(accountDetails.getVirtualAccountNumber());
        profile.setPrimaryForCustomer(true);
        profile.setMetadataJson("{\"source\":\"TEST_ONBOARDING_BYPASS\",\"currencyCode\":\"CAD\"}");
        customerMarketProfileService.save(profile);
    }

    private AddAccountDetails findCadAccount(String email) {
        List<AddAccountDetails> accounts = addAccountDetailsRepo.findByCountryCodeByEmailAddress(COUNTRY_CODE, email);
        if (accounts == null) {
            return null;
        }
        for (AddAccountDetails account : accounts) {
            if (CURRENCY_CODE.equalsIgnoreCase(account.getCurrencyCode())) {
                return account;
            }
        }
        return null;
    }

    private String resolveCustomerId(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getCustomerId(), defaultIfBlank(walletInfo.getCustomerId(), "TEST-CAD-" + System.currentTimeMillis()));
    }

    private String resolveUuid(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getUuid(), defaultIfBlank(walletInfo.getUuid(), "TEST-CAD-DEVICE-" + System.currentTimeMillis()));
    }

    private String resolveWalletId(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getWalletId(), defaultIfBlank(walletInfo.getWalletId(), nextUniqueWalletId()));
    }

    private String resolveAccountNumber(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getAccountNumber(), defaultIfBlank(walletInfo.getAccountNumber(), nextUniqueAccountNumber()));
    }

    private String resolveWalletIdFromWallet(RegWalletInfo walletInfo) {
        return defaultIfBlank(walletInfo.getWalletId(), nextUniqueWalletId());
    }

    private String resolveAccountNumberFromWallet(RegWalletInfo walletInfo) {
        return defaultIfBlank(walletInfo.getAccountNumber(), nextUniqueAccountNumber());
    }

    private String nextUniqueWalletId() {
        String walletId = uniqueIdService.nextUniqueWalletId();
        while (regWalletInfoRepository.findByCustomerId(walletId).isPresent()) {
            walletId = uniqueIdService.nextUniqueWalletId();
        }
        return walletId;
    }

    private String nextUniqueAccountNumber() {
        String accountNumber = uniqueIdService.nextUniquePhoneNumber();
        while (regWalletInfoRepository.findByAccountNumber(accountNumber).isPresent()) {
            accountNumber = uniqueIdService.nextUniquePhoneNumber();
        }
        return accountNumber;
    }

    private String encryptPassword(String password) {
        try {
            return utilityMethods.encyrpt(password, encryptionKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encrypt test user password", ex);
        }
    }

    private String fullName(String firstName, String middleName, String lastName) {
        StringBuilder builder = new StringBuilder(clean(firstName));
        if (StringUtils.hasText(middleName)) {
            builder.append(' ').append(clean(middleName));
        }
        builder.append(' ').append(clean(lastName));
        return builder.toString().trim();
    }

    private void require(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? clean(value) : fallback;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
