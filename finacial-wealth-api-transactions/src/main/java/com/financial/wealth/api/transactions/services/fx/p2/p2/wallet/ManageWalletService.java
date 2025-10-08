/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.fx.p2.p2.wallet;

import com.financial.wealth.api.transactions.domain.LocalTransFailedTransInfo;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SettlementFailureLogRepo;
import com.financial.wealth.api.transactions.services.LocalTransferService;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class ManageWalletService {

    private final SettlementFailureLogRepo settlementFailureLogRepo;
    private final LocalTransferService localTransferService;
    private final UttilityMethods utilMeth;
    private final WalletTransactionsDetailsRepo walletTransactionsDetailsRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;

    public ManageWalletService(SettlementFailureLogRepo settlementFailureLogRepo,
            LocalTransferService localTransferService,
            UttilityMethods utilMeth,
            WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            RegWalletInfoRepository regWalletInfoRepository) {
        this.settlementFailureLogRepo = settlementFailureLogRepo;
        this.localTransferService = localTransferService;
        this.utilMeth = utilMeth;
        this.walletTransactionsDetailsRepo = walletTransactionsDetailsRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
    }

    //get total balalnce
    public BaseResponse getAccountBal(WalletInfo rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String descripton = "Something went wrong internally";
        try {
            statusCode = 400;
            BaseResponse getTotalBal = localTransferService.getTotalBalByPhoneNumb(rq.getAccountNumber());

            if (getTotalBal.getStatusCode() != 200) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        getTotalBal.getDescription());
                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription(getTotalBal.getDescription());
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            Object amountObj = getTotalBal.getData().get("accountBalance");
            BigDecimal accBalAmount = BigDecimal.ZERO;

            if (amountObj instanceof BigDecimal) {
                accBalAmount = (BigDecimal) amountObj;
            } else if (amountObj instanceof Number) {
                accBalAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                accBalAmount = new BigDecimal((String) amountObj);
            } else {
                accBalAmount = BigDecimal.ZERO; // default or throw exception
            }

            System.out.println("Gotten receiver account balance: " + accBalAmount);
            responseModel.setDescription("Success!");
            responseModel.setStatusCode(200);
            Map mp = new HashMap();
            mp.put("accountBalance", accBalAmount);
            responseModel.setData(mp);
            return responseModel;
        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(descripton);
            responseModel.setStatusCode(statusCode);

        }
        return responseModel;
    }

    public BaseResponse validateAccountBalnce(WalletInfoValiAcctBal rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String descripton = "Something went wrong internally";
        try {
            statusCode = 400;

            WalletInfo ree = new WalletInfo();
            ree.setAccountNumber(rq.getAccountNumber());
            BaseResponse getTotalBal = this.getAccountBal(ree);

            Object amountObj = getTotalBal.getData().get("accountBalance");
            BigDecimal accBalAmount = BigDecimal.ZERO;

            if (amountObj instanceof BigDecimal) {
                accBalAmount = (BigDecimal) amountObj;
            } else if (amountObj instanceof Number) {
                accBalAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                accBalAmount = new BigDecimal((String) amountObj);
            } else {
                accBalAmount = BigDecimal.ZERO; // default or throw exception
            }

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accBalAmount) == 0) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                settlementFailureLogRepo.save(conWall);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (rq.getRequestedAmount().compareTo(accBalAmount) == 1) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Sorry, your account balance is insufficient. Your account balance is " + accBalAmount.toString());
                settlementFailureLogRepo.save(conWall);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Sorry, your account balance is insufficient. Your account balance is " + accBalAmount.toString());
                return responseModel;

            }

            responseModel.setDescription("Success!");
            responseModel.setStatusCode(200);
            Map mp = new HashMap();
            mp.put("accountBalance", accBalAmount);
            responseModel.setData(mp);
            return responseModel;
        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(descripton);
            responseModel.setStatusCode(statusCode);

        }
        return responseModel;
    }

    public BaseResponse createofferValidateAccount(WalletInfoValAcct rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String descripton = "Something went wrong internally";
        try {
            statusCode = 400;

            WalletInfo ree = new WalletInfo();
            ree.setAccountNumber(rq.getAccountNumber());
            BaseResponse getTotalBal = this.getAccountBal(ree);

            Object amountObj = getTotalBal.getData().get("accountBalance");
            BigDecimal accBalAmount = BigDecimal.ZERO;

            if (amountObj instanceof BigDecimal) {
                accBalAmount = (BigDecimal) amountObj;
            } else if (amountObj instanceof Number) {
                accBalAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                accBalAmount = new BigDecimal((String) amountObj);
            } else {
                accBalAmount = BigDecimal.ZERO; // default or throw exception
            }

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accBalAmount) == 0) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                settlementFailureLogRepo.save(conWall);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (rq.getTransactionAmmount().compareTo(accBalAmount) == 1) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Sorry, your account balance is insufficient. Your account balance is " + accBalAmount.toString());
                settlementFailureLogRepo.save(conWall);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Sorry, your account balance is insufficient. Your account balance is " + accBalAmount.toString());
                return responseModel;

            }
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByWalletIdOptional(rq.getWalletId());

            WalletTransactionsDetails logTrans = new WalletTransactionsDetails();
            logTrans.setAccountNumber(rq.getAccountNumber());
            //logTrans.setAmountPurchased(null);
           // BigDecimal avilBal = logTrans.getAvailableQuantity() == null ? BigDecimal.ZERO : logTrans.getAvailableQuantity();
            logTrans.setAvailableQuantity(rq.getTransactionAmmount());
            logTrans.setTotalQuantityCreated(rq.getTransactionAmmount());
            logTrans.setBuyerAccount("");
            logTrans.setBuyerId("");
            logTrans.setBuyerName("");
            logTrans.setCorrelationId(rq.getCorrelationId());
            logTrans.setCreatedBy("System");
            logTrans.setCreatedDate(Instant.now());
            logTrans.setCurrencyToBuy(rq.getCurrencyToBuy());
            logTrans.setCurrencyToSell(rq.getCurrencyToSell());
            logTrans.setSellerName(getRec.get().getFullName());
            logTrans.setTransactionId("");
            logTrans.setSellerId(rq.getWalletId());
            walletTransactionsDetailsRepo.save(logTrans);

            return responseModel;
        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(descripton);
            responseModel.setStatusCode(statusCode);

        }
        return responseModel;
    }

}
