/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.config;

import com.financial.wealth.api.transactions.services.WalletCreditRetryScheduler;
import com.financial.wealth.api.transactions.tranfaar.services.WebhookKeyService;
import java.util.Arrays;

import java.util.Calendar;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author HRH
 */
@Component
@EnableScheduling
public class SchedulerComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerComponent.class.getName());

    @Autowired
    WebhookKeyService webhookKeyService;

    @Autowired
    WalletCreditRetryScheduler walletCreditRetryScheduler;

    @Value("${sch.cron.disable}")
    private String disable;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    private boolean isDevProfile() {
        return Arrays.stream(activeProfiles.split(","))
                .map(String::trim)
                .anyMatch("dev"::equalsIgnoreCase);
    }

    //@Scheduled(fixedRate = 60 * 60 * 1000 * 3)
    @Scheduled(cron = "${pool.process.webhook.withdrawal.cron}")
    @SchedulerLock(name = "SchedulerComponent.processWebHookWithdrawal", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    public void processWebHookWithdrawal() {
        if (disable.equalsIgnoreCase("false") && isDevProfile()) {
            webhookKeyService.processWebHookWithdrawal();
            LOG.info("****** running scheduled processWebHookWithdrawal  >>> " + Calendar.getInstance().getTime() + "******");
        }
    }

    @Scheduled(cron = "${pool.process.webhook.deposit.cron}")
    @SchedulerLock(name = "SchedulerComponent.processWebHookDeposit", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    public void processWebHookDeposit() {
        if (disable.equalsIgnoreCase("false") && isDevProfile()) {
            webhookKeyService.processWebHookDeposit();
            LOG.info("****** running scheduled processWebHookDeposit  >>> " + Calendar.getInstance().getTime() + "******");
        }
    }

    // @Scheduled(cron = "${pool.process.retry.failed.credit.wallet.cron}")
    public void retryFailedCredits() {

        if (disable.equalsIgnoreCase("false")) {
            walletCreditRetryScheduler.retryFailedCredits();
            LOG.info("****** running scheduled retryFailedCredits  >>> " + Calendar.getInstance().getTime() + "******");
        }

    }

    // @Scheduled(cron = "${pool.process.retry.failed.debit.wallet.cron}")
    public void retryFailedDebits() {

        if (disable.equalsIgnoreCase("false")) {
            walletCreditRetryScheduler.retryFailedDebits();
            LOG.info("****** running scheduled retryFailedDebits  >>> " + Calendar.getInstance().getTime() + "******");
        }

    }

    private boolean shouldRunWebhookSchedulers() {
        if (!"false".equalsIgnoreCase(disable)) {
            return false;
        }
        if (activeProfiles == null || activeProfiles.trim().isEmpty()) {
            return false;
        }
        for (String profile : activeProfiles.split(",")) {
            if ("dev".equalsIgnoreCase(profile.trim())) {
                return true;
            }
        }
        return false;
    }
}
