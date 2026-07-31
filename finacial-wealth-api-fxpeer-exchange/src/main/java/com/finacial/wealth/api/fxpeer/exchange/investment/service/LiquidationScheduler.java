/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author olufemioshin
 */
@Component
public class LiquidationScheduler {

    private static final Logger log = LoggerFactory.getLogger(LiquidationScheduler.class);

    private final LiquidationActionService liquidationService;
    private final boolean schedulerEnabled;

    public LiquidationScheduler(LiquidationActionService liquidationService,
            @Value("${investment.redemption.scheduler-enabled:${fx.investment.liquidation.scheduler-enabled:true}}") boolean schedulerEnabled) {
        this.liquidationService = liquidationService;
        this.schedulerEnabled = schedulerEnabled;
    }

    @Scheduled(cron = "${investment.redemption.scheduler-cron:${liquidation.scheduler.cron:${fx.investment.run.liquidation.scheduler.cron:0 */5 * * * *}}}")
    public void runLiquidationJob() {
        if (!schedulerEnabled) {
            log.debug("Liquidation batch job skipped because scheduler is disabled.");
            return;
        }
        try {
            log.info("Running liquidation batch job...");
            liquidationService.processPendingLiquidationsBatch();
            log.info("Liquidation batch job completed.");
        } catch (Exception e) {
            log.error("Liquidation batch job failed", e);
        }
    }
}
