/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.services.notify.FcmService;
import com.financial.wealth.api.transactions.domain.CommissionCfg;
import com.financial.wealth.api.transactions.domain.DeviceChangeLimitConfig;
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.FinWealthPayServiceConfig;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.GlobalLimitConfig;
import com.financial.wealth.api.transactions.domain.LocalBeneficiaries;
import com.financial.wealth.api.transactions.domain.LocalBeneficiariesIndividual;
import com.financial.wealth.api.transactions.domain.LocalTLogRetrialDebit;
import com.financial.wealth.api.transactions.domain.LocalTransFailedTransInfo;
import com.financial.wealth.api.transactions.domain.LocalTransferRequestLog;
import com.financial.wealth.api.transactions.domain.RegWalletCheckLog;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.domain.UserLimitConfig;
import com.financial.wealth.api.transactions.domain.WToWaletTransfer;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.models.FinWalletPaymentTransModel;
import com.financial.wealth.api.transactions.models.GetActBalPhoneNumber;
import com.financial.wealth.api.transactions.models.GetActBalReq;
import com.financial.wealth.api.transactions.models.LocalBeneficiariesFind;
import com.financial.wealth.api.transactions.models.LocalTransferRequest;
import com.financial.wealth.api.transactions.models.ProcLedgerRequestCreditOneTime;
import com.financial.wealth.api.transactions.models.ProcLedgerRequestDebitOneTime;
import com.financial.wealth.api.transactions.models.PushNotificationFireBase;
import com.financial.wealth.api.transactions.models.SaveBeneficiary;
import com.financial.wealth.api.transactions.models.WalletNoReq;
import com.financial.wealth.api.transactions.models.local.trans.NameLookUp;
import com.financial.wealth.api.transactions.repo.CommissionCfgRepo;
import com.financial.wealth.api.transactions.repo.DeviceChangeLimitConfigRepo;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPayServiceConfigRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.GlobalLimitConfigRepo;
import com.financial.wealth.api.transactions.repo.LocalBeneficiariesIndividualRepo;
import com.financial.wealth.api.transactions.repo.LocalBeneficiariesRepo;
import com.financial.wealth.api.transactions.repo.LocalTLogRetrialDebitRepo;
import com.financial.wealth.api.transactions.repo.LocalTransFailedTransInfoRepo;
import com.financial.wealth.api.transactions.repo.LocalTransferRequestLogRepo;
import com.financial.wealth.api.transactions.repo.RegWalletCheckLogRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.UserLimitConfigRepo;
import com.financial.wealth.api.transactions.repo.WToWaletTransferRepo;
import com.financial.wealth.api.transactions.services.notify.MessageCenterService;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.StrongAES;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author olufemioshin
 */
@Service
public class LocalTransferService {

    private final LocalTransFailedTransInfoRepo localTransFailedTransInfoRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final UserLimitConfigRepo userLimitConfigRepo;
    private final DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo;
    private final GlobalLimitConfigRepo globalLimitConfigRepo;
    private final FinWealthPayServiceConfigRepo kuleanPayServiceConfigRepo;
    private final CommissionCfgRepo commissionCfgRepo;
    private final RegWalletCheckLogRepo regWalletCheckLogRepo;
    private final LocalTransferRequestLogRepo localTransferRequestLogRepo;
    private final UttilityMethods utilMeth;
    private final WToWaletTransferRepo wToWaletTransferRepo;
    private final LocalBeneficiariesRepo localBeneficiariesRepo;
    long nowMillis = (Instant.now().toEpochMilli() / 1000);
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final LocalTLogRetrialDebitRepo localTLogRetrialDebitRepo;
    private final LocalBeneficiariesIndividualRepo localBeneficiariesIndividualRepo;
    private static final int DEFAULT_DEVICE_LIMIT_DAYS = 2;
    private final FcmService fcmService;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final MessageCenterService messageCenterService;
    private static final String CCY = "CAD";

    @Qualifier("withEureka")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    public LocalTransferService(LocalTransFailedTransInfoRepo localTransFailedTransInfoRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            UserLimitConfigRepo userLimitConfigRepo, DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo,
            GlobalLimitConfigRepo globalLimitConfigRepo,
            FinWealthPayServiceConfigRepo kuleanPayServiceConfigRepo,
            CommissionCfgRepo commissionCfgRepo, RegWalletCheckLogRepo regWalletCheckLogRepo,
            LocalTransferRequestLogRepo localTransferRequestLogRepo,
            UttilityMethods utilMeth, WToWaletTransferRepo wToWaletTransferRepo,
            LocalBeneficiariesRepo localBeneficiariesRepo,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            LocalTLogRetrialDebitRepo localTLogRetrialDebitRepo,
            LocalBeneficiariesIndividualRepo localBeneficiariesIndividualRepo,
            FcmService fcmService,
            DeviceDetailsRepo deviceDetailsRepo,
            MessageCenterService messageCenterService) {

        this.localTransFailedTransInfoRepo = localTransFailedTransInfoRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.userLimitConfigRepo = userLimitConfigRepo;
        this.deviceChangeLimitConfigRepo = deviceChangeLimitConfigRepo;
        this.globalLimitConfigRepo = globalLimitConfigRepo;
        this.kuleanPayServiceConfigRepo = kuleanPayServiceConfigRepo;
        this.commissionCfgRepo = commissionCfgRepo;
        this.regWalletCheckLogRepo = regWalletCheckLogRepo;
        this.localTransferRequestLogRepo = localTransferRequestLogRepo;
        this.utilMeth = utilMeth;
        this.wToWaletTransferRepo = wToWaletTransferRepo;
        this.localBeneficiariesRepo = localBeneficiariesRepo;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.localTLogRetrialDebitRepo = localTLogRetrialDebitRepo;
        this.localBeneficiariesIndividualRepo = localBeneficiariesIndividualRepo;
        this.fcmService = fcmService;
        this.deviceDetailsRepo = deviceDetailsRepo;
        this.messageCenterService = messageCenterService;
    }

    private static int parseDaysSafely(String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String safeStr(String str) {
        return (str == null) ? "0" : str;
    }

    public BaseResponse getTotalBal(String auth) {
        BaseResponse baseResponse = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            GetActBalReq reeeq = new GetActBalReq();
            reeeq.setAuth(auth);

            BaseResponse reqres = restTemplate.postForObject("http://" + "utilities-service" + "/walletmgt/account/balance",
                    reeeq, BaseResponse.class);
            if (reqres.getStatusCode() == HttpServletResponse.SC_OK) {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                baseResponse.setData(reqres.getData());
            } else {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception ex) {
            baseResponse.setDescription(statusMessage);
            baseResponse.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return baseResponse;

    }

    public BaseResponse getTotalBalByPhoneNumb(String phoneNumber) {
        BaseResponse baseResponse = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            GetActBalPhoneNumber reeeq = new GetActBalPhoneNumber();
            reeeq.setPhoneNumber(phoneNumber);

            BaseResponse reqres = restTemplate.postForObject("http://" + "utilities-service" + "/get-account-bal-phone",
                    reeeq, BaseResponse.class);
            if (reqres.getStatusCode() == HttpServletResponse.SC_OK) {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                baseResponse.setData(reqres.getData());
            } else {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception ex) {
            baseResponse.setDescription(statusMessage);
            baseResponse.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return baseResponse;

    }

    public static boolean betweenTransBand(BigDecimal i, BigDecimal minValueInclusive, BigDecimal maxValueInclusive) {
        return i.subtract(minValueInclusive).signum() >= 0 && i.subtract(maxValueInclusive).signum() <= 0;

    }

    public List<CommissionCfg> findAllByTransactionType(String transType) {

        List<CommissionCfg> getAllPartActiveNoti = commissionCfgRepo.findAllByTransactionType(transType);

        return getAllPartActiveNoti;
    }

    public BaseResponse nameLookUp(NameLookUp rq, String channel, String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            System.out.println("NameLookUp req :::::::: " + "    ::::::::::::::::::::: " + new Gson().toJson(rq));

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String processId = String.valueOf(GlobalMethods.generateTransactionId());
            boolean isWalletId = true;
            boolean isPhonenUmber = false;

            boolean isWalletIdSender = true;
            boolean isPhonenUmberSender = false;
            String receiverPhoneNumber = null;
            String senderPhoneNumber = null;
            List<RegWalletInfo> getReceiver = null;
            List<RegWalletInfo> getSender = null;

            if (GlobalMethods.isTenDigits(rq.getReceiver().trim()) == false) {
                isWalletId = false;
                isPhonenUmber = true;

            }

            /* if (!GlobalMethods.isElevenDigits(rq.getReceiver().trim()) == true) {

                isPhonenUmber = false;

            }*/
            System.out.println("isWalletIdBool :::::::: " + "     " + isWalletId);

            System.out.println("isPhonenUmberBool :::::::: " + "     " + isPhonenUmber);

            if (isWalletId == false && isPhonenUmber == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid Receiver");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            if (isWalletId) {

                System.out.println("isWalletId :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByWalletIdList(rq.getReceiver());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Receiver does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Receiver does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                receiverPhoneNumber = getReceiver.get(0).getPhoneNumber();
            }

            if (isPhonenUmber) {

                System.out.println("isPhonenUmber :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByPhoneNumberData(rq.getReceiver());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Receiver does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Receiver does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                receiverPhoneNumber = getReceiver.get(0).getPhoneNumber();

            }

            if (GlobalMethods.isTenDigits(rq.getSender().trim()) == false) {
                isWalletIdSender = false;
                isPhonenUmberSender = true;

            }

            System.out.println("iisWalletIdSenderBool :::::::: " + "     " + isWalletIdSender);

            System.out.println("isPhonenUmberSenderBool :::::::: " + "     " + isPhonenUmberSender);

            if (isWalletIdSender == false && isPhonenUmberSender == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid Sender");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            if (isWalletIdSender) {

                System.out.println("isWalletIdSender :::::::: " + "     ");

                getSender = regWalletInfoRepository.findByWalletIdList(rq.getSender());

                if (getSender.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                senderPhoneNumber = getSender.get(0).getPhoneNumber();
            }

            if (isPhonenUmberSender) {

                System.out.println("isPhonenUmberSender :::::::: " + "     ");

                getSender = regWalletInfoRepository.findByPhoneNumberData(rq.getSender());

                if (getSender.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                senderPhoneNumber = getSender.get(0).getPhoneNumber();

            }

            if (receiverPhoneNumber.equals(getDecoded.phoneNumber)) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, Customer cannot transfer to self!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, Customer cannot transfer to self!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            if (!getReceiver.get(0).isCompleted()) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Receiver has not completed registration!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Receiver has not completed registration!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            System.out.println("Phone number :::::::: " + "     " + getDecoded.phoneNumber);

            getSender = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            //System.out.println("getSender :::::::: " + "    ::::::::::::::::::::: " + new Gson().toJson(getSender));

            boolean isBeforeYesterday = false;

            //  System.out.println("Sender Request walletId :::::::: " + "     " + getSender.get(0).getWalletId());
            List<UserLimitConfig> userLimit1 = userLimitConfigRepo.findByWalletNumber(getSender.get(0).getWalletId());
            List<DeviceChangeLimitConfig> deviceLimit1 = deviceChangeLimitConfigRepo.findByWalletNumberList(getDecoded.phoneNumber);
            String getActiveCat1 = userLimit1.get(0).getTierCategory();
            if (deviceLimit1 != null && !deviceLimit1.isEmpty()) {
                final DeviceChangeLimitConfig dl = deviceLimit1.get(0);
                final Date refDate = (dl.getLastModifiedDate() != null) ? dl.getLastModifiedDate() : dl.getCreatedDate();

                final int days = parseDaysSafely(utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD(), DEFAULT_DEVICE_LIMIT_DAYS);

                System.out.println("Customer exists in the Device Limit Tbl ::::::::");
                System.out.println("Customer last device Date ::::::::      " + (refDate != null ? refDate : "<none>"));
                System.out.println("Configured no of Day(s) ::::::::        " + days);

                boolean tooOld = false;
                if (refDate != null) {
                    org.joda.time.DateTime ref = new org.joda.time.DateTime(refDate);
                    org.joda.time.DateTime cutoff = org.joda.time.DateTime.now().minusDays(days);
                    tooOld = ref.isBefore(cutoff);
                }

                System.out.println("Customer not within Limit days?? :::::::: " + tooOld);

                if (!tooOld && dl.getTierCategory() != null) {
                    getActiveCat1 = dl.getTierCategory();
                }
            }

            List<GlobalLimitConfig> getG1 = globalLimitConfigRepo.findByLimitCategory(getActiveCat1);

            BaseResponse getTotalBal = this.getTotalBal(auth);

            if (getTotalBal.getStatusCode() != 200) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", getTotalBal.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription(getTotalBal.getDescription());
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            Object amountObj = getTotalBal.getData().get("accountBalance");
            BigDecimal amount = BigDecimal.ZERO;

            if (amountObj instanceof BigDecimal) {
                amount = (BigDecimal) amountObj;
            } else if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                amount = BigDecimal.ZERO; // default or throw exception
            }

            System.out.println("Gotten account balance: " + amount);

            //get account balance
            BigDecimal accountBal1 = amount;

            //a.compareTo(b) 
            if (isBeforeYesterday == true) {
                if (accountBal1.compareTo(new BigDecimal(getG1.get(0).getMaximumBalance())) > 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to wALLET transfer - Sorry, your account balance is greater than your Tier's, kindly upgrade to higher Tier. Your maximum account balance is: " + getG1.get(0).getMaximumBalance(),
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    localTransFailedTransInfoRepo.save(procFailedTrans);

                    responseModel.setStatusCode(400);
                    responseModel.setDescription("Wallet to Wallet transfer - Sorry, your account balance is greater than your Tier's, kindly upgrade to higher Tier. Your maximum account balance is: " + getG1.get(0).getMaximumBalance());
                    return responseModel;

                }
            }

            //check maximum account balance
            List<UserLimitConfig> userLimit = userLimitConfigRepo.findByWalletNumber(getReceiver.get(0).getWalletId());
            List<DeviceChangeLimitConfig> deviceLimit = deviceChangeLimitConfigRepo.findByWalletNumberList(receiverPhoneNumber);
            String getActiveCat = userLimit.get(0).getTierCategory();
            if (deviceLimit.size() > 0) {
                //get lastModified date, check if it is above last configured day(s)
                System.out.println("Customer exists in the Device Limit Tbl :::::::: " + "     ");
                System.out.println("Customer last device Date :::::::: " + "     " + deviceLimit.get(0).getLastModifiedDate() == null ? deviceLimit.get(0).getCreatedDate() : deviceLimit.get(0).getLastModifiedDate());
                System.out.println("Configured no of Day(s) :::::::: " + "     " + utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD());

                isBeforeYesterday = new DateTime(deviceLimit.get(0).getLastModifiedDate() == null ? deviceLimit.get(0).getCreatedDate() : deviceLimit.get(0).getLastModifiedDate()).isBefore(DateTime.now().minusDays(Integer.valueOf(utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD())));
                System.out.println("Customer not within Limit days?? :::::::: " + "     " + isBeforeYesterday);
                if (isBeforeYesterday != true) {
                    getActiveCat = deviceLimit.get(0).getTierCategory();
                }

            }
            List<GlobalLimitConfig> getG = globalLimitConfigRepo.findByLimitCategory(getActiveCat);
            BigDecimal accountBalRec;
            BigDecimal getMaxAcctBal = new BigDecimal(getG.get(0).getMaximumBalance());
            // System.out.println("receiverPhoneNumber" + "    ::::::::::::::::::::: " + receiverPhoneNumber);

            BaseResponse getTotalBalRec = this.getTotalBalByPhoneNumb(receiverPhoneNumber);
            //  System.out.println("getTotalBalRec" + "    ::::::::::::::::::::: " + new Gson().toJson(getTotalBalRec));

            if (getTotalBalRec.getStatusCode() != 200) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", getTotalBalRec.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription(getTotalBal.getDescription());
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            Object amountObjRec = getTotalBalRec.getData().get("accountBalance");
            BigDecimal amountRec = BigDecimal.ZERO;

            if (amountObjRec instanceof BigDecimal) {
                amountRec = (BigDecimal) amountObjRec;
            } else if (amountObj instanceof Number) {
                amountRec = BigDecimal.valueOf(((Number) amountObjRec).doubleValue());
            } else if (amountObjRec instanceof String) {
                amountRec = new BigDecimal((String) amountObjRec);
            } else {
                amountRec = BigDecimal.ZERO; // default or throw exception
            }

            accountBalRec = amountRec;

            //get account balance
            BigDecimal newAcctBalance;

            newAcctBalance = accountBalRec.add(new BigDecimal(rq.getAmount()));

            System.out.println("current receiver AcctBalance :::::::: " + "     " + accountBalRec);
            System.out.println("new receiver  newAcctBalance:::::::: " + "     " + newAcctBalance);
            System.out.println("new receiver  getMaxAcctBal:::::::: " + "     " + getMaxAcctBal);
          
            if (newAcctBalance.compareTo(getMaxAcctBal) > 0) {
                responseModel.setStatusCode(400);
                System.out.println("Receiver newAcctBalance " + newAcctBalance + " will be greater than Maximuim bal: :::::::: " + "     " + newAcctBalance);

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry this transaction cannot be processed, Thank you.",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setDescription("Wallet to Wallet transfer - Sorry this transaction cannot be processed, Thank you.");
                return responseModel;
            }
            rq.setSender(senderPhoneNumber);
            System.out.println("getDecoded.phoneNumber :::::::: " + "     " + getDecoded.phoneNumber);
            System.out.println("rq.getSender() :::::::: " + "     " + rq.getSender());

            if (!rq.getSender().trim().equals(getDecoded.phoneNumber.trim())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid sender!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            List<FinWealthPayServiceConfig> getKulList = kuleanPayServiceConfigRepo.findByServiceTypeEnable("localtransfer");
            List<CommissionCfg> pullData = findAllByTransactionType("localtransfer");
            if (pullData.size() > 0) {
                if (pullData.isEmpty()) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Transaction Type does not exist!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );
                    localTransFailedTransInfoRepo.save(procFailedTrans);

                    responseModel.setStatusCode(400);
                    responseModel.setDescription("Transaction Type does not exist!");
                    return responseModel;

                }
            } else {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Transaction Type does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Transaction Type does not exist!");
                return responseModel;
            }

            boolean transTypeExist = false;

            for (CommissionCfg partData : pullData) {

                if (betweenTransBand(new BigDecimal(rq.getAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {

                    transTypeExist = true;
                }

            }

            if (transTypeExist == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Amount is not within the transaction band, please check!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);

                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Amount is not within the transaction band, please check!");
                return responseModel;
            }

            BigDecimal pFees = BigDecimal.ZERO;

            for (CommissionCfg partData : pullData) {
                if (getKulList.get(0).getServiceType().trim().equals(partData.getTransType())) {

                    if (betweenTransBand(new BigDecimal(rq.getAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {

                        //compute the fees
                        //1.8% + 100 (convenience fee)
                        System.out.println("rq.getAmount()" + "  :::::::::::::::::::::   " + rq.getAmount());
                        System.out.println("pullData.get(0).getFee()" + "  :::::::::::::::::::::   " + partData.getFee());

                        pFees = partData.getFee();
                        System.out.println("pFees" + "  :::::::::::::::::::::   " + pFees);
                        if (getKulList.size() <= 0) {
                            LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                                    "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, service type not configured!",
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Local-Transfer-Service"
                            );
                            localTransFailedTransInfoRepo.save(procFailedTrans);

                            responseModel.setDescription("Wallet to Wallet transfer, service type not configured!");
                            responseModel.setStatusCode(statusCode);

                            localTransFailedTransInfoRepo.save(procFailedTrans);
                            return responseModel;
                        }

                        if (!getKulList.get(0).isEnabled()) {

                            LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                                    "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, service type is disabled!",
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Local-Transfer-Service"
                            );

                            localTransFailedTransInfoRepo.save(procFailedTrans);

                            responseModel.setDescription("Wallet to Wallet transfer, service type is disabled!");
                            responseModel.setStatusCode(statusCode);

                            return responseModel;

                        }

                        Optional<FinWealthPayServiceConfig> getKul = kuleanPayServiceConfigRepo.findAllByServiceType("localtransfer");

                        String flagMinAmt = "amount cannot be less than N" + getKul.get().getMinimumAmmount() + ".00, please check!";
                        if (new BigDecimal(rq.getAmount()).compareTo(new BigDecimal(getKul.get().getMinimumAmmount())) == -1) {

                            LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                                    "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, " + flagMinAmt,
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Local-Transfer-Service"
                            );

                            localTransFailedTransInfoRepo.save(procFailedTrans);

                            responseModel.setDescription("Wallet to Wallet transfer, " + flagMinAmt);
                            responseModel.setStatusCode(statusCode);

                            return responseModel;
                        }

                        /* for (CommissionCfg partData : pullData) {
                // System.out.println("CommissionCfg partData : pullData agencyTransType.trim()  :::::::: " + "::::: " + agencyTransType.trim());
                // System.out.println("CommissionCfg partData : pullData pullData.get(0).getTransType()  :::::::: " + "::::: " + pullData.get(0).getTransType());

                if (getKul.get().getServiceType().trim().equals(pullData.get(0).getTransType())) {

                    if (!partData.getFee().toString().equals(rq.getFees())) {

                        LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                                "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, " + "invalid fees amount",
                                String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                "Local-Transfer-Service"
                        );

                        localTransFailedTransInfoRepo.save(procFailedTrans);

                        responseModel.setDescription("Wallet to Wallet transfer, " + "invalid fees amount");
                        responseModel.setStatusCode(statusCode);

                        return responseModel;

                    }
                }
            }*/
                        String receiverName;
                        //List<WalletTierVerifyBvn> getBvnName = walletTierVerifyBvnRepo.findByWalletNo(rq.getReceiver());
                        List<RegWalletCheckLog> logTransUpList = regWalletCheckLogRepo.findByPhoneNumberIdList(rq.getSender());
                        receiverName = getReceiver.get(0).getFirstName() + " " + getReceiver.get(0).getLastName();

                        if (logTransUpList.size() > 0) {

                            RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(rq.getSender());
                            logTransUp.setLTransServiceType(getKulList.get(0).getServiceType() == null ? "localtransfer" : getKulList.get(0).getServiceType());
                            logTransUp.setLTransSessAmount(new BigDecimal(rq.getAmount()));
                            logTransUp.setLTransSessFees(new BigDecimal(pFees.toString()));
                            logTransUp.setPhoneNumber(getDecoded.phoneNumber);
                            logTransUp.setLTransSessReceiverName(receiverName);
                            logTransUp.setLTransSessReceiverWalletNo(receiverPhoneNumber);
                            logTransUp.setLTransSessSenderWalletNo(getDecoded.phoneNumber);
                            logTransUp.setProcessId(processId);
                            logTransUp.setProcessIdStatus("1");
                            logTransUp.setLastModifiedDate(Instant.now());
                            logTransUp.setTheNarration(rq.getTheNarration());
                            regWalletCheckLogRepo.save(logTransUp);

                        } else {
                            RegWalletCheckLog logTransUp = new RegWalletCheckLog();
                            logTransUp.setLTransServiceType(getKulList.get(0).getServiceType() == null ? "localtransfer" : getKulList.get(0).getServiceType());
                            logTransUp.setLTransSessAmount(new BigDecimal(rq.getAmount()));
                            logTransUp.setLTransSessFees(new BigDecimal(pFees.toString()));

                            logTransUp.setLTransSessReceiverName(receiverName);
                            logTransUp.setLTransSessReceiverWalletNo(receiverPhoneNumber);
                            logTransUp.setLTransSessSenderWalletNo(getDecoded.phoneNumber);
                            logTransUp.setProcessId(processId);
                            logTransUp.setProcessIdStatus("1");
                            logTransUp.setPhoneNumber(getDecoded.phoneNumber);
                            logTransUp.setCreatedDate(Instant.now());
                            logTransUp.setTheNarration(rq.getTheNarration());
                            regWalletCheckLogRepo.save(logTransUp);

                        }

                        LocalTransferRequestLog reqLog = new LocalTransferRequestLog();
                        reqLog.setCreatedDate(Instant.now());
                        reqLog.setLTransServiceType(getKulList.get(0).getServiceType());
                        reqLog.setLTransSessAmount(rq.getAmount());
                        reqLog.setLTransSessFees(pFees.toString());
                        reqLog.setLTransSessReceiverName(receiverName);
                        reqLog.setLTransSessReceiverWalletNo(receiverPhoneNumber);
                        reqLog.setProcessId(processId);
                        reqLog.setProcessIdStatus("1");
                        reqLog.setProcessIdStatusDesc("in-progress");
                        reqLog.setRequestChannel(channel);
                        reqLog.setTheNarration(rq.getTheNarration());
                        localTransferRequestLogRepo.save(reqLog);

                        responseModel.addData("processId", processId);
                        responseModel.addData("transactionType", getKulList.get(0).getServiceType() == null ? "localtransfer" : getKulList.get(0).getServiceType());
                        responseModel.addData("fees", pFees.toString());
                        String amountToDebit = new BigDecimal(rq.getAmount()).add(pFees).toString();
                        responseModel.addData("amount", amountToDebit);
                        responseModel.addData("receiverName", receiverName);
                        responseModel.addData("receiver", rq.getReceiver());
                        responseModel.setDescription("Name lookup was successful.");
                        responseModel.setStatusCode(200);

                        return responseModel;

                    }
                }
            }

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse saveBeneficiary(SaveBeneficiary rq, String channel, String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            /*List<WToWaletTransfer> getExistRec = wToWaletTransferRepo.findBySenderAndReceiver(getDecoded.phoneNumber, rq.getBeneficiaryNo());
            if (getExistRec.size() > 0) {*/
            List<LocalBeneficiaries> getSavedBen = localBeneficiariesRepo.findByWalletNoByBeneficiaryActive(getDecoded.phoneNumber, rq.getBeneficiaryNo(), "1");
            if (getSavedBen.size() <= 0) {
                System.out.println("saveBeneficiary rq.getBeneficiaryName() :::::::: " + "  ::::::::::::::::::::: " + rq.getBeneficiaryName());
                System.out.println("saveBeneficiary rq.getBeneficiaryName().replaceAll() :::::::: " + "  ::::::::::::::::::::: " + rq.getBeneficiaryName().replaceAll("\\s+", ""));
                //String rmSpace = rq.getBeneficiaryName().replaceAll("\\s+", "");
                /*if (utilMeth.isAlpha(rq.getBeneficiaryName().replaceAll("\\s+", "")) == false) {
                        if (utilMeth.isAlphaNumeric(rq.getBeneficiaryName().replaceAll("\\s+", "")) == false) {
                            LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                                    "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Alias as name!",
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Local-Transfer-Service"
                            );

                            responseModel.setDescription("Wallet to Wallet transfer, invalid Alias as name!");
                            responseModel.setStatusCode(statusCode);

                            localTransFailedTransInfoRepo.save(procFailedTrans);
                            return responseModel;
                        }
                    }*/
                LocalBeneficiaries sBen = new LocalBeneficiaries();
                sBen.setBeneficiaryName(rq.getBeneficiaryName());
                sBen.setBeneficiaryNo(rq.getBeneficiaryNo());
                sBen.setBeneficiaryStatus("1");
                sBen.setTransactionCount(1);
                sBen.setCreatedDate(Instant.now());
                sBen.setWalletNo(getDecoded.phoneNumber);
                sBen.setRequestSource("Wallet-To-Wallet-Transfer");
                localBeneficiariesRepo.save(sBen);
            }
            responseModel.setDescription("Beneficiary saved successfully");
            responseModel.setStatusCode(200);

            /* } else {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Customer has not performed a successful transaction with Receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Customer has not performed a successful transaction with Receiver!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }*/
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
        //find frequentlyusedBeneficiaries
    }

    public ApiResponseModel findSavedBeneficiaries(String channel, String auth) {

        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            List<LocalBeneficiaries> getLocalBen = localBeneficiariesRepo.findByWalletNoActive(getDecoded.phoneNumber, "1");
            if (getLocalBen.size() <= 0) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Customer has no Beneficiary!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Customer has no Beneficiary!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            //LocalBeneficiariesFind
            List<Object> mapAll = new ArrayList<Object>();
            // AllKycLevelsData allData = new AllKycLevelsData();
            if (getLocalBen.size() > 0) {

                for (LocalBeneficiaries gConfig : getLocalBen) {

                    LocalBeneficiariesFind lData = new LocalBeneficiariesFind();

                    lData.setBeneficiaryName(gConfig.getBeneficiaryName());
                    lData.setBeneficiaryNo(gConfig.getBeneficiaryNo());
                    mapAll.add(lData);

                }

                responseModel.setDescription("List of Beneficiaries.");
                responseModel.setStatusCode(200);
                responseModel.setData(mapAll);

            } else {

                responseModel.setDescription("Customer has no Beneficiary!");
                responseModel.setStatusCode(statusCode);
            }
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse validateTransferOthers(LocalTransferRequest rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //validate pin (has user created pin?)
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (rq.getTransactionType().equals("debitwalletbillspayment")) {
                rq.setTransactionType("localtransfer");
            }
            if (!senderWalletdetails.get(0).isActivation()) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Customer has not created PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //validate pin (is pin valid?)

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = senderWalletdetails.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, invalid PIN!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            //check if user has validate emailaddress
            if (!senderWalletdetails.get(0).isEmailVerification()) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Customer has not activated email address!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Customer has not activated email address!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //process limits

            List<UserLimitConfig> userLimit = userLimitConfigRepo.findByWalletNumber(senderWalletdetails.get(0).getWalletId());
            List<DeviceChangeLimitConfig> deviceLimit = deviceChangeLimitConfigRepo.findByWalletNumberList(senderWalletdetails.get(0).getWalletId());
            String getActiveCat = userLimit.get(0).getTierCategory();
            if (deviceLimit.size() > 0) {
                //get lastModified date, check if it is above last configured day(s)
                System.out.println("Sender exists in the Device Limit Tbl :::::::: " + "     ");
                System.out.println("Sender last device Date :::::::: " + "     " + deviceLimit.get(0).getLastModifiedDate());
                System.out.println("Sender last device created Date :::::::: " + "     " + deviceLimit.get(0).getCreatedDate());

                System.out.println("Configured no of Day(s) :::::::: " + "     " + utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD());
//(str == null) ? "0" : str
                boolean isBeforeYesterday = new DateTime(deviceLimit.get(0).getLastModifiedDate() == null ? deviceLimit.get(0).getCreatedDate() : deviceLimit.get(0).getLastModifiedDate()).isBefore(DateTime.now().minusDays(Integer.valueOf(utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD())));
                System.out.println("Customer not within Limit days?? :::::::: " + "     " + isBeforeYesterday);
                if (isBeforeYesterday != true) {
                    getActiveCat = deviceLimit.get(0).getTierCategory();
                }

            }
            List<GlobalLimitConfig> getG = globalLimitConfigRepo.findByLimitCategory(getActiveCat);
            String transType = (rq.getTransactionType() != null) ? rq.getTransactionType() : "localtransfer";
            BigDecimal cummulative = BigDecimal.ZERO;
            BigDecimal transTypeCummulative = BigDecimal.ZERO;
            BigDecimal configAmount = BigDecimal.ZERO;
            BigDecimal singleLimit = BigDecimal.ZERO;

            BigDecimal configuredReceiverMaxBalance = BigDecimal.ZERO;
            BigDecimal transAmt = (!StringUtils.isEmpty(rq.getFees())) ? new BigDecimal(rq.getAmount()).add(new BigDecimal(rq.getFees())) : new BigDecimal(rq.getAmount());
            BigDecimal transAmount = BigDecimal.ZERO;
            List<RegWalletCheckLog> getNameLookUpDe = regWalletCheckLogRepo.findByProcessIdList(rq.getProcessId());
            BaseResponse getTotalBal = this.getTotalBalByPhoneNumb(rq.getReceiver());

            if (getTotalBal.getStatusCode() != 200) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", getTotalBal.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription(getTotalBal.getDescription());
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
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

            //(receiver maximum amount limit)
            List<GlobalLimitConfig> getReciverG = null;
            List<RegWalletInfo> receiverWalletdetails = regWalletInfoRepository.findByPhoneNumberData(rq.getReceiver());
            if (!receiverWalletdetails.get(0).isEmailVerification()) {
                System.out.println("!receiverWalletdetails.get(0).isEmailVerification() :::::::: ");

                getReciverG = globalLimitConfigRepo.findByLimitCategory(utilMeth.getTier1());
                configuredReceiverMaxBalance = new BigDecimal(getReciverG.get(0).getMaximumBalance());

                System.out.println("genLedCum.get(0).getTotalBalance():::::::: " + "     " + accBalAmount);

                System.out.println("configuredReceiverMaxBalance :::::::: " + "     " + configuredReceiverMaxBalance);
                System.out.println("genLedCum.get(0).getTotalBalance().compareTo() :::::::: " + "     " + accBalAmount.compareTo(configuredReceiverMaxBalance));

                if (accBalAmount.compareTo(configuredReceiverMaxBalance) == 1) {
                    responseModel.setStatusCode(statusCode);
                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, your tier's maximum amount is:  " + configuredReceiverMaxBalance + ", please check!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );
                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    responseModel.setDescription("Wallet to Wallet transfer, Receiver tier's maximum amount is:  " + configuredReceiverMaxBalance + ", please check!");
                    return responseModel;
                }

            } else {
                List<UserLimitConfig> userLimitReceiver = userLimitConfigRepo.findByWalletNumber(receiverWalletdetails.get(0).getWalletId());
                List<DeviceChangeLimitConfig> deviceLimitRec = deviceChangeLimitConfigRepo.findByWalletNumberList(receiverWalletdetails.get(0).getWalletId());
                String getActiveCatRec = userLimitReceiver.get(0).getTierCategory();
                if (deviceLimitRec.size() > 0) {
                    //get lastModified date, check if it is above last configured day(s)
                    System.out.println("Receiver exists in the Device Limit Tbl :::::::: " + "     ");
                    System.out.println("Receiver last device Date :::::::: " + "     " + deviceLimitRec.get(0).getLastModifiedDate());
                    System.out.println("Receiver last device created Date :::::::: " + "     " + deviceLimitRec.get(0).getCreatedDate());

                    System.out.println("Configured no of Day(s) :::::::: " + "     " + utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD());

                    boolean isBeforeYesterday = new DateTime(deviceLimitRec.get(0).getLastModifiedDate() == null ? deviceLimitRec.get(0).getCreatedDate() : deviceLimitRec.get(0).getLastModifiedDate()).isBefore(DateTime.now().minusDays(Integer.valueOf(utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD())));
                    System.out.println("Receiver not within Limit days :::::::: " + "     " + isBeforeYesterday);
                    if (isBeforeYesterday != true) {
                        getActiveCatRec = deviceLimitRec.get(0).getTierCategory();
                    }

                }
                getReciverG = globalLimitConfigRepo.findByLimitCategory(getActiveCatRec);
                configuredReceiverMaxBalance = new BigDecimal(getReciverG.get(0).getMaximumBalance());

                System.out.println("configuredReceiverMaxBalance :::::::: " + "     " + configuredReceiverMaxBalance);
                System.out.println("genLedCum.get(0).getTotalBalance().compareTo() :::::::: " + "     " + accBalAmount.compareTo(configuredReceiverMaxBalance));

                if (accBalAmount.compareTo(configuredReceiverMaxBalance) == 1) {
                    responseModel.setStatusCode(statusCode);
                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, your tier's maximum amount is:  " + configuredReceiverMaxBalance + ", please check!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );
                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    responseModel.setDescription("Wallet to Wallet transfer, Receiver tier's maximum amount is:  " + configuredReceiverMaxBalance + ", please check!");
                    return responseModel;
                }

            }

            switch (transType) {
                case "localtransfer":
                    transAmount = transAmt;
                    transTypeCummulative = new BigDecimal(safeStr(getNameLookUpDe.get(0).getWalletTransferCumm()));
                    cummulative = transTypeCummulative.add(transAmount);
                    //getNameLookUpDe.get(0).setWalletTransferCumm(cummulative.toString());
                    configAmount = new BigDecimal(safeStr(getG.get(0).getDailyLimit()));
                    singleLimit = new BigDecimal(safeStr(getG.get(0).getWalletSingleTransfer()));

                    break;
                default:
                    responseModel.setStatusCode(400);
                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, transaction not found!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );
                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    responseModel.setDescription("Wallet to Wallet transfer, transaction not found");
                    return responseModel;
            }
            //(single transaction limit)
            //(daily transaction limit)

            BaseResponse getTotalBalSender = this.getTotalBalByPhoneNumb(rq.getSender());

            if (getTotalBalSender.getStatusCode() != 200) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", getTotalBalSender.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription(getTotalBalSender.getDescription());
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            Object amountObjSender = getTotalBalSender.getData().get("accountBalance");
            BigDecimal accBalAmountSender = BigDecimal.ZERO;

            if (amountObjSender instanceof BigDecimal) {
                accBalAmountSender = (BigDecimal) amountObjSender;
            } else if (amountObjSender instanceof Number) {
                accBalAmountSender = BigDecimal.valueOf(((Number) amountObjSender).doubleValue());
            } else if (amountObjSender instanceof String) {
                accBalAmountSender = new BigDecimal((String) amountObjSender);
            } else {
                accBalAmountSender = BigDecimal.ZERO; // default or throw exception
            }

            System.out.println("Gotten sender account balance: " + accBalAmountSender);

            //get account balance
            BigDecimal accountBal = accBalAmountSender;

            //a.compareTo(b) 
            accountBal = accBalAmountSender;
            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 1) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 0) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your minimum account balance is:  " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 1) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Wallet transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 0) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry insufficient fund, Your minimum account balance is:  " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Wallet transfer - Sorry insufficient fund, Your minimum account balance is:  " + utilMeth.minAcctBalance());
                return responseModel;

            }

            // ensure config amount is set
            if (configAmount.equals(BigDecimal.ZERO)) {
                responseModel.setStatusCode(400);
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Limit not configured!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setDescription("Limit not configured");
                return responseModel;
            }

            // single limit check
            if (transAmount.compareTo(singleLimit) > 0) {
                responseModel.setStatusCode(400);
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer - Sorry, your transfer amount exceed single transfer limit of " + singleLimit.toString(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);

                responseModel.setDescription("Wallet to Wallet transfer - Sorry, your transfer amount exceed single transfer limit of " + singleLimit.toString());
                return responseModel;
            }

            //return when cummulative amount is greater than config amount
            System.out.println("wallet-wallet cummulative:::::::: req" + "   >>>>>>>>>>>>>>>>>>  " + cummulative);
            System.out.println("wallet-wallet configAmount:::::::: req" + "   >>>>>>>>>>>>>>>>>>  " + configAmount);
            if (cummulative.compareTo(configAmount) > 0) {
                responseModel.setStatusCode(400);
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, you Have Exceeded Your Daily Transaction Limit!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setDescription("Wallet to Wallet transfer, you have Exceeded Your Daily Transaction Limit!");
                return responseModel;
            }

            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse processTransfer(LocalTransferRequest rq, String channel, String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String processId = String.valueOf(GlobalMethods.generateTransactionId());
            boolean isWalletId = true;

            boolean isPhonenUmber = false;
            String receiverPhoneNumber = null;
            boolean isWalletIdSender = true;
            boolean isPhonenUmberSender = false;
            String senderPhoneNumber = null;
            List<RegWalletInfo> getSender = null;
            List<RegWalletInfo> getReceiver = null;

            if (!regWalletCheckLogRepo.existsByProcessId(rq.getProcessId())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Transaction has not been initiated!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Transaction has not been initiated!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            if (GlobalMethods.isTenDigits(rq.getReceiver().trim()) == false) {
                isWalletId = false;
                isPhonenUmber = true;

            }

            /* if (!GlobalMethods.isElevenDigits(rq.getReceiver().trim()) == true) {

                isPhonenUmber = false;

            }*/
            System.out.println("isWalletIdBool :::::::: " + "     " + isWalletId);

            System.out.println("isPhonenUmberBool :::::::: " + "     " + isPhonenUmber);

            if (isWalletId == false && isPhonenUmber == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid Receiver");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            if (isWalletId) {

                System.out.println("isWalletId :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByWalletIdList(rq.getReceiver());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Receiver does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Receiver does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                receiverPhoneNumber = getReceiver.get(0).getPhoneNumber();
            }

            if (isPhonenUmber) {

                System.out.println("isPhonenUmber :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByPhoneNumberData(rq.getReceiver());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Receiver does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Receiver does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                receiverPhoneNumber = getReceiver.get(0).getPhoneNumber();

            }

            if (GlobalMethods.isTenDigits(rq.getSender().trim()) == false) {
                isWalletIdSender = false;
                isPhonenUmberSender = true;

            }

            System.out.println("iisWalletIdSenderBool :::::::: " + "     " + isWalletIdSender);

            System.out.println("isPhonenUmberSenderBool :::::::: " + "     " + isPhonenUmberSender);

            if (isWalletIdSender == false && isPhonenUmberSender == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid Sender");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            if (isWalletIdSender) {

                System.out.println("isWalletIdSender :::::::: " + "     ");

                getSender = regWalletInfoRepository.findByWalletIdList(rq.getSender());

                if (getSender.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                senderPhoneNumber = getSender.get(0).getPhoneNumber();
            }

            if (isPhonenUmberSender) {

                System.out.println("isPhonenUmberSender :::::::: " + "     ");

                getSender = regWalletInfoRepository.findByPhoneNumberData(rq.getSender());

                if (getSender.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                senderPhoneNumber = getSender.get(0).getPhoneNumber();

            }

            rq.setSender(senderPhoneNumber);

            String str1 = rq.getAmount().replaceAll(",", "");
            System.out.println("remove comma fro amount " + "  ::::::::::::::::::::: " + str1);

            rq.setAmount(str1);

            List<RegWalletCheckLog> getNameLookUpDe = regWalletCheckLogRepo.findByProcessIdList(rq.getProcessId());

            if (!getNameLookUpDe.get(0).getLTransSessSenderWalletNo().equals(getDecoded.phoneNumber)) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Transaction is invalid!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Transaction is invalid!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            // System.out.println("getNameLookUpDe.get(0).getLTransServiceType() " + "  ::::::::::::::::::::: " + getNameLookUpDe.get(0).getLTransServiceType());
            Optional<FinWealthPayServiceConfig> getKul = kuleanPayServiceConfigRepo.findAllByServiceType(getNameLookUpDe.get(0).getLTransServiceType());
            BigDecimal kulFees = BigDecimal.ZERO;

            // System.out.println("getKul.get().getMinimumAmmount() " + "  ::::::::::::::::::::: " + getKul.get().getMinimumAmmount());
            //System.out.println("processTransfer rq.getAmount() " + "  ::::::::::::::::::::: " + rq.getAmount());
            //check minimum amount
            String flagMinAmt = "amount cannot be less than N" + getKul.get().getMinimumAmmount() + ".00, please check!";
            if (new BigDecimal(rq.getAmount()).compareTo(new BigDecimal(getKul.get().getMinimumAmmount())) == -1) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, " + flagMinAmt,
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, " + flagMinAmt);
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            //check if sender is the same
            if (!getNameLookUpDe.get(0).getLTransSessSenderWalletNo().equals(rq.getSender())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, sender!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            rq.setReceiver(receiverPhoneNumber);
            //check if receiver is the same
            if (!getNameLookUpDe.get(0).getLTransSessReceiverWalletNo().equals(rq.getReceiver())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, receiver!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            //check if amount is the same
            // System.out.println("getNameLookUpDe.get(0).getLTransSessAmount() req " + "  ::::::::::::::::::::: " + getNameLookUpDe.get(0).getLTransSessAmount());
            // System.out.println("rq.getAmount() " + "  ::::::::::::::::::::: " + rq.getAmount());
            String getNameLookUpDeAmount = getNameLookUpDe.get(0).getLTransSessAmount().toString();
            String amount = rq.getAmount().trim();
            // System.out.println("getNameLookUpDeAmount " + "  ::::::::::::::::::::: " + getNameLookUpDeAmount);
            // System.out.println("amount " + "  ::::::::::::::::::::: " + amount);

            String sanitizedAmount = rq.getAmount() == null ? "0" : rq.getAmount().trim().replace(",", "");
            BigDecimal requestAmount = new BigDecimal(sanitizedAmount);
            BigDecimal expectedAmount = getNameLookUpDe.get(0).getLTransSessAmount();

            if (expectedAmount.compareTo(requestAmount) != 0) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, amount!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, amount!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //check if fees is the same
            String sanitizedFees = rq.getFees() == null ? "0" : rq.getFees().trim().replace(",", "");
            BigDecimal requestFees = new BigDecimal(sanitizedFees);
            BigDecimal expectedFees = getNameLookUpDe.get(0).getLTransSessFees();

            if (expectedFees.compareTo(requestFees) != 0) {
                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, fees!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, fees!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //check if receiver name is the same
            if (!getNameLookUpDe.get(0).getLTransSessReceiverName().equals(rq.getReceiverName())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, receiver name!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, receiver name!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!getNameLookUpDe.get(0).getLTransServiceType().equals(rq.getTransactionType())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, transaction type!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, transaction type!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            if (utilMeth.getIfNO_PERMITTED_TRASANCTION(getDecoded.phoneNumber)) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Transaction failed, please contact kuleanpaysupport@thefifthlab.com!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Transaction failed, please contact kuleanpaysupport@thefifthlab.com");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            //check if processIdStatus is 2 -> if 2 transaction has completed
            /* if (getNameLookUpDe.get(0).getProcessIdStatus().equals("2")) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, transaction has already completed!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, transaction has already completed!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }*/
            //check if processIdStatus is 1 -> and request is within 2 mins, existing transaction is still running
            System.out.println("Current time nowMillis  ::::::::::::::::               ::::: %S  " + nowMillis);
            if (getNameLookUpDe.get(0).getProcessIdStatus().equals("2")) {
                if (getNameLookUpDe.get(0).getLTransSessExpiry() < nowMillis) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, duplicate transaction, please try again in " + utilMeth.ltExistingRunningWindow() + " minutes!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, duplicate transaction, please try again in " + utilMeth.ltExistingRunningWindow() + " minutes!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
            }

            kulFees = new BigDecimal(rq.getFees());

            BigDecimal amountToCredit = new BigDecimal(rq.getAmount());
            BigDecimal amountToDebit = new BigDecimal(rq.getAmount()).add(kulFees);
            rq.setAmount(amountToDebit.toString());

            //  System.out.println("final amount to Check " + "  ::::::::::::::::::::: " + rq.getAmount());
            BaseResponse secondCheck = validateTransferOthers(rq, channel, auth);

            //System.out.println("secondCheck " + "  ::::::::::::::::::::: " + secondCheck);
            if (secondCheck.getStatusCode() != 200) {

                responseModel.setDescription(secondCheck.getDescription());
                responseModel.setStatusCode(secondCheck.getStatusCode());
                return responseModel;

            }
            rq.setAmount(amountToCredit.toString());

            getSender = regWalletInfoRepository.findByPhoneNumberData(rq.getSender());
            //List<RegWalletInfo> getReceiver = regWalletInfoRepository.findByPhoneNumberData(rq.getReceiver());

            String senderName;
//            if (getBvnName.size() > 0) {
//
//                senderName = getBvnName.get(0).getFirstName() + " " + getBvnName.get(0).getLastName();
//
//            } else {
//                senderName = getSender.get(0).getFirstName();
//            }
            senderName = getSender.get(0).getFirstName() + " " + getSender.get(0).getLastName();
            //   System.out.println("senderName " + "  ::::::::::::::::::::: " + senderName);

            String getnarration;

            if (getNameLookUpDe.get(0).getTheNarration() == null || getNameLookUpDe.get(0).getTheNarration().isEmpty()) {
                getnarration = "Wallet to Wallet Transfer";
            } else {

                getnarration = getNameLookUpDe.get(0).getTheNarration();

            }

            String narration = "TRF/" + getnarration + "/FRM " + senderName + " TO "
                    + getNameLookUpDe.get(0).getLTransSessReceiverName();

            // System.out.println("narration " + "  ::::::::::::::::::::: " + narration);
            if (wToWaletTransferRepo.existsByTransactionId(rq.getProcessId())) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, transaction has already completed, please check the processId!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, transaction has already completed!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            //handle save and spend
            //check if user has the product
            BigDecimal procPercent = BigDecimal.ZERO;

            //I call ledger api to credit receiver 
            //and to debit sender
            BigDecimal finalChrges = new BigDecimal(rq.getAmount()).add(procPercent);
            ProcLedgerRequestDebitOneTime reqq = new ProcLedgerRequestDebitOneTime();
            reqq.setDescription("Debit account");
            reqq.setFinalCharges(finalChrges.toString());
            reqq.setPhonenumber(rq.getSender());
            reqq.setKuleanFess(getKul.get().getFees());
            reqq.setNarration(narration);
            reqq.setTransactionId(rq.getProcessId());
            //System.out.println("debitAcct :::::::: reqq" + "    ::::::::::::::::::::: " + new Gson().toJson(reqq));
            DebitWalletCaller rqD = new DebitWalletCaller();
            rqD.setAuth(auth);
            rqD.setFees(reqq.getKuleanFess());
            rqD.setFinalCHarges(reqq.getFinalCharges());
            rqD.setNarration(reqq.getNarration());
            rqD.setPhoneNumber(reqq.getPhonenumber());
            rqD.setTransAmount(rq.getAmount());
            rqD.setTransactionId(rq.getProcessId());
            BaseResponse debitAcct = utilMeth.debitCustomerWithType(rqD, "CUSTOMER", CCY);

            //   System.out.println("Debit Response from core ::::::::::::::::  %S  " + new Gson().toJson(debitAcct));
            // BaseResponse debitAcct = genLedgerProxy.debitOneTime(reqq);
            //    System.out.println("verify local transfer in long expiry" + "   ::::::::::::::::::::: " + utilMeth.ltExistingRunningWindow());
            LocalDateTime expireMinutes = LocalDateTime.now().plusMinutes(Long.valueOf(utilMeth.ltExistingRunningWindow()));
            long expiry = Timestamp.valueOf(expireMinutes).getTime();

            //   System.out.println("verify locat transfer in long expiry" + "   ::::::::::::::::::::: " + expiry);
            if (debitAcct.getStatusCode() == 200) {
                DebitWalletCaller debGLCredit = new DebitWalletCaller();
                debGLCredit.setAuth("Sender");
                debGLCredit.setFees("0.00");
                debGLCredit.setFinalCHarges(amount);
                debGLCredit.setNarration("CAD_Withdrawal");
                debGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
                debGLCredit.setTransAmount(amount);
                debGLCredit.setTransactionId(rq.getProcessId() + "-CAD_GL");

                utilMeth.debitCustomerWithType(debGLCredit, "CAD_GL", CCY);
                /* KuleanPaymentTransaction kTrans = new KuleanPaymentTransaction();
                kTrans.setAmmount(amountToDebit);
                kTrans.setCreatedDate(Instant.now());
                kTrans.setFees(new BigDecimal(getKul.get().getFees()));
                kTrans.setPaymentType("Wallet to Wallet Transfer");
                kTrans.setReceiver(rq.getReceiver());
                kTrans.setSender(getDecoded.phoneNumber);
                kTrans.setTransactionId(rq.getProcessId());
                kTrans.setTransactionType("Withdrawal");
                kTrans.setWalletNo(getDecoded.phoneNumber);
                kTrans.setReceiverName(rq.getReceiverName());
                kTrans.setSenderName(senderName);
                kTrans.setSentAmount(amountToCredit.toString());
                processKuleanPaymentTransactionLedger(kTrans);

                KuleanPaymentTransaction kTrans1a = new KuleanPaymentTransaction();
                kTrans1a.setAmmount(amountToDebit);
                kTrans1a.setCreatedDate(Instant.now());
                kTrans1a.setFees(new BigDecimal(getKul.get().getFees()));
                kTrans1a.setPaymentType("Wallet to Wallet Transfer");
                kTrans1a.setReceiver(rq.getReceiver());
                kTrans1a.setSender(getDecoded.phoneNumber);
                kTrans1a.setTransactionId(rq.getProcessId());
                kTrans1a.setTransactionType("Withdrawal");
                kTrans1a.setWalletNo(getDecoded.phoneNumber);
                kTrans1a.setReceiverName(rq.getReceiverName());
                kTrans1a.setSenderName(senderName);
                kTrans1a.setSentAmount(amountToCredit.toString());

                kuleanPaymentTransactionRepo.save(kTrans1a);*/
                ProcLedgerRequestCreditOneTime rqq = new ProcLedgerRequestCreditOneTime();
                rqq.setFundingType(getKul.get().getServiceType());
                rqq.setKulFees(kulFees);
                rqq.setKulTransactionId(rq.getProcessId());

                rqq.setNarration(narration);
                rqq.setPhoneNumber(rq.getReceiver());
                rqq.setSwFees(BigDecimal.ZERO);
                rqq.setSwRefrenceNumber("");
                rqq.setTransAmount(amountToCredit);
                CreditWalletCaller rqC = new CreditWalletCaller();
                rqC.setAuth("Receiver");
                rqC.setFees(rqq.getSwFees().toString());
                rqC.setFinalCHarges(rqq.getTransAmount().toString());
                rqC.setNarration(narration);
                rqC.setPhoneNumber(rq.getReceiver());
                rqC.setTransAmount(rqq.getTransAmount().toString());
                rqC.setTransactionId(rq.getProcessId());
                BaseResponse creditAcct = utilMeth.creditCustomer(rqC);

                //  System.out.println("Credit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct));
                //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
                if (creditAcct.getStatusCode() == 200) {

                    // Credit BAAS CAD_GL
                    CreditWalletCaller cadGLCredit = new CreditWalletCaller();
                    cadGLCredit.setAuth("Receiver");
                    cadGLCredit.setFees("0.00");
                    cadGLCredit.setFinalCHarges(amount);
                    cadGLCredit.setNarration("CAD_Deposit");
                    cadGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
                    cadGLCredit.setTransAmount(amount);
                    cadGLCredit.setTransactionId(rq.getProcessId());

                    utilMeth.creditCustomerWithType(cadGLCredit, "CAD_GL");

                    FinWealthPaymentTransaction kTrans2 = new FinWealthPaymentTransaction();
                    kTrans2.setAmmount(amountToDebit);
                    kTrans2.setCreatedDate(Instant.now().plusSeconds(1));
                    kTrans2.setFees(new BigDecimal(getKul.get().getFees()));
                    kTrans2.setPaymentType("Wallet to Wallet Transfer");
                    kTrans2.setReceiver(rq.getReceiver());
                    kTrans2.setSender(getDecoded.phoneNumber);
                    kTrans2.setTransactionId(rq.getProcessId());
                    kTrans2.setSenderTransactionType("Withdrawal");
                    kTrans2.setReceiverTransactionType("Deposit");
                    kTrans2.setWalletNo(getDecoded.phoneNumber);
                    kTrans2.setReceiverName(rq.getReceiverName());
                    kTrans2.setSenderName(senderName);
                    kTrans2.setSentAmount(amountToCredit.toString());
                    kTrans2.setTheNarration(getnarration);
                    //processKuleanPaymentTransactionLedger(kTrans2);

                    FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                    kTrans2b.setAmmount(new BigDecimal(rq.getAmount()));
                    kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                    kTrans2b.setFees(new BigDecimal(getKul.get().getFees()));
                    kTrans2b.setPaymentType("Wallet to Wallet Transfer");
                    kTrans2b.setReceiver(rq.getReceiver());
                    kTrans2b.setSender(getDecoded.phoneNumber);
                    kTrans2b.setTransactionId(rq.getProcessId());
                    kTrans2b.setSenderTransactionType("Withdrawal");
                    kTrans2b.setReceiverTransactionType("Deposit");

                    kTrans2b.setWalletNo(getDecoded.phoneNumber);
                    kTrans2b.setReceiverName(rq.getReceiverName());
                    kTrans2b.setSenderName(senderName);
                    kTrans2b.setSentAmount(amountToCredit.toString());
                    kTrans2b.setTheNarration(getnarration);

                    finWealthPaymentTransactionRepo.save(kTrans2b);

                    //save to wallet to wallet log
                    WToWaletTransfer saveWalletT = new WToWaletTransfer(
                            rq.getProcessId(), true,
                            rq.getSender(), new BigDecimal(rq.getAmount()),
                            finalChrges, rq.getReceiver(),
                            new BigDecimal(rq.getAmount()),
                            kulFees,
                            narration, getKul.get().getServiceType(), getNameLookUpDe.get(0).getLTransSessReceiverName());
                    wToWaletTransferRepo.save(saveWalletT);
                    //update regWalletCheck
                    RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(rq.getSender());
                    logTransUp.setProcessIdStatus("2");
                    logTransUp.setLastModifiedDate(Instant.now());
                    String procWalletTransferCumm = getNameLookUpDe.get(0).getWalletTransferCumm() == null ? "0" : getNameLookUpDe.get(0).getWalletTransferCumm();
                    logTransUp.setWalletTransferCumm(new BigDecimal(procWalletTransferCumm).add(finalChrges).toString());
                    logTransUp.setLTransSessExpiry(expiry);
                    //logTransUp.setLTransSessExpiry(nowMillis);
                    regWalletCheckLogRepo.save(logTransUp);

                    List<LocalTransferRequestLog> getDee = localTransferRequestLogRepo.findByProcesIdProcessStatus(rq.getProcessId(), "1");

                    if (getDee.size() > 0) {

                        LocalTransferRequestLog getDeeDE = localTransferRequestLogRepo.findByProcesIdProcessStatusDe(rq.getProcessId(), "1");
                        getDeeDE.setLastModifiedDate(Instant.now());
                        getDeeDE.setProcessIdStatus("2");
                        getDeeDE.setProcessIdStatusDesc("completed");

                        localTransferRequestLogRepo.save(getDeeDE);

                    }

                    List<LocalBeneficiaries> getSavedBen = localBeneficiariesRepo.findByWalletNoByBeneficiaryActive(rq.getSender(), rq.getReceiver(), "1");
                    if (getSavedBen.size() > 0) {

                        LocalBeneficiaries getSavedBenUp = localBeneficiariesRepo.findByWalletNoByBeneficiaryActiveUpdate(rq.getSender(), rq.getReceiver(), "1");
                        getSavedBenUp.setTransactionCount(getSavedBen.get(0).getTransactionCount() + 1);
                        getSavedBenUp.setLastModifiedDate(Instant.now());
                        getSavedBenUp.setRequestSource("Wallet-To-Wallet-Transfer");
                        localBeneficiariesRepo.save(getSavedBenUp);

                    }
                    LocalBeneficiariesIndividual logBene = new LocalBeneficiariesIndividual();

                    logBene.setBeneficiaryName(rq.getReceiverName());
                    logBene.setBeneficiaryNo(rq.getReceiver());
                    logBene.setBeneficiaryStatus("1");
                    logBene.setCreatedDate(Instant.now());
                    logBene.setWalletNo(getDecoded.phoneNumber);
                    logBene.setRequestSource("Wallet-To-Wallet-Transfer");
                    localBeneficiariesIndividualRepo.save(logBene);

                    List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rq.getReceiver());
                    List<RegWalletInfo> getSenderName = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

                    PushNotificationFireBase puFire = new PushNotificationFireBase();
                    puFire.setBody(pushNotifyDebitWalletForWalletTransfer(new BigDecimal(rq.getAmount()),
                            rq.getReceiverName(), getSenderName.get(0).getFirstName() + " " + getSenderName.get(0).getLastName()
                    ));
                    List<DeviceDetails> getDe = deviceDetailsRepo.findAllByWalletId(getReceiverName.get(0).getWalletId());

                    puFire.setTitle("Wallet-To-Wallet-Transfer");
                    if (getDe.size() > 0) {
                        String getToken = getDe.get(0).getToken() == null ? "" : getDe.get(0).getToken();

                        if (getToken != "") {
                            System.out.println("Receiver has token::::::::::::::::  %S  ");

                            puFire.setDeviceToken(getDe.get(0).getToken());
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("type", "ALERT");            // sample custom data
                            if (puFire.getData() != null) {
                                data.putAll(puFire.getData());
                            }

                            messageCenterService.createAndPushToUser(getReceiverName.get(0).getWalletId(), puFire.getTitle(),
                                    puFire.getBody(),
                                    data, null, "");

                            /*fcmService.sendToToken(
                                    puFire.getDeviceToken(),
                                    puFire.getTitle(),
                                    puFire.getBody(),
                                    data
                            );*/
                        }

                    }
                    PushNotificationFireBase puFireSender = new PushNotificationFireBase();
                    puFireSender.setBody(pushNotifyDebitWalletForWalletTransferSender(new BigDecimal(rq.getAmount()),
                            rq.getReceiverName(), getSenderName.get(0).getFirstName() + " " + getSenderName.get(0).getLastName()
                    ));
                    List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(getSenderName.get(0).getWalletId());

                    puFireSender.setTitle("Wallet-To-Wallet-Transfer");
                    if (getDepuFireSender.size() > 0) {

                        String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                        if (getToken != null && !getToken.trim().isEmpty()) {

                            System.out.println("Sender has token::::::::::::::::  %S  ");

                            puFireSender.setDeviceToken(getDepuFireSender.get(0).getToken());
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("type", "ALERT");            // sample custom data
                            if (puFireSender.getData() != null) {
                                data.putAll(puFireSender.getData());
                            }

                            /*fcmService.sendToToken(
                                    puFireSender.getDeviceToken(),
                                    puFireSender.getTitle(),
                                    puFireSender.getBody(),
                                    data
                            );*/
                            messageCenterService.createAndPushToUser(getSenderName.get(0).getWalletId(), puFireSender.getTitle(),
                                    puFireSender.getBody(),
                                    data, null, "");

                        }
                    }
                    responseModel.setDescription("Wallet to Wallet transfer, transfer performed successfully.");
                    //    List<WToWaletTransfer> getExistRec = wToWaletTransferRepo.findBySenderAndReceiver(getDecoded.phoneNumber, rq.getReceiver());

                    if (getSavedBen.size() > 0) {
                        responseModel.addData("isBeneficiary", true);
                    } else {
                        responseModel.addData("isBeneficiary", false);
                        responseModel.addData("receiverName", rq.getReceiverName());
                    }

                    /* EmailRequest emailRe = new EmailRequest();
                    emailRe.setBody(generateTopUpMsg(getReceiver.get(0).getFirstName() + " " + getReceiver.get(0).getLastName(), rq.getAmount()));
                    emailRe.setSubject("KuleanPay Wallet Topup Notification");
                    emailRe.setTo(getReceiver.get(0).getEmail());
                    BaseResponse sendMail = utilitiesProxy.sendEmails(emailRe);
                    System.out.println("sendMail response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + new Gson().toJson(sendMail));
                    System.out.println("sendMail response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: ");

                    EmailRequest emailSe = new EmailRequest();
                    emailSe.setBody(generateDebitAcctMsg(getSenderName.get(0).getFirstName() + " " + getSenderName.get(0).getLastName(), rq.getAmount()));
                    emailSe.setSubject("KuleanPay Wallet Account Debit Notification");
                    emailSe.setTo(getSenderName.get(0).getEmail());
                    BaseResponse sendMailemailSe = utilitiesProxy.sendEmails(emailSe);
                    System.out.println("sendMail sender response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + new Gson().toJson(sendMailemailSe));
                    System.out.println("sendMail sender response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: ");
                     */
                    responseModel.setStatusCode(200);

                    return responseModel;

                } else {
                    //roll back -> credit sender or log for retrial

                    LocalTLogRetrialDebit logDeb = new LocalTLogRetrialDebit();
                    logDeb.setCreatedDate(Instant.now());
                    logDeb.setDescription(reqq.getDescription());
                    logDeb.setFinalCharges(reqq.getFinalCharges());
                    logDeb.setKuleanFees(reqq.getKuleanFess());
                    logDeb.setNarration(reqq.getNarration());
                    logDeb.setTransactionId(reqq.getTransactionId());
                    logDeb.setWalletNo(reqq.getPhonenumber());
                    logDeb.setProcessedStatus("1");
                    logDeb.setProcessedStatusDesc("Pending");
                    localTLogRetrialDebitRepo.save(logDeb);
                    //log failed wallet-wallet
                    RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(rq.getSender());
                    logTransUp.setProcessIdStatus("2");
                    logTransUp.setLastModifiedDate(Instant.now());
                    regWalletCheckLogRepo.save(logTransUp);

                    List<LocalTransferRequestLog> getDee = localTransferRequestLogRepo.findByProcesIdProcessStatus(rq.getProcessId(), "1");

                    if (getDee.size() > 0) {

                        LocalTransferRequestLog getDeeDE = localTransferRequestLogRepo.findByProcesIdProcessStatusDe(rq.getProcessId(), "1");
                        getDeeDE.setLastModifiedDate(Instant.now());
                        getDeeDE.setProcessIdStatus("3");
                        getDeeDE.setProcessIdStatusDesc("debit was successful, but credit failed!");

                        localTransferRequestLogRepo.save(getDeeDE);

                    }

                    responseModel.setDescription("Wallet to Wallet transfer, transfer failed!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            } else {
                RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(rq.getSender());
                logTransUp.setProcessIdStatus("2");
                logTransUp.setLastModifiedDate(Instant.now());
                regWalletCheckLogRepo.save(logTransUp);

                List<LocalTransferRequestLog> getDee = localTransferRequestLogRepo.findByProcesIdProcessStatus(rq.getProcessId(), "1");

                if (getDee.size() > 0) {

                    LocalTransferRequestLog getDeeDE = localTransferRequestLogRepo.findByProcesIdProcessStatusDe(rq.getProcessId(), "1");
                    getDeeDE.setLastModifiedDate(Instant.now());
                    getDeeDE.setProcessIdStatus("3");
                    getDeeDE.setProcessIdStatusDesc("transaction failed!");

                    localTransferRequestLogRepo.save(getDeeDE);

                }

                //log failed wallet-wallet
                responseModel.setDescription("Wallet to Wallet transfer, transfer failed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public static String pushNotifyDebitWalletForWalletTransfer(BigDecimal amount, String recName, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been credited with " + "N" + amount + " "
                + "of a transfer from " + senderName + " to you," + ""
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    public static String pushNotifyDebitWalletForWalletTransferSender(BigDecimal amount, String recName, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been debited with " + "N" + amount + " "
                + "of a transfer to " + recName + "," + ""
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    public String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);

        // log.info("decryptData ::::: {} ", decryptData);
        return decryptData;

    }

    public ApiResponseModel getUserTransactionsHistory(WalletNoReq rq, String channel, String auth) {

        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String processId = String.valueOf(GlobalMethods.generateTransactionId());
            boolean isWalletId = true;
            boolean isPhonenUmber = true;
            String userPhoneNumber = null;

            List<RegWalletInfo> getReceiver = null;
            if (GlobalMethods.isTenDigits(rq.getMemberId().trim()) == false) {
                isWalletId = false;
                isPhonenUmber = true;

            }


            /* if (!GlobalMethods.isElevenDigits(rq.getMemberId().trim()) == true) {

                isPhonenUmber = false;

            }*/
            System.out.println("isWalletIdBool :::::::: " + "     " + isWalletId);

            System.out.println("isPhonenUmberBool :::::::: " + "     " + isPhonenUmber);

            if (isWalletId == false && isPhonenUmber == false) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid Receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid Receiver");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            if (isWalletId) {

                System.out.println("isWalletId :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByWalletIdList(rq.getMemberId());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "User does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("User does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                userPhoneNumber = getReceiver.get(0).getPhoneNumber();
            }

            if (isPhonenUmber) {

                System.out.println("isPhonenUmber :::::::: " + "     ");

                getReceiver = regWalletInfoRepository.findByPhoneNumberData(rq.getMemberId());

                if (getReceiver.size() <= 0) {

                    LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                            "Wallet-Wallet-Transfer", "User does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("User does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                userPhoneNumber = getReceiver.get(0).getPhoneNumber();

            }

            List<FinWealthPaymentTransaction> getKulTrans = finWealthPaymentTransactionRepo.findByWalletNoList(userPhoneNumber);
            if (getKulTrans.size() <= 0) {

                LocalTransFailedTransInfo procFailedTrans = new LocalTransFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Customer does not have existing transaction!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, Customer does not have existing transaction!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            List<Object> mapAll = new ArrayList<Object>();
            for (FinWealthPaymentTransaction getKul : getKulTrans) {

                FinWalletPaymentTransModel getK = new FinWalletPaymentTransModel();
                getK.setAmmount(getKul.getAmmount());
                getK.setTransactionDate(formDate(getKul.getCreatedDate()));
                getK.setFees(getKul.getFees());
                getK.setPaymentType(getKul.getPaymentType());
                getK.setReceiver(getKul.getReceiver());
                getK.setSender(getKul.getSender());
                getK.setTransactionId(getKul.getTransactionId());
                getK.setReceiverTransactionType(getKul.getReceiverTransactionType());
                getK.setSenderTransactionType(getKul.getSenderTransactionType());
                getK.setCurrencyCode(getKul.getCurrencyCode());
                mapAll.add(getK);

            }

            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    private String formDate(Instant datte) {

        LocalDateTime datetime = LocalDateTime.ofInstant(datte, ZoneOffset.UTC);
        String formatted = DateTimeFormatter.ofPattern("MMM dd, yyyy").format(datetime);
        //System.out.println(formatted);

        return formatted;
    }

}
