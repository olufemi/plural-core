/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Collections;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class LiquidationActionService {

    private final InvestmentOrderRepository orderRepo;
    private final UttilityMethods utilService; // your JWT claim extractor
    private final ActivityService activityService;
    private final InvestmentOrderService investmentService;
    // or inject the class where onLiquidationSettled lives

    public LiquidationActionService(InvestmentOrderRepository orderRepo,
            UttilityMethods utilService,
            ActivityService activityService,
            InvestmentOrderService investmentService) {
        this.orderRepo = orderRepo;
        this.utilService = utilService;
        this.activityService = activityService;
        this.investmentService = investmentService;
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
            InvestmentOrder order = orderRepo.findByOrderRef(liquidationOrderRef)
                    .orElseThrow(() -> new NotFoundException("Liquidation order not found"));

            // Ownership check
            if (order.getEmailAddress() == null //|| !order.getEmailAddress().equalsIgnoreCase(email)
                    ) {
                res.setStatusCode(403);
                res.setDescription("You are not allowed to cancel this liquidation.");
                return res;
            }

            // Only cancel when processing
            if (order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PROCESSING) {
                res.setStatusCode(statusCode);
                res.setDescription("Liquidation cannot be cancelled in current status: " + order.getStatus());
                return res;
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
}
