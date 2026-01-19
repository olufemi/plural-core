/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.transaction.Transactional;
import java.time.Instant;
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
    public BaseResponse approveLiquidation( String liquidationOrderRef) {

        BaseResponse res = new BaseResponse();
        int statusCode = 500;
        String description = "Something went wrong";

        try {
            statusCode = 400;

           /* String email = utilService.getClaimFromJwt(auth, "emailAddress");
            if (email == null || email.trim().isEmpty()) {
                res.setStatusCode(statusCode);
                res.setDescription("Invalid token: emailAddress claim missing");
                return res;
            }*/

            InvestmentOrder order = orderRepo.findByOrderRef(liquidationOrderRef)
                    .orElseThrow(() -> new NotFoundException("Liquidation order not found"));

            // Ownership check
            if (order.getEmailAddress() == null 
                   // || !order.getEmailAddress().equalsIgnoreCase(email)
                    ) {
                res.setStatusCode(403);
                res.setDescription("You are not allowed to approve this liquidation.");
                return res;
            }

            // Must be in processing to approve
            if (order.getStatus() != InvestmentOrderStatus.LIQUIDATION_PROCESSING) {
                res.setStatusCode(statusCode);
                res.setDescription("Liquidation cannot be approved in current status: " + order.getStatus());
                return res;
            }

            // Approve -> settle
            return investmentService.onLiquidationSettled(liquidationOrderRef);

        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(statusCode);
            res.setDescription(description);
            return res;
        }
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
            if (order.getEmailAddress() == null 
                    //|| !order.getEmailAddress().equalsIgnoreCase(email)
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

