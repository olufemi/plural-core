package com.finacial.wealth.api.profiling.referralprogram.service;

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralAttribution;
import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgram;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralAttributionStatus;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardCurrencyMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardTarget;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramStatus;
import com.finacial.wealth.api.profiling.referralprogram.model.ApplyReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.CompleteReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.QualifyReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.repo.ReferralAttributionRepository;
import com.finacial.wealth.api.profiling.referralprogram.repo.ReferralProgramRepository;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferralProgramRuntimeService {

    private final ReferralProgramRepository referralProgramRepository;
    private final ReferralAttributionRepository referralAttributionRepository;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final UttilityMethods uttilityMethods;

    public ReferralProgramRuntimeService(ReferralProgramRepository referralProgramRepository,
            ReferralAttributionRepository referralAttributionRepository,
            RegWalletInfoRepository regWalletInfoRepository,
            UttilityMethods uttilityMethods) {
        this.referralProgramRepository = referralProgramRepository;
        this.referralAttributionRepository = referralAttributionRepository;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.uttilityMethods = uttilityMethods;
    }

    @Transactional
    public BaseResponse apply(ApplyReferralAttributionRequest req, String auth) {
        BaseResponse response = new BaseResponse();

        String referralCode = trimToNull(req == null ? null : req.getReferralCode());
        if (isBlank(referralCode)) {
            return error(response, 400, "That code isn’t valid or has expired.");
        }

        ReferralProgramProductType productType = parseProductType(req == null ? null : req.getProductType(), response);
        if (productType == null) {
            return response;
        }

        ReferralProgram program = getActiveProgram(productType);
        if (program == null) {
            return error(response, 400, "This referral offer isn’t available right now.");
        }

        RegWalletInfo referee = getCurrentCustomer(auth, response);
        if (referee == null) {
            return response;
        }

        Optional<ReferralAttribution> existing = referralAttributionRepository
                .findByRefereeWalletIdAndProductType(referee.getWalletId(), productType);
        if (existing.isPresent()) {
            ReferralAttribution attribution = existing.get();
            if (ReferralAttributionStatus.REWARDED.equals(attribution.getStatus())) {
                return error(response, 409, "Referral rewards are for new users only.");
            }
            if (referralCode.equalsIgnoreCase(attribution.getReferrerCode())) {
                return populateApplyResponse(ok(response, "Referral code applied successfully."), attribution);
            }
            return error(response, 409, "A referral is already applied to your account.");
        }

        List<RegWalletInfo> referrers = regWalletInfoRepository.findByReferralCode(referralCode);
        if (referrers == null || referrers.isEmpty()) {
            return error(response, 400, "That code isn’t valid or has expired.");
        }

        RegWalletInfo referrer = referrers.get(0);
        if (sameCustomer(referrer, referee)) {
            return error(response, 400, "That code isn’t valid or has expired.");
        }

        Date now = new Date();
        ReferralAttribution attribution = new ReferralAttribution();
        attribution.setProgramId(program.getId());
        attribution.setProgramCode(program.getProgramCode());
        attribution.setProductType(program.getProductType());
        attribution.setReferrerWalletId(referrer.getWalletId());
        attribution.setReferrerEmail(referrer.getEmail());
        attribution.setReferrerName(resolveName(referrer));
        attribution.setReferrerCode(referralCode);
        attribution.setRefereeWalletId(referee.getWalletId());
        attribution.setRefereeEmail(referee.getEmail());
        attribution.setRefereeName(resolveName(referee));
        attribution.setRewardTarget(program.getRewardTarget());
        attribution.setRewardMode(program.getRewardMode());
        attribution.setRewardValue(program.getRewardValue());
        attribution.setRewardCurrencyMode(program.getRewardCurrencyMode());
        attribution.setFixedCurrencyCode(program.getFixedCurrencyCode());
        attribution.setMinQualifyingAmount(program.getMinQualifyingAmount());
        attribution.setMinRewardAmount(program.getMinRewardAmount());
        attribution.setMaxRewardAmount(program.getMaxRewardAmount());
        attribution.setQualifyingTransactionCount(program.getQualifyingTransactionCount());
        attribution.setStatus(ReferralAttributionStatus.APPLIED);
        attribution.setAppliedAt(now);
        attribution.setCreatedAt(now);
        attribution.setUpdatedAt(now);
        attribution.setReferrerRewardPaid(Boolean.FALSE);
        attribution.setRefereeRewardPaid(Boolean.FALSE);

        ReferralAttribution saved = referralAttributionRepository.save(attribution);
        return populateApplyResponse(ok(response, "Referral code applied successfully."), saved);
    }

    @Transactional
    public BaseResponse qualify(QualifyReferralAttributionRequest req, String auth) {
        BaseResponse response = new BaseResponse();

        ReferralProgramProductType productType = parseProductType(req == null ? null : req.getProductType(), response);
        if (productType == null) {
            return response;
        }

        if (req == null || isBlank(req.getTransactionId())) {
            return error(response, 400, "transactionId is required.");
        }
        if (req.getTransactionAmount() == null || req.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return error(response, 400, "transactionAmount must be greater than zero.");
        }

        RegWalletInfo referee = getCurrentCustomer(auth, response);
        if (referee == null) {
            return response;
        }

        Optional<ReferralAttribution> optionalAttribution = referralAttributionRepository
                .findByRefereeWalletIdAndProductType(referee.getWalletId(), productType);
        if (!optionalAttribution.isPresent()) {
            return error(response, 404, "No referral is applied to this account.");
        }

        ReferralAttribution attribution = optionalAttribution.get();
        if (ReferralAttributionStatus.REWARDED.equals(attribution.getStatus())) {
            response = ok(response, "Referral reward already processed.");
            response.addData("attributionId", attribution.getId());
            response.addData("shouldReward", Boolean.FALSE);
            return response;
        }

        if (ReferralAttributionStatus.QUALIFIED_PENDING_PAYOUT.equals(attribution.getStatus())) {
            return populateQualificationResponse(ok(response, "Referral reward is pending payout."), attribution, true);
        }

        if (req.getCompletedTransactionCount() != null
                && req.getCompletedTransactionCount() < attribution.getQualifyingTransactionCount()) {
            return error(response, 400, "Referral reward is not yet eligible.");
        }

        BigDecimal transactionAmount = scaleMoney(req.getTransactionAmount());
        if (attribution.getMinQualifyingAmount() != null
                && transactionAmount.compareTo(attribution.getMinQualifyingAmount()) < 0) {
            return error(response, 400, "Referral reward is not yet eligible.");
        }

        BigDecimal rewardAmount = calculateRewardAmount(attribution, transactionAmount);
        String rewardCurrencyCode = resolveRewardCurrencyCode(attribution, req.getTradeCurrencyCode());

        BigDecimal referrerRewardAmount = BigDecimal.ZERO;
        BigDecimal refereeRewardAmount = BigDecimal.ZERO;

        if (ReferralProgramRewardTarget.BOTH.equals(attribution.getRewardTarget())
                || ReferralProgramRewardTarget.REFERRER_ONLY.equals(attribution.getRewardTarget())) {
            referrerRewardAmount = rewardAmount;
        }
        if (ReferralProgramRewardTarget.BOTH.equals(attribution.getRewardTarget())
                || ReferralProgramRewardTarget.REFEREE_ONLY.equals(attribution.getRewardTarget())) {
            refereeRewardAmount = rewardAmount;
        }

        attribution.setQualifiedTransactionId(req.getTransactionId());
        attribution.setQualifiedCorrelationId(trimToNull(req.getCorrelationId()));
        attribution.setQualifiedAmount(transactionAmount);
        attribution.setQualifiedCurrencyCode(normalizeCurrencyCode(req.getTradeCurrencyCode()));
        attribution.setRewardCurrencyCode(rewardCurrencyCode);
        attribution.setReferrerRewardAmount(scaleMoney(referrerRewardAmount));
        attribution.setRefereeRewardAmount(scaleMoney(refereeRewardAmount));
        attribution.setQualifiedAt(new Date());
        attribution.setUpdatedAt(new Date());

        boolean shouldReward = hasRewardDue(attribution.getReferrerRewardAmount())
                || hasRewardDue(attribution.getRefereeRewardAmount());

        if (shouldReward) {
            attribution.setStatus(ReferralAttributionStatus.QUALIFIED_PENDING_PAYOUT);
        } else {
            attribution.setStatus(ReferralAttributionStatus.REWARDED);
            attribution.setRewardedAt(new Date());
            attribution.setReferrerRewardPaid(Boolean.TRUE);
            attribution.setRefereeRewardPaid(Boolean.TRUE);
        }

        ReferralAttribution saved = referralAttributionRepository.save(attribution);
        return populateQualificationResponse(ok(response,
                shouldReward ? "Referral reward qualified successfully." : "Referral qualification recorded."), saved, shouldReward);
    }

    @Transactional
    public BaseResponse complete(Long attributionId, CompleteReferralAttributionRequest req, String auth) {
        BaseResponse response = new BaseResponse();
        if (attributionId == null) {
            return error(response, 400, "attributionId is required.");
        }
        if (req == null) {
            return error(response, 400, "Completion payload is required.");
        }

        Optional<ReferralAttribution> optionalAttribution = referralAttributionRepository.findById(attributionId);
        if (!optionalAttribution.isPresent()) {
            return error(response, 404, "Referral attribution not found.");
        }

        ReferralAttribution attribution = optionalAttribution.get();
        if (Boolean.TRUE.equals(req.getReferrerRewardPaid())) {
            attribution.setReferrerRewardPaid(Boolean.TRUE);
        }
        if (Boolean.TRUE.equals(req.getRefereeRewardPaid())) {
            attribution.setRefereeRewardPaid(Boolean.TRUE);
        }
        if (!isBlank(req.getReferrerPayoutReference())) {
            attribution.setReferrerPayoutReference(req.getReferrerPayoutReference().trim());
        }
        if (!isBlank(req.getRefereePayoutReference())) {
            attribution.setRefereePayoutReference(req.getRefereePayoutReference().trim());
        }
        attribution.setUpdatedAt(new Date());

        if (isRewardCompletionSatisfied(attribution)) {
            attribution.setStatus(ReferralAttributionStatus.REWARDED);
            attribution.setRewardedAt(new Date());
        }

        ReferralAttribution saved = referralAttributionRepository.save(attribution);
        BaseResponse ok = ok(response, ReferralAttributionStatus.REWARDED.equals(saved.getStatus())
                ? "Referral reward marked as paid."
                : "Referral reward completion recorded.");
        ok.addData("attributionId", saved.getId());
        ok.addData("status", saved.getStatus().name());
        return ok;
    }

    private BaseResponse populateApplyResponse(BaseResponse response, ReferralAttribution attribution) {
        response.addData("attributionId", attribution.getId());
        response.addData("programCode", attribution.getProgramCode());
        response.addData("productType", attribution.getProductType().name());
        response.addData("referrerName", attribution.getReferrerName());
        response.addData("referralCode", attribution.getReferrerCode());
        response.addData("rewardMode", attribution.getRewardMode().name());
        response.addData("rewardValue", attribution.getRewardValue());
        response.addData("rewardCurrencyMode", attribution.getRewardCurrencyMode().name());
        response.addData("fixedCurrencyCode", attribution.getFixedCurrencyCode());
        response.addData("rewardTarget", attribution.getRewardTarget().name());
        return response;
    }

    private BaseResponse populateQualificationResponse(BaseResponse response, ReferralAttribution attribution, boolean shouldReward) {
        response.addData("attributionId", attribution.getId());
        response.addData("shouldReward", shouldReward);
        response.addData("rewardCurrencyCode", attribution.getRewardCurrencyCode());
        response.addData("referrerRewardAmount", zeroIfNull(attribution.getReferrerRewardAmount()));
        response.addData("refereeRewardAmount", zeroIfNull(attribution.getRefereeRewardAmount()));
        response.addData("referrerWalletId", attribution.getReferrerWalletId());
        response.addData("refereeWalletId", attribution.getRefereeWalletId());
        response.addData("referrerEmail", attribution.getReferrerEmail());
        response.addData("refereeEmail", attribution.getRefereeEmail());
        response.addData("referrerName", attribution.getReferrerName());
        response.addData("refereeName", attribution.getRefereeName());
        response.addData("status", attribution.getStatus().name());
        return response;
    }

    private BigDecimal calculateRewardAmount(ReferralAttribution attribution, BigDecimal transactionAmount) {
        BigDecimal rewardAmount;
        if (ReferralProgramRewardMode.PERCENTAGE_OF_TRANSACTION.equals(attribution.getRewardMode())) {
            rewardAmount = transactionAmount
                    .multiply(attribution.getRewardValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            rewardAmount = attribution.getRewardValue();
        }

        rewardAmount = scaleMoney(rewardAmount);

        if (attribution.getMinRewardAmount() != null
                && rewardAmount.compareTo(attribution.getMinRewardAmount()) < 0) {
            rewardAmount = scaleMoney(attribution.getMinRewardAmount());
        }
        if (attribution.getMaxRewardAmount() != null
                && rewardAmount.compareTo(attribution.getMaxRewardAmount()) > 0) {
            rewardAmount = scaleMoney(attribution.getMaxRewardAmount());
        }
        return rewardAmount;
    }

    private String resolveRewardCurrencyCode(ReferralAttribution attribution, String tradeCurrencyCode) {
        if (ReferralProgramRewardCurrencyMode.FIXED_CURRENCY.equals(attribution.getRewardCurrencyMode())) {
            return normalizeCurrencyCode(attribution.getFixedCurrencyCode());
        }
        return normalizeCurrencyCode(tradeCurrencyCode);
    }

    private boolean isRewardCompletionSatisfied(ReferralAttribution attribution) {
        boolean referrerDue = hasRewardDue(attribution.getReferrerRewardAmount());
        boolean refereeDue = hasRewardDue(attribution.getRefereeRewardAmount());

        boolean referrerPaid = !referrerDue || Boolean.TRUE.equals(attribution.getReferrerRewardPaid());
        boolean refereePaid = !refereeDue || Boolean.TRUE.equals(attribution.getRefereeRewardPaid());

        return referrerPaid && refereePaid;
    }

    private boolean hasRewardDue(BigDecimal rewardAmount) {
        return rewardAmount != null && rewardAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }

    private ReferralProgram getActiveProgram(ReferralProgramProductType productType) {
        List<ReferralProgram> programs = referralProgramRepository.findActivePrograms(
                productType, ReferralProgramStatus.ACTIVE, new Date());
        if (programs == null || programs.isEmpty()) {
            return null;
        }
        return programs.get(0);
    }

    private ReferralProgramProductType parseProductType(String productType, BaseResponse response) {
        if (isBlank(productType)) {
            error(response, 400, "productType is required.");
            return null;
        }
        try {
            return ReferralProgramProductType.valueOf(productType.trim().toUpperCase());
        } catch (Exception ex) {
            error(response, 400, "Unsupported productType: " + productType);
            return null;
        }
    }

    private RegWalletInfo getCurrentCustomer(String auth, BaseResponse response) {
        String emailAddress = uttilityMethods.getClaimFromJwt(auth, "emailAddress");
        if (isBlank(emailAddress)) {
            error(response, 401, "Unauthorized");
            return null;
        }

        Optional<RegWalletInfo> currentCustomer = regWalletInfoRepository.findByEmail(emailAddress);
        if (!currentCustomer.isPresent()) {
            error(response, 404, "Customer not found.");
            return null;
        }
        return currentCustomer.get();
    }

    private String resolveName(RegWalletInfo walletInfo) {
        if (!isBlank(walletInfo.getFullName())) {
            return walletInfo.getFullName().trim();
        }
        String firstName = trimToNull(walletInfo.getFirstName());
        String lastName = trimToNull(walletInfo.getLastName());
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private boolean sameCustomer(RegWalletInfo left, RegWalletInfo right) {
        if (left == null || right == null) {
            return false;
        }
        if (!isBlank(left.getWalletId()) && !isBlank(right.getWalletId())) {
            return left.getWalletId().equalsIgnoreCase(right.getWalletId());
        }
        if (!isBlank(left.getEmail()) && !isBlank(right.getEmail())) {
            return left.getEmail().equalsIgnoreCase(right.getEmail());
        }
        return false;
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCurrencyCode(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BaseResponse ok(BaseResponse response, String description) {
        response.setStatusCode(200);
        response.setDescription(description);
        return response;
    }

    private BaseResponse error(BaseResponse response, int statusCode, String description) {
        response.setStatusCode(statusCode);
        response.setDescription(description);
        return response;
    }
}
