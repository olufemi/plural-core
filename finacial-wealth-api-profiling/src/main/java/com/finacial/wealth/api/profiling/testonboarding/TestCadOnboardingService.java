package com.finacial.wealth.api.profiling.testonboarding;

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.UserDetails;
import com.finacial.wealth.api.profiling.market.service.impl.MarketProfileSyncService;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.repo.UserDetailsRepository;
import com.finacial.wealth.api.profiling.services.UniqueIdService;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TestCadOnboardingService {

    private static final String MARKET_CODE = "CA_RETAIL";
    private static final String COUNTRY_CODE = "CA";
    private static final String CURRENCY_CODE = "CAD";

    private final RegWalletInfoRepository regWalletInfoRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UniqueIdService uniqueIdService;
    private final MarketProfileSyncService marketProfileSyncService;
    private final UttilityMethods utilityMethods;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    public TestCadOnboardingService(RegWalletInfoRepository regWalletInfoRepository,
            UserDetailsRepository userDetailsRepository,
            UniqueIdService uniqueIdService,
            MarketProfileSyncService marketProfileSyncService,
            UttilityMethods utilityMethods) {
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.uniqueIdService = uniqueIdService;
        this.marketProfileSyncService = marketProfileSyncService;
        this.utilityMethods = utilityMethods;
    }

    @Transactional
    public TestCadOnboardingResponse createCadUser(TestCadOnboardingRequest request) {
        validate(request);

        String email = clean(request.getEmailAddress()).toLowerCase();
        String phone = normalizePhoneNumber(request.getPhoneNumber());
        String fullName = fullName(request.getFirstName(), request.getMiddleName(), request.getLastName());

        Optional<RegWalletInfo> existing = regWalletInfoRepository.findByEmail(email);
        if (!existing.isPresent()) {
            existing = regWalletInfoRepository.findByPhoneNumber(phone);
        }
        rejectConflictingUuid(request, existing);

        boolean createdCustomer = !existing.isPresent();
        RegWalletInfo walletInfo = existing.orElseGet(RegWalletInfo::new);
        populateWalletInfo(walletInfo, request, email, phone, fullName, createdCustomer);
        walletInfo = regWalletInfoRepository.save(walletInfo);
        upsertUserDetails(walletInfo, request);

        marketProfileSyncService.syncCanadaOnboarding(walletInfo);

        TestCadOnboardingResponse response = new TestCadOnboardingResponse();
        response.setCustomerId(walletInfo.getCustomerId());
        response.setEmailAddress(email);
        response.setPhoneNumber(phone);
        response.setFullName(fullName);
        response.setMarketCode(MARKET_CODE);
        response.setCountryCode(COUNTRY_CODE);
        response.setCurrencyCode(CURRENCY_CODE);
        response.setAccountNumber(walletInfo.getAccountNumber());
        response.setWalletId(walletInfo.getWalletId());
        response.setCreatedCustomer(createdCustomer);
        response.setCreatedCadAccount(false);
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
        require(request.getUuid(), "uuid");
        require(request.getPhoneNumber(), "phoneNumber");
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
        walletInfo.setCustomerId(defaultIfBlank(walletInfo.getCustomerId(), ""));
        walletInfo.setUuid(resolveUuid(request, walletInfo));
        walletInfo.setWalletId(resolveWalletId(request, walletInfo));
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
        walletInfo.setReferralCodeLink(resolveReferralCodeLink(walletInfo));
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

    private void upsertUserDetails(RegWalletInfo walletInfo, TestCadOnboardingRequest request) {
        Optional<UserDetails> existingUser = userDetailsRepository.findByUserEmailId(walletInfo.getEmail());
        UserDetails userDetails;
        if (existingUser.isPresent()) {
            userDetails = existingUser.get();
            if (!walletInfo.getPhoneNumber().equals(userDetails.getUniqueIdentification())) {
                if (userDetailsRepository.existsByUniqueIdentification(walletInfo.getPhoneNumber())) {
                    throw new IllegalArgumentException("phoneNumber already belongs to a different user");
                }
                userDetailsRepository.delete(userDetails);
                userDetails = newUserDetails(walletInfo);
                userDetailsRepository.save(userDetails);
                return;
            }
            userDetails.setEmailAddress(walletInfo.getEmail());
            userDetails.setFirstName(walletInfo.getFirstName());
            userDetails.setLastName(walletInfo.getLastName());
            userDetails.setUserName(walletInfo.getUserName());
            userDetails.setUserGroup(utilityMethods.returnWalletUserGroupId());
            userDetails.setEnabled(true);
            userDetails.setToken("Authenticated");
            userDetails.setTokenStatus(true);
            userDetails.setChangePassword(true);
            userDetails.setOneTimePwd("No One Time Password");
            userDetails.setOneTimePwdExpired(true);
            if (Boolean.TRUE.equals(request.getResetPassword()) || !StringUtils.hasText(userDetails.getPassword())) {
                userDetails.setPassword(walletInfo.getPassword());
            }
        } else {
            userDetails = newUserDetails(walletInfo);
        }
        userDetailsRepository.save(userDetails);
    }

    private UserDetails newUserDetails(RegWalletInfo walletInfo) {
        return new UserDetails(walletInfo.getPhoneNumber(), walletInfo.getEmail(), "Authenticated",
                true, walletInfo.getPassword(), walletInfo.getLastName(), walletInfo.getUserName(),
                utilityMethods.returnWalletUserGroupId(), true, true, "No One Time Password", true,
                walletInfo.getFirstName());
    }

    private String resolveUuid(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getUuid(), walletInfo.getUuid());
    }

    private void rejectConflictingUuid(TestCadOnboardingRequest request, Optional<RegWalletInfo> existing) {
        String uuid = clean(request.getUuid());
        if (!regWalletInfoRepository.existsByUuid(uuid)) {
            return;
        }
        if (existing.isPresent() && uuid.equals(existing.get().getUuid())) {
            return;
        }
        throw new IllegalArgumentException("The Customer's Device already exist!");
    }

    private String resolveWalletId(TestCadOnboardingRequest request, RegWalletInfo walletInfo) {
        return defaultIfBlank(request.getWalletId(), defaultIfBlank(walletInfo.getWalletId(), nextUniqueWalletId()));
    }

    private String resolveReferralCodeLink(RegWalletInfo walletInfo) {
        String existingReferralLink = clean(walletInfo.getReferralCodeLink());
        if (StringUtils.hasText(existingReferralLink) && !existingReferralLink.startsWith("TEST-BYPASS-")) {
            return existingReferralLink;
        }
        return buildReferralLink(utilityMethods.getSETTING_REF_LINK(), walletInfo.getReferralCode());
    }

    private String buildReferralLink(String baseUrl, String referralCode) {
        if (!StringUtils.hasText(baseUrl)) {
            return referralCode;
        }
        String trimmedBaseUrl = baseUrl.trim();
        if (trimmedBaseUrl.endsWith("/")) {
            return trimmedBaseUrl + referralCode;
        }
        return trimmedBaseUrl + "/" + referralCode;
    }

    private String nextUniqueWalletId() {
        String walletId = uniqueIdService.nextUniqueWalletId();
        while (regWalletInfoRepository.findByCustomerId(walletId).isPresent()) {
            walletId = uniqueIdService.nextUniqueWalletId();
        }
        return walletId;
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

    private String normalizePhoneNumber(String value) {
        String phone = clean(value);
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        phone = phone.replaceAll("\\s+", "");
        if (phone.startsWith("+234") && phone.length() == 14) {
            return "0" + phone.substring(4);
        }
        if (phone.startsWith("234") && phone.length() == 13) {
            return "0" + phone.substring(3);
        }
        return phone;
    }
}
