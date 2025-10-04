/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.financial.wealth.api.transactions.domain.FailedCreditLog;
import com.financial.wealth.api.transactions.domain.FailedDebitLog;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.repo.FailedCreditLogRepo;
import com.financial.wealth.api.transactions.repo.FailedDebitLogRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 *
 * @author olufemioshin
 */
@Service
public class WalletCreditRetryScheduler {

    private final FailedCreditLogRepo failedCreditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UttilityMethods utilMeth;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final FailedDebitLogRepo failedDebitLogRepo;

    public WalletCreditRetryScheduler(FailedCreditLogRepo failedCreditLogRepository,
            UttilityMethods utilMeth,
            RegWalletInfoRepository regWalletInfoRepository,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            FailedDebitLogRepo failedDebitLogRepo) {
        this.failedCreditLogRepository = failedCreditLogRepository;
        this.utilMeth = utilMeth;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.failedDebitLogRepo = failedDebitLogRepo;
    }

    //@Scheduled(fixedDelay = 60000) // retry every 1 minute
    public void retryFailedCredits() {
           System.out.println("schedule retryFailedCredits ::::::::::::::::  %S  " );

        List<FailedCreditLog> pendingLogs = failedCreditLogRepository.findByResolvedFalse();

        for (FailedCreditLog log : pendingLogs) {
            try {
                
                CreditWalletCaller request = objectMapper.readValue(log.getRequestJson(), CreditWalletCaller.class);
                BaseResponse res = utilMeth.creditCustomer(request);

                if (res.getStatusCode() == 200) {
                    log.setResolved(true);
                    log.setLastModifiedDate(Instant.now());
                    if (log.getPayloadType().equals("CUSTOMER")) {

                        FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                        kTrans2b.setAmmount(new BigDecimal(request.getFinalCHarges()));
                        kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                        kTrans2b.setFees(new BigDecimal(request.getFees()));
                        kTrans2b.setPaymentType("Deposit to Account");
                        kTrans2b.setReceiver(request.getPhoneNumber());
                        kTrans2b.setSender(request.getPhoneNumber());
                        kTrans2b.setTransactionId(request.getTransactionId());
                        kTrans2b.setSenderTransactionType("");
                        kTrans2b.setReceiverTransactionType("Deposit");

                        List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(request.getPhoneNumber());

                        kTrans2b.setWalletNo(request.getPhoneNumber());
                        kTrans2b.setReceiverName(getReceiverName.get(0).getFullName());
                        kTrans2b.setSenderName(getReceiverName.get(0).getFullName());
                        kTrans2b.setSentAmount(request.getFinalCHarges());
                        kTrans2b.setTheNarration("Deposit to account");

                        finWealthPaymentTransactionRepo.save(kTrans2b);
                    }
                } else {
                    log.setRetryCount(log.getRetryCount() + 1);
                    log.setLastModifiedDate(Instant.now());
                }

                failedCreditLogRepository.save(log);
            } catch (Exception ex) {
                ex.printStackTrace(); // log if needed
            }
        }
    }

   // @Scheduled(fixedDelay = 60000) // retry every 1 minute
    public void retryFailedDebits() {
         System.out.println("schedule retryFailedDebits ::::::::::::::::  %S  " );
        List<FailedDebitLog> pendingLogs = failedDebitLogRepo.findByResolvedFalse();

        for (FailedDebitLog log : pendingLogs) {
            try {
                DebitWalletCaller request = objectMapper.readValue(log.getRequestJson(), DebitWalletCaller.class);
                BaseResponse res = utilMeth.debitCustomer(request);

                if (res.getStatusCode() == 200) {
                    log.setResolved(true);
                    log.setLastModifiedDate(Instant.now());
                    if (log.getPayloadType().equals("CUSTOMER")) {

                    }
                } else {
                    log.setRetryCount(log.getRetryCount() + 1);
                    log.setLastModifiedDate(Instant.now());
                }

                failedDebitLogRepo.save(log);
            } catch (Exception ex) {
                ex.printStackTrace(); // log if needed
            }
        }
    }
}
