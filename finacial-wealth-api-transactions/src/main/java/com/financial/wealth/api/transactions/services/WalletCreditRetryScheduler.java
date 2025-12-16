/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.CreateCreditLog;
import com.financial.wealth.api.transactions.domain.CreateDebitLog;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SuccessDebitLog;
import com.financial.wealth.api.transactions.enumm.CreditLogStatus;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.repo.CreateCreditLogRepository;
import com.financial.wealth.api.transactions.repo.CreateDebitLogRepository;
import com.financial.wealth.api.transactions.repo.FailedCreditLogRepo;
import com.financial.wealth.api.transactions.repo.FailedDebitLogRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SuccessDebitLogRepo;
import com.financial.wealth.api.transactions.utils.UttilityMethods;

import java.math.BigDecimal;

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
    private final SuccessDebitLogRepo successDebitLogRepo;
    private final CreateDebitLogRepository createDebitLogRepository;
    private final CreateCreditLogRepository ceateCreditLogRepository;

    public WalletCreditRetryScheduler(FailedCreditLogRepo failedCreditLogRepository,
            UttilityMethods utilMeth,
            RegWalletInfoRepository regWalletInfoRepository,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            FailedDebitLogRepo failedDebitLogRepo,
            SuccessDebitLogRepo successDebitLogRepo,
            CreateDebitLogRepository createDebitLogRepository,
            CreateCreditLogRepository ceateCreditLogRepository) {
        this.failedCreditLogRepository = failedCreditLogRepository;
        this.utilMeth = utilMeth;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.failedDebitLogRepo = failedDebitLogRepo;
        this.successDebitLogRepo = successDebitLogRepo;
        this.createDebitLogRepository = createDebitLogRepository;
        this.ceateCreditLogRepository = ceateCreditLogRepository;
    }

    //@Scheduled(fixedDelay = 60000) // retry every 1 minute
    public void retryFailedCredits() {
        System.out.println("schedule retryFailedCredits ::::::::::::::::  %S  ");

        List<CreateCreditLog> pendingLogs = ceateCreditLogRepository.findByResolvedFalse();

        for (CreateCreditLog log : pendingLogs) {
            try {

                CreditWalletCaller request = objectMapper.readValue(log.getRequestJson(), CreditWalletCaller.class);
                BaseResponse res = utilMeth.creditCustomer(request);

                if (res.getStatusCode() == 200) {
                    log.setResolved(true);
                    log.setStatus(CreditLogStatus.SUCCESS);
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

                ceateCreditLogRepository.save(log);
            } catch (Exception ex) {
                ex.printStackTrace(); // log if needed
            }
        }
    }

    // @Scheduled(fixedDelay = 60000) // retry every 1 minute
    public void retryFailedDebits() {
        System.out.println("schedule retryFailedDebits ::::::::::::::::  %S  ");
        List<CreateDebitLog> pendingLogs = createDebitLogRepository.findByResolvedFalse();

        for (CreateDebitLog log : pendingLogs) {
            try {
                if (!log.getStatus().equals(CreditLogStatus.PENDING)) {
                    CreditWalletCaller request = objectMapper.readValue(log.getRequestJson(), CreditWalletCaller.class);
                    BaseResponse res = utilMeth.creditCustomer(request);

                    if (res.getStatusCode() == 200) {
                        log.setResolved(true);
                        log.setStatus(CreditLogStatus.SUCCESS);
                        log.setLastModifiedDate(Instant.now());
                        if (log.getPayloadType().equals("CUSTOMER")) {

                        }
                    } else {
                        log.setRetryCount(log.getRetryCount() + 1);
                        log.setLastModifiedDate(Instant.now());
                    }

                    createDebitLogRepository.save(log);
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // log if needed
            }
        }
    }

    public void retrySuccessfulDebitsMarkForRoolBack() {
        System.out.println("schedule retrySuccessfulDebitsMarkForRoolBack ::::::::::::::::  %S");
        List<SuccessDebitLog> markForRollBackLogs = successDebitLogRepo.findByMarkForRollBack(1);

        for (SuccessDebitLog log : markForRollBackLogs) {
            try {
                // We stored a DEBIT payload; read it as DebitWalletCaller
                DebitWalletCaller originalDebit = objectMapper.readValue(log.getRequestJson(), DebitWalletCaller.class);

                // Build CREDIT request for rollback
                CreditWalletCaller rollbackReq = buildCreditFromDebit(originalDebit, log);

                BaseResponse res = utilMeth.creditCustomer(rollbackReq);

                if (res != null && res.getStatusCode() == 200) {
                    log.setMarkForRollBack(0);
                    log.setResolved(true);
                    log.setLastModifiedDate(Instant.now());
                } else {
                    log.setRetryCount(log.getRetryCount() + 1);
                    log.setLastModifiedDate(Instant.now());
                }

                successDebitLogRepo.save(log);
            } catch (Exception ex) {
                // bump retry and persist so the job can try again later
                log.setRetryCount(log.getRetryCount() + 1);
                log.setLastModifiedDate(Instant.now());
                successDebitLogRepo.save(log);
                ex.printStackTrace();
            }
        }
    }

    // helper: build a credit (rollback) request from an original debit request
    private CreditWalletCaller buildCreditFromDebit(DebitWalletCaller d, SuccessDebitLog log) {
        CreditWalletCaller c = new CreditWalletCaller();
        c.setAuth("Receiver");
        c.setFees(nz(d.getFees())); // same fees used on debit
        // use debit's final charges if set; otherwise use the trans amount
        String finalCharges = nz(d.getFinalCHarges()).isEmpty() ? nz(d.getTransAmount()) : nz(d.getFinalCHarges());
        c.setFinalCHarges(finalCharges);
        // credit back the same account that was debited
        c.setPhoneNumber(nz(d.getPhoneNumber()));
        c.setTransAmount(nz(d.getTransAmount()));
        // keep narration or tag as rollback if you prefer
        c.setNarration(nz(d.getNarration())); // e.g., nz(d.getNarration()) + "_ROLLBACK"
        // avoid duplicate IDs on the core by suffixing
        String baseId = log.getTransactionId() != null ? log.getTransactionId() : d.getTransactionId();
        c.setTransactionId((baseId == null ? "" : baseId) + "-RB");
        return c;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
