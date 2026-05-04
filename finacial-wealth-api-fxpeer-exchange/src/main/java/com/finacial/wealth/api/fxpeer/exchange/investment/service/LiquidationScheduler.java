/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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

    public LiquidationScheduler(LiquidationActionService liquidationService) {
        this.liquidationService = liquidationService;
    }

    @Scheduled(cron = "${liquidation.scheduler.cron:0 */5 * * * *}")
    @SchedulerLock(name = "LiquidationScheduler.runLiquidationJob", lockAtMostFor = "15m", lockAtLeastFor = "30s")
    public void runLiquidationJob() {
        try {
            log.info("Running liquidation batch job...");
            liquidationService.processPendingLiquidationsBatch();
            log.info("Liquidation batch job completed.");
        } catch (Exception e) {
            log.error("Liquidation batch job failed", e);
        }
    }
}
