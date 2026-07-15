/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnLookup;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnLookupRepository;
import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.DeviceDetails;
import com.finacial.wealth.api.profiling.domain.ReferralsLog;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.UserDetails;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.DeviceDetailsRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoBackOfficeRepository;
import com.finacial.wealth.api.profiling.repo.ReferralsLogRepo;
import com.finacial.wealth.api.profiling.repo.UserDetailsRepository;
import com.finacial.wealth.api.profiling.repo.UserBlockAuditRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegWalletInfoBackofficeServiceImpl implements RegWalletInfoBackofficeServiceInter {

    private static final String BLOCKED = "0";
    private static final String ACTIVE = "1";

    private final RegWalletInfoBackOfficeRepository regWalletInfoRepository;
    private final UserBlockAuditRepository userBlockAuditRepository;
    private final RegWalletInfoBackofficeMapper mapper;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final UserDetailsRepository userDetailsRepository;
    private final ReferralsLogRepo referralsLogRepo;
    private final BvnLookupRepository bvnLookupRepository;

    @Override
    public Page<RegWalletInfoBackofficeResponse> getAll(Pageable pageable) {
        return regWalletInfoRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public RegWalletInfoBackofficeResponse getById(Long id) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public RegWalletInfoBackofficeResponse getByCustomerId(String customerId) {
        RegWalletInfo entity = regWalletInfoRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for customerId: " + customerId));
        return mapper.toResponse(entity);
    }

    @Override
    public RegWalletInfoBackofficeResponse getByUuid(String uuid) {
        RegWalletInfo entity = regWalletInfoRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for uuid: " + uuid));
        return mapper.toResponse(entity);
    }

    @Override
    public Map<String, Object> getCustomer360(Long id) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));
        RegWalletInfoBackofficeResponse profile = mapper.toResponse(entity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("profile", profile);
        response.put("identity", buildIdentitySummary(entity));
        response.put("wallets", buildWalletSummary(entity));
        response.put("accounts", buildAccountSummaries(entity));
        response.put("devices", buildDeviceSummaries(entity));
        response.put("referral", buildReferralSummary(entity));
        response.put("userAccess", buildUserAccessSummary(entity));
        response.put("activityTimeline", buildActivityTimeline(entity));
        response.put("coverage", buildCoverageSummary());
        return response;
    }

    @Override
    public Page<RegWalletInfoBackofficeResponse> filter(
            String keyword,
            String customerId,
            String email,
            String phoneNumber,
            String accountNumber,
            String isUserBlocked,
            Pageable pageable
    ) {
        return regWalletInfoRepository.findAll(
                RegWalletInfoSpecification.filter(
                        keyword,
                        customerId,
                        email,
                        phoneNumber,
                        accountNumber,
                        isUserBlocked
                ),
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public RegWalletInfoBackofficeResponse blockUser(Long id, BlockUserRequest request) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));

        String previousStatus = normalizeStatus(entity.getIsUserBlocked());
        entity.setIsUserBlocked(BLOCKED);
        RegWalletInfo saved = regWalletInfoRepository.save(entity);

        saveAudit(saved, previousStatus, BLOCKED, "BLOCK", request);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RegWalletInfoBackofficeResponse unblockUser(Long id, BlockUserRequest request) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));

        String previousStatus = normalizeStatus(entity.getIsUserBlocked());
        entity.setIsUserBlocked(ACTIVE);
        RegWalletInfo saved = regWalletInfoRepository.save(entity);

        saveAudit(saved, previousStatus, ACTIVE, "UNBLOCK", request);

        return mapper.toResponse(saved);
    }

    private void saveAudit(RegWalletInfo entity,
            String previousStatus,
            String newStatus,
            String action,
            BlockUserRequest request) {

        UserBlockAudit audit = new UserBlockAudit();
        audit.setRegWalletInfoId(entity.getId());
        audit.setCustomerId(entity.getCustomerId());
        audit.setPreviousStatus(previousStatus);
        audit.setNewStatus(newStatus);
        audit.setAction(action);
        audit.setPerformedBy(request != null ? request.getPerformedBy() : null);
        audit.setReason(request != null ? request.getReason() : null);
        audit.setActionDate(Instant.now());

        userBlockAuditRepository.save(audit);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ACTIVE;
        }
        return status;
    }

    private Map<String, Object> buildIdentitySummary(RegWalletInfo entity) {
        Optional<BvnLookup> bvnLookup = hasText(entity.getBvnNumber())
                ? bvnLookupRepository.findByBvn(entity.getBvnNumber().trim())
                : Optional.empty();

        Map<String, Object> identity = new LinkedHashMap<>();
        identity.put("onboardingStatus", entity.getIsOnboarded());
        identity.put("activation", entity.isActivation());
        identity.put("completed", entity.isCompleted());
        identity.put("blocked", BLOCKED.equals(normalizeStatus(entity.getIsUserBlocked())));
        identity.put("emailVerified", entity.isEmailVerification());
        identity.put("emailCreation", entity.getEmailCreation());
        identity.put("phoneVerification", entity.getPhoneVerification());
        identity.put("livePhotoUpload", entity.getLivePhotoUpload());
        identity.put("walletTier", entity.getWalletTier());
        identity.put("bvnMasked", mask(entity.getBvnNumber(), 3, 2));
        identity.put("bvnLookupAvailable", bvnLookup.isPresent());
        identity.put("bvnImageAvailable", bvnLookup.map(lookup -> hasText(lookup.getBase64Image())).orElse(false));
        identity.put("bvnWatchListed", bvnLookup.map(BvnLookup::getWatchListed).orElse(null));
        identity.put("bvnResponseCode", bvnLookup.map(BvnLookup::getResponseCode).orElse(null));
        identity.put("bvnLastCheckedAt", bvnLookup.map(BvnLookup::getLastCheckedAt).orElse(null));
        return identity;
    }

    private List<Map<String, Object>> buildWalletSummary(RegWalletInfo entity) {
        List<Map<String, Object>> wallets = new ArrayList<>();
        Map<String, Object> wallet = new LinkedHashMap<>();
        wallet.put("walletType", "PRIMARY");
        wallet.put("currencyCode", "CAD");
        wallet.put("walletId", entity.getWalletId());
        wallet.put("accountNumber", entity.getAccountNumber());
        wallet.put("accountName", entity.getAccountName());
        wallet.put("accountBankCode", entity.getAccountBankCode());
        wallet.put("bankName", entity.getBankName());
        wallet.put("walletTier", entity.getWalletTier());
        wallet.put("active", entity.isActivation());
        wallets.add(wallet);
        return wallets;
    }

    private List<Map<String, Object>> buildAccountSummaries(RegWalletInfo entity) {
        if (!hasText(entity.getEmail())) {
            return Collections.emptyList();
        }
        return addAccountDetailsRepo.findByEmailAddress(entity.getEmail()).stream()
                .map(this::toAccountSummary)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toAccountSummary(AddAccountDetails account) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", account.getId());
        item.put("countryCode", account.getCountryCode());
        item.put("countryName", account.getCountryName());
        item.put("currencyCode", account.getCurrencyCode());
        item.put("currencyName", account.getCurrencyName());
        item.put("walletId", account.getWalletId());
        item.put("accountNumber", account.getAccountNumber());
        item.put("virtualAccountNumber", account.getVirtualAccountNumber());
        item.put("virtualAccountName", account.getVirtualAccountName());
        item.put("phoneNumber", account.getPhoneNumber());
        item.put("createdDate", account.getCreatedDate());
        item.put("lastModifiedDate", account.getLastModifiedDate());
        return item;
    }

    private List<Map<String, Object>> buildDeviceSummaries(RegWalletInfo entity) {
        List<Map<String, Object>> devices = new ArrayList<>();
        if (hasText(entity.getUuid())) {
            Map<String, Object> primary = new LinkedHashMap<>();
            primary.put("source", "REG_WALLET_INFO");
            primary.put("walletId", entity.getWalletId());
            primary.put("uuid", entity.getUuid());
            primary.put("uuidAllowUser", entity.getUuidAllowUser());
            primary.put("active", true);
            devices.add(primary);
        }
        if (hasText(entity.getWalletId())) {
            deviceDetailsRepo.findByWalletId(entity.getWalletId()).stream()
                    .map(this::toDeviceSummary)
                    .forEach(devices::add);
        }
        return devices;
    }

    private Map<String, Object> toDeviceSummary(DeviceDetails device) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", "DEVICE_DETAILS");
        item.put("id", device.getId());
        item.put("walletId", device.getWalletId());
        item.put("uuid", device.getUuid());
        item.put("platform", device.getPlatform());
        item.put("pushTokenPresent", hasText(device.getToken()));
        item.put("createdDate", device.getCreatedDate());
        item.put("lastModifiedDate", device.getLastModifiedDate());
        return item;
    }

    private Map<String, Object> buildReferralSummary(RegWalletInfo entity) {
        Map<String, Object> referral = new LinkedHashMap<>();
        referral.put("referralCode", entity.getReferralCode());
        referral.put("referralCodeLink", entity.getReferralCodeLink());

        List<ReferralsLog> received = hasText(entity.getPhoneNumber())
                ? referralsLogRepo.findByReceiverNumber(entity.getPhoneNumber())
                : Collections.emptyList();
        referral.put("referredBy", received.stream().findFirst().map(this::toReferralSummary).orElse(null));
        referral.put("referralLogCount", received.size());
        return referral;
    }

    private Map<String, Object> toReferralSummary(ReferralsLog log) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("senderNumber", log.getSenderNumber());
        item.put("senderName", log.getSenderName());
        item.put("receiverNumber", log.getReceiverNumber());
        item.put("receiverName", log.getReceiverName());
        item.put("referralCode", log.getReferralCode());
        item.put("referralCodeLink", log.getReferralCodeLink());
        item.put("createdDate", log.getCreatedDate());
        return item;
    }

    private Map<String, Object> buildUserAccessSummary(RegWalletInfo entity) {
        Optional<UserDetails> user = hasText(entity.getEmail())
                ? userDetailsRepository.findByUserEmailId(entity.getEmail())
                : Optional.empty();
        Map<String, Object> access = new LinkedHashMap<>();
        access.put("available", user.isPresent());
        user.ifPresent(details -> {
            access.put("uniqueIdentification", details.getUniqueIdentification());
            access.put("enabled", details.isEnabled());
            access.put("changePassword", details.isChangePassword());
            access.put("userGroup", details.getUserGroup());
            access.put("tokenStatus", details.isTokenStatus());
            access.put("created", details.getCreated());
            access.put("modified", details.getModified());
        });
        return access;
    }

    private List<Map<String, Object>> buildActivityTimeline(RegWalletInfo entity) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        addTimelineEvent(timeline, "PROFILE_CREATED", entity.getCreatedDate(), "Customer profile was created");
        addTimelineEvent(timeline, "PROFILE_MODIFIED", entity.getLastModifiedDate(), "Customer profile was last modified");
        addTimelineEvent(timeline, "WALLET_CREATED", entity.getCreated(), "Primary wallet record created");
        addTimelineEvent(timeline, "WALLET_MODIFIED", entity.getModified(), "Primary wallet record modified");
        return timeline;
    }

    private void addTimelineEvent(List<Map<String, Object>> timeline, String eventType, Object eventTime, String description) {
        if (eventTime == null) {
            return;
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", eventType);
        event.put("eventTime", eventTime);
        event.put("description", description);
        timeline.add(event);
    }

    private Map<String, Object> buildCoverageSummary() {
        Map<String, Object> coverage = new LinkedHashMap<>();
        coverage.put("included", Arrays.asList(
                "profile",
                "identityStatus",
                "primaryWallet",
                "additionalAccounts",
                "devices",
                "referral",
                "userAccess",
                "basicTimeline"
        ));
        coverage.put("pendingIntegrations", Arrays.asList(
                "walletLedgerAndBalances",
                "transactionHistory",
                "supportNotes",
                "communicationLog",
                "fullKycDocumentArchive",
                "riskFlags",
                "serviceEnrollments"
        ));
        return coverage;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String mask(String value, int visibleStart, int visibleEnd) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= visibleStart + visibleEnd) {
            return repeat("*", trimmed.length());
        }
        return trimmed.substring(0, visibleStart)
                + repeat("*", trimmed.length() - visibleStart - visibleEnd)
                + trimmed.substring(trimmed.length() - visibleEnd);
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
