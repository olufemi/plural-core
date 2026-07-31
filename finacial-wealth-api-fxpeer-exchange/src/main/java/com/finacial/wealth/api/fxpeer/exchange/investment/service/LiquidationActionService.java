/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class LiquidationActionService {
    private static final Logger log = LoggerFactory.getLogger(LiquidationActionService.class);
    private static final String REDEMPTION_APPROVAL_MODE_CONFIG = "investment.redemption.approval-mode";
    private static final String REDEMPTION_AUTO_APPROVAL_THRESHOLD_CONFIG = "investment.redemption.auto-approval-threshold";
    private static final String REDEMPTION_SCHEDULER_ENABLED_CONFIG = "investment.redemption.scheduler-enabled";

    private final InvestmentOrderRepository orderRepo;
    private final AppConfigRepo appConfigRepo;
    private final UttilityMethods utilService; // your JWT claim extractor
    private final ActivityService activityService;
    private final InvestmentOrderService investmentService;
    private final InvestmentPositionRepository positionRepo;
    private final String redemptionApprovalMode;
    private final String autoApprovalThreshold;
    // or inject the class where onLiquidationSettled lives

    public LiquidationActionService(InvestmentOrderRepository orderRepo,
            AppConfigRepo appConfigRepo,
            UttilityMethods utilService,
            ActivityService activityService,
            InvestmentOrderService investmentService,
            InvestmentPositionRepository positionRepo,
            @Value("${investment.redemption.approval-mode:${fx.investment.liquidation.approval-mode:AUTO}}") String redemptionApprovalMode,
            @Value("${investment.redemption.auto-approval-threshold:${fx.investment.liquidation.auto-approval-threshold:0}}") String autoApprovalThreshold) {
        this.orderRepo = orderRepo;
        this.appConfigRepo = appConfigRepo;
        this.utilService = utilService;
        this.activityService = activityService;
        this.investmentService = investmentService;
        this.positionRepo = positionRepo;
        this.redemptionApprovalMode = redemptionApprovalMode;
        this.autoApprovalThreshold = autoApprovalThreshold;
    }
    


    @Transactional
    public void processPendingLiquidationsBatch() {

        List<InvestmentOrderStatus> statuses = Arrays.asList(
                InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL,
                InvestmentOrderStatus.LIQUIDATION_PROCESSING
        );

        List<InvestmentOrder> orders = orderRepo.findByTypeAndStatusIn(
                InvestmentOrderType.LIQUIDATION,
                statuses
        );

        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (InvestmentOrder order : orders) {

            try {
                // Lock each order (important for concurrency)
                InvestmentOrder lockedOrder = orderRepo.lockByOrderRef(order.getOrderRef()).orElse(null);
                if (lockedOrder == null) {
                    continue;
                }

                // Idempotency check
                if (lockedOrder.getStatus() == InvestmentOrderStatus.SETTLED) {
                    continue;
                }

                // Only process allowed states
                if (lockedOrder.getStatus() != InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL
                        && lockedOrder.getStatus() != InvestmentOrderStatus.LIQUIDATION_PROCESSING) {
                    continue;
                }

                if (lockedOrder.getStatus() == InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL
                        && !shouldAutoProcess(lockedOrder)) {
                    log.info("Liquidation orderRef={} left pending approval by approvalMode={}",
                            lockedOrder.getOrderRef(), normalizedApprovalMode());
                    continue;
                }

                // Move to PROCESSING (if coming from PENDING_APPROVAL)
                lockedOrder.setStatus(InvestmentOrderStatus.LIQUIDATION_PROCESSING);
                lockedOrder.setUpdatedAt(Instant.now());
                orderRepo.save(lockedOrder);

                // 🔥 Actual settlement
                investmentService.onLiquidationSettledInternal(lockedOrder);

            } catch (Exception ex) {
                // DO NOT fail whole batch
               // log.error("Failed processing liquidation orderRef={}", order.getOrderRef(), ex);

                // leave in PROCESSING for retry
            }
        }
    }

    public boolean isRedemptionSchedulerEnabled(boolean propertyFallback) {
        String configuredValue = appConfigValue(REDEMPTION_SCHEDULER_ENABLED_CONFIG, Boolean.toString(propertyFallback));
        return !"false".equalsIgnoreCase(configuredValue == null ? null : configuredValue.trim());
    }

    @Transactional
    public BaseResponse approveAndSettleLiquidation(String liquidationOrderRef) {

        BaseResponse res = new BaseResponse();

        if (liquidationOrderRef == null || liquidationOrderRef.trim().isEmpty()) {
            res.setStatusCode(400);
            res.setDescription("Liquidation order ref is required");
            res.setData(Collections.emptyMap());
            return res;
        }

        InvestmentOrder order = orderRepo.lockByOrderRef(liquidationOrderRef.trim()).orElse(null);
        if (order == null) {
            res.setStatusCode(404);
            res.setDescription("Liquidation order not found");
            res.setData(Collections.emptyMap());
            return res;
        }

        if (order.getType() != InvestmentOrderType.LIQUIDATION) {
            res.setStatusCode(400);
            res.setDescription("Order is not a liquidation order");
            res.setData(Collections.emptyMap());
            return res;
        }

        // Idempotency: already settled
        if (order.getStatus() == InvestmentOrderStatus.SETTLED) {
            res.setStatusCode(200);
            res.setDescription("Already settled");
            res.setData(Collections.emptyMap());
            return res;
        }

        // Only allow approve from pending approval (or allow processing for retry)
        if (order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL
                && order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PROCESSING) {
            res.setStatusCode(409);
            res.setDescription("Cannot approve/settle liquidation in state: " + order.getStatus());
            res.setData(Collections.emptyMap());
            return res;
        }

        // Mark as processing (audit trail)
        order.setStatus(InvestmentOrderStatus.LIQUIDATION_PROCESSING);
        order.setUpdatedAt(Instant.now());
        orderRepo.save(order);

        // Now settle immediately (this will lock position, credit wallet, update balances, SETTLED)
        BaseResponse settled = investmentService.onLiquidationSettledInternal(order); // internal method below
        return settled;
    }

    @Transactional
    public BaseResponse cancelLiquidation(String liquidationOrderRef) {

        BaseResponse res = new BaseResponse();
        int statusCode = 500;
        String description = "Something went wrong";

        try {
            statusCode = 400;

            /*String email = utilService.getClaimFromJwt(auth, "emailAddress");
            if (email == null || email.trim().isEmpty()) {
                res.setStatusCode(statusCode);
                res.setDescription("Invalid token: emailAddress claim missing");
                return res;
            }*/
            InvestmentOrder order = orderRepo.lockByOrderRef(liquidationOrderRef.trim())
                    .orElseThrow(() -> new NotFoundException("Liquidation order not found"));

            // Ownership check
            if (order.getEmailAddress() == null //|| !order.getEmailAddress().equalsIgnoreCase(email)
                    ) {
                res.setStatusCode(403);
                res.setDescription("You are not allowed to cancel this liquidation.");
                return res;
            }

            // Only cancel while the liquidation is still open
            if (order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL
                    && order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PROCESSING) {
                res.setStatusCode(statusCode);
                res.setDescription("Liquidation cannot be cancelled in current status: " + order.getStatus());
                return res;
            }

            InvestmentPosition position = order.getPosition();
            if (position != null && order.getAmount() != null && order.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                InvestmentPosition lockedPosition = positionRepo.lockById(position.getId())
                        .orElseThrow(() -> new NotFoundException("Investment position not found"));
                BigDecimal reserved = nvl(lockedPosition.getReservedLiquidationAmount());
                BigDecimal releaseAmount = order.getAmount().min(reserved);
                lockedPosition.setReservedLiquidationAmount(reserved.subtract(releaseAmount));
                lockedPosition.setUpdatedAt(Instant.now());
                positionRepo.save(lockedPosition);
            }

            // Cancel it
            order.setStatus(InvestmentOrderStatus.CANCELLED);
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);

            //activityService.logInvestmentLiquidationCancelled(order);
            res.setStatusCode(200);
            res.setDescription("Liquidation cancelled successfully.");
            return res;

        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(statusCode);
            res.setDescription(description);
            return res;
        }
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean shouldAutoProcess(InvestmentOrder order) {
        String mode = normalizedApprovalMode();
        if ("MANUAL".equals(mode)) {
            return false;
        }
        if ("THRESHOLD".equals(mode)) {
            return nvl(order.getAmount()).compareTo(autoApprovalThresholdAmount()) <= 0;
        }
        return true;
    }

    private String normalizedApprovalMode() {
        String configuredMode = appConfigValue(REDEMPTION_APPROVAL_MODE_CONFIG, redemptionApprovalMode);
        if (configuredMode == null || configuredMode.trim().isEmpty()) {
            return "AUTO";
        }
        String mode = configuredMode.trim().toUpperCase(Locale.ROOT);
        return ("MANUAL".equals(mode) || "THRESHOLD".equals(mode) || "AUTO".equals(mode)) ? mode : "AUTO";
    }

    private BigDecimal autoApprovalThresholdAmount() {
        String configuredThreshold = appConfigValue(REDEMPTION_AUTO_APPROVAL_THRESHOLD_CONFIG, autoApprovalThreshold);
        if (configuredThreshold == null || configuredThreshold.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(configuredThreshold.trim());
        } catch (NumberFormatException ex) {
            log.warn("Invalid redemption auto approval threshold '{}'; defaulting to 0", configuredThreshold);
            return BigDecimal.ZERO;
        }
    }

    private String appConfigValue(String configName, String fallback) {
        try {
            List<AppConfig> configs = appConfigRepo.findByConfigName(configName);
            if (configs != null && !configs.isEmpty() && configs.get(0).getConfigValue() != null
                    && !configs.get(0).getConfigValue().trim().isEmpty()) {
                return configs.get(0).getConfigValue();
            }
        } catch (Exception ex) {
            log.warn("Unable to read app_config {} for redemption policy; using property fallback", configName);
        }
        return fallback;
    }
}
