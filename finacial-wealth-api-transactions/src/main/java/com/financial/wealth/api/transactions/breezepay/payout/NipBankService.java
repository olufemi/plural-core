/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
// package: com.finacial.wealth.api.nip.service
import com.financial.wealth.api.transactions.config.ApiClientException;
import com.financial.wealth.api.transactions.domain.CommissionCfg;
import com.financial.wealth.api.transactions.domain.DeviceChangeLimitConfig;
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.FinWealthPayServiceConfig;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.GlobalLimitConfig;
import com.financial.wealth.api.transactions.domain.LocalTransFailedTransInfo;

import com.financial.wealth.api.transactions.domain.LocalTransferRequestLog;
import com.financial.wealth.api.transactions.domain.OtherBankBeneficiaries;
import com.financial.wealth.api.transactions.domain.OtherBankBeneficiariesInd;
import com.financial.wealth.api.transactions.domain.RegWalletCheckLog;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SuccessDebitLog;
import com.financial.wealth.api.transactions.domain.UserLimitConfig;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.models.OtherBankTransferRequest;
import com.financial.wealth.api.transactions.models.PushNotificationFireBase;
import com.financial.wealth.api.transactions.models.WalletNoReq;
import com.financial.wealth.api.transactions.models.local.trans.NameLookUp;
import com.financial.wealth.api.transactions.proxies.BreezePayVirtAcctProxy;
import com.financial.wealth.api.transactions.repo.AddAccountDetailsRepo;
import com.financial.wealth.api.transactions.repo.CommissionCfgRepo;
import com.financial.wealth.api.transactions.repo.DeviceChangeLimitConfigRepo;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPayServiceConfigRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.GlobalLimitConfigRepo;

import com.financial.wealth.api.transactions.repo.LocalTransferRequestLogRepo;
import com.financial.wealth.api.transactions.repo.OtherBankBeneficiariesIndRepo;
import com.financial.wealth.api.transactions.repo.OtherBankBeneficiariesRepo;
import com.financial.wealth.api.transactions.repo.RegWalletCheckLogRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SuccessDebitLogRepo;
import com.financial.wealth.api.transactions.repo.UserLimitConfigRepo;
import com.financial.wealth.api.transactions.services.LocalTransferService;
import static com.financial.wealth.api.transactions.services.LocalTransferService.betweenTransBand;
import static com.financial.wealth.api.transactions.services.LocalTransferService.pushNotifyDebitWalletForWalletTransferSender;
import com.financial.wealth.api.transactions.services.notify.MessageCenterService;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class NipBankService {

    private final RestTemplate restTemplate;
    private final NipBankRepository repository;
    private final PaymentsFailedTransInfoRepo localTransFailedTransInfoRepo;

    private final RegWalletInfoRepository regWalletInfoRepository;
    private final WToBankTransferRepo wToBankTransferRepo;
    private final UserLimitConfigRepo userLimitConfigRepo;
    private final DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo;
    private final GlobalLimitConfigRepo globalLimitConfigRepo;
    private final FinWealthPayServiceConfigRepo kuleanPayServiceConfigRepo;
    private final CommissionCfgRepo commissionCfgRepo;
    private final RegWalletCheckLogRepo regWalletCheckLogRepo;
    private final LocalTransferRequestLogRepo localTransferRequestLogRepo;
    private final UttilityMethods utilMeth;
    private static final int DEFAULT_DEVICE_LIMIT_DAYS = 2;
    private final LocalTransferService localTransferService;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final BreezePayVirtAcctProxy breezePayVirtAcctProxy;
    private final RequestForPayOutLogRepo requestForPayOutLogRepo;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final OtherBankBeneficiariesRepo otherBankBeneficiariesRepo;
    private final OtherBankBeneficiariesIndRepo otherBankBeneficiariesIndRepo;
    private final MessageCenterService messageCenterService;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final SuccessDebitLogRepo successDebitLogRepo;
    private final NipCredAcccTranLogRepo nipCredAcccTranLogRepo;

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();
    private static final int LENGTH = 21;
    private static final String CCY = "NGN";
    private static final int STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE = 58;
    private static final String STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION = "Please validate bvn";
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\n\r\t]]");
    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B-\\u200D\\uFEFF]");
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Autowired
    private org.springframework.core.env.Environment env;

    @Value("${fin.wealth.breeze.pay.mer.code.id}")
    private String channelCode;

    @Value("${fin.wealth.breeze.pay.mer.id}")
    private String merchantId;

    @Value("${fin.wealth.breeze.wallet.pay.base.url}")
    private String banksWalletUrl;

    @Value("${fin.wealth.breeze.pay.base.url}")
    private String banksUrl;

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${fin.wealth.goto.breeze}")
    private String gotoBreezeapay;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    // Optional headers, if your sandbox enforces them:
    @Value("${fin.wealth.breeze.pay.mer.sub.key}")
    private String subscriptionKey;  // Ocp-Apim-Subscription-Key
    @Value("${fin.wealth.breeze.pay.mer.auth}")
    private String authorization;      // Bearer <token>

    @Value("${app.nipbanks.seed.fs.path}")
    private String nipBanksFilePath;

    long nowMillis = System.currentTimeMillis();

    public NipBankService(RestTemplate restTemplate, NipBankRepository repository,
            PaymentsFailedTransInfoRepo localTransFailedTransInfoRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            UserLimitConfigRepo userLimitConfigRepo,
            DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo,
            GlobalLimitConfigRepo globalLimitConfigRepo,
            FinWealthPayServiceConfigRepo kuleanPayServiceConfigRepo,
            CommissionCfgRepo commissionCfgRepo,
            RegWalletCheckLogRepo regWalletCheckLogRepo,
            LocalTransferRequestLogRepo localTransferRequestLogRepo,
            UttilityMethods utilMeth, LocalTransferService localTransferService,
            AddAccountDetailsRepo addAccountDetailsRepo, BreezePayVirtAcctProxy breezePayVirtAcctProxy,
            RequestForPayOutLogRepo requestForPayOutLogRepo, WToBankTransferRepo wToBankTransferRepo,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            OtherBankBeneficiariesRepo otherBankBeneficiariesRepo,
            OtherBankBeneficiariesIndRepo otherBankBeneficiariesIndRepo, MessageCenterService messageCenterService,
            DeviceDetailsRepo deviceDetailsRepo, SuccessDebitLogRepo successDebitLogRepo,
            NipCredAcccTranLogRepo nipCredAcccTranLogRepo) {
        this.otherBankBeneficiariesIndRepo = otherBankBeneficiariesIndRepo;
        this.otherBankBeneficiariesRepo = otherBankBeneficiariesRepo;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.restTemplate = restTemplate;
        this.repository = repository;
        this.localTransFailedTransInfoRepo = localTransFailedTransInfoRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.userLimitConfigRepo = userLimitConfigRepo;
        this.deviceChangeLimitConfigRepo = deviceChangeLimitConfigRepo;
        this.globalLimitConfigRepo = globalLimitConfigRepo;
        this.kuleanPayServiceConfigRepo = kuleanPayServiceConfigRepo;
        this.commissionCfgRepo = commissionCfgRepo;
        this.regWalletCheckLogRepo = regWalletCheckLogRepo;
        this.localTransferRequestLogRepo = localTransferRequestLogRepo;
        this.localTransferService = localTransferService;
        this.utilMeth = utilMeth;
        this.breezePayVirtAcctProxy = breezePayVirtAcctProxy;
        this.requestForPayOutLogRepo = requestForPayOutLogRepo;
        this.wToBankTransferRepo = wToBankTransferRepo;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.messageCenterService = messageCenterService;
        this.deviceDetailsRepo = deviceDetailsRepo;
        this.successDebitLogRepo = successDebitLogRepo;
        this.nipCredAcccTranLogRepo = nipCredAcccTranLogRepo;
    }

    static String stripBom(byte[] raw) {
        if (raw.length >= 3 && raw[0] == UTF8_BOM[0] && raw[1] == UTF8_BOM[1] && raw[2] == UTF8_BOM[2]) {
            return new String(raw, 3, raw.length - 3, StandardCharsets.UTF_8);
        }
        return new String(raw, StandardCharsets.UTF_8);
    }

    static String cleanText(String in) {
        if (in == null) {
            return null;
        }
        String s = ZERO_WIDTH.matcher(in).replaceAll("");
        s = CONTROL.matcher(s).replaceAll("");
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        return s.trim();
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

    public List<CommissionCfg> findAllByTransactionType(String transType) {

        return commissionCfgRepo.findAllByTransactionType(transType);
    }

    public BaseResponse nameLookUp(NameLookUpInterBank rq, String channel, String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            System.out.println("NameLookUp req :::::::: " + "    ::::::::::::::::::::: " + new Gson().toJson(rq));
            System.out.println(" authKey :::::::::::::::: %S " + authorization);
            System.out.println(" subKey :::::::::::::::: %S " + subscriptionKey);

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String processId = String.valueOf(GlobalMethods.generateTransactionId());

            boolean isWalletIdSender = true;
            boolean isPhonenUmberSender = false;
            String senderPhoneNumber = null;
            List<RegWalletInfo> getSender = null;

            if (GlobalMethods.isTenDigits(rq.getSender().trim()) == false) {
                isWalletIdSender = false;
                isPhonenUmberSender = true;

            }

            List<AddAccountDetails> getVirtDe = addAccountDetailsRepo.findByEmailAddress(getDecoded.emailAddress);
            if (getVirtDe.size() > 0) {

                String senderVirtAcct = getVirtDe.get(0).getAccountNumber() == null ? "0" : getVirtDe.get(0).getAccountNumber();

                if (getVirtDe.get(0).getAccountNumber().equals("0")) {
                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer," + STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION,
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );
                    responseModel.setDescription(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION);
                    responseModel.setStatusCode(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE);
                    return responseModel;

                }

                if (getVirtDe.get(0).getAccountNumber().equals(rq.getBankAccount().trim())) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer, The Receiver's Account is same as Sender Virtual Account.",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );

                    responseModel.setDescription("Wallet to Bank transfer, This transaction cannot be processed!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

            }

            List<AddAccountDetails> getVirtDeOther = addAccountDetailsRepo.findByAccountNumberList(rq.getBankAccount());
            if (getVirtDeOther.size() > 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, The Receiver's Account exists as Virtual Account.",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, This transaction cannot be processed!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            String senderVirtAccount = null;
            boolean isCountryReady = true;
            for (AddAccountDetails getAcctOb : getVirtDe) {
                if (getAcctOb.getCurrencyCode().equals(CCY)) {
                    senderVirtAccount = getAcctOb.getAccountNumber();
                } else {
                    isCountryReady = false;
                }
            }

            if (!isCountryReady) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Country not yet available!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, Country not yet available!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            if (rq.getBankCode().isEmpty() || rq.getBankCode() == null) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Bank is empty",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, Bank is empty!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            System.out.println("iisWalletIdSenderBool :::::::: " + "     " + isWalletIdSender);

            System.out.println("isPhonenUmberSenderBool :::::::: " + "     " + isPhonenUmberSender);

            if (isWalletIdSender == false && isPhonenUmberSender == false) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Wallet transfer, invalid Sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Wallet-Bank-Transfer"
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

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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

            if (!getDecoded.phoneNumber.equals(senderPhoneNumber)) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid sender!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            if (rq.getReceiver().equals(getDecoded.phoneNumber)) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, invalid transaction, Customer cannot transfer to self!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );

                responseModel.setDescription("Wallet to Wallet transfer, invalid transaction, Customer cannot transfer to self!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            System.out.println("Phone number :::::::: " + "     " + getDecoded.phoneNumber);

            getSender = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            //System.out.println("getSender :::::::: " + "    ::::::::::::::::::::: " + new Gson().toJson(getSender));

            boolean isBeforeYesterday = false;

            //  System.out.println("Sender Request walletId :::::::: " + "     " + getSender.get(0).getWalletId());
            /* List<UserLimitConfig> userLimit1 = userLimitConfigRepo.findByWalletNumber(getSender.get(0).getWalletId());
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

            List<GlobalLimitConfig> getG1 = globalLimitConfigRepo.findByLimitCategory(getActiveCat1);*/
            List<AddAccountDetails> getNigDe = addAccountDetailsRepo.findByEmailAddress(getDecoded.emailAddress);

            BaseResponse getTotalBal = localTransferService.getTotalBalByPhoneNumb(getNigDe.get(0).getAccountNumber());

            if (getTotalBal.getStatusCode() != 200) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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
            BigDecimal transAmount = new BigDecimal(rq.getAmount());

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
            BigDecimal accountBal = amount;

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 1) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 1) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry insufficient fund, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry insufficient fund, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            //a.compareTo(b) 
            /* if (isBeforeYesterday == true) {
                if (accountBal1.compareTo(new BigDecimal(getG1.get(0).getMaximumBalance())) > 0) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Wallet-Transfer", "Wallet to wALLET transfer - Sorry, your account balance is greater than your Tier's, kindly upgrade to higher Tier. Your maximum account balance is: " + getG1.get(0).getMaximumBalance(),
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    localTransFailedTransInfoRepo.save(procFailedTrans);

                    responseModel.setStatusCode(400);
                    responseModel.setDescription("Wallet to Wallet transfer - Sorry, your account balance is greater than your Tier's, kindly upgrade to higher Tier. Your maximum account balance is: " + getG1.get(0).getMaximumBalance());
                    return responseModel;

                }
            }*/
            rq.setSender(getNigDe.get(0).getAccountNumber());
            // System.out.println("getDecoded.phoneNumber :::::::: " + "     " + getDecoded.phoneNumber);
            System.out.println("rq.getSender() :::::::: " + "     " + rq.getSender());

            //  List<FinWealthPayServiceConfig> getKulList = kuleanPayServiceConfigRepo.findByServiceTypeEnable("payouttransfer");
            List<FinWealthPayServiceConfig> getKulList = kuleanPayServiceConfigRepo.findByServiceTypeEnable("payouttransfer");
            if (getKulList.size() <= 0) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Wallet to Wallet transfer, Service Type does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);

                responseModel.setStatusCode(400);
                responseModel.setDescription("Service Type does not exist!");
                return responseModel;
            }

            Optional<FinWealthPayServiceConfig> getKul = kuleanPayServiceConfigRepo.findAllByServiceType("payouttransfer");

            List<CommissionCfg> pullData = findAllByTransactionType("payouttransfer");
            if (pullData.size() > 0) {
                if (pullData.isEmpty()) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Wallet-Transfer", "Amount is not within the transaction band, please check!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Local-Transfer-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);

                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Amount is not within the transaction band, please check!");
                return responseModel;
            }

            String account_name = null;

            BigDecimal pFees = BigDecimal.ZERO;

            for (CommissionCfg partData : pullData) {
                if (getKul.get().getServiceType().trim().equals(partData.getTransType())) {
                    System.out.println("pullData.get(0).getTransType()" + "  :::::::::::::::::::::   " + partData.getTransType());

                    System.out.println("rq.getAmount()" + "  :::::::::::::::::::::   " + rq.getAmount());
                    System.out.println("pullData.get(0).getFee()" + "  :::::::::::::::::::::   " + partData.getFee());
                    System.out.println("pullData.get(0).getAmountMin()" + "  :::::::::::::::::::::   " + partData.getAmountMin());
                    System.out.println("pullData.get(0).getAmountMax()" + "  :::::::::::::::::::::   " + partData.getAmountMax());

                    if (betweenTransBand(new BigDecimal(rq.getAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {

                        //compute the fees
                        //1.8% + 100 (convenience fee)
                        pFees = partData.getFee();
                        System.out.println("pFees" + "  :::::::::::::::::::::   " + pFees);

                        // if (environment.equals("pilot") || environment.equals("prod")) {
                        if (gotoBreezeapay.equals("1")) {

                            NameEnquiryReq reqq = new NameEnquiryReq();

                            //ProvidusValAcctApi reqq = new ProvidusValAcctApi();
                            reqq.setChannelCode(channelCode);
                            reqq.setCreditAccount(rq.getBankAccount());
                            reqq.setCreditBankCode(rq.getBankCode());
                            reqq.setMsgId("BRZ" + generateMsgId());
                            reqq.setSenderAccount(senderPhoneNumber);

                            System.out.println("NameEnquiryReq reqq " + "  :::::::::::::::::::::   " + new Gson().toJson(reqq));

                            NameEnquiryResponse validateAcctDe = breezePayVirtAcctProxy.nameEnquiry(reqq, authorization, subscriptionKey);

                            System.out.println("NameEnquiryResponse  " + "  :::::::::::::::::::::   " + new Gson().toJson(validateAcctDe));

                            if (!"00".equals(validateAcctDe.getResponseCode())) {
                                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                                        "Initiate-Add-Bank-Account", validateAcctDe.getResponseMessage(),
                                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                        "Payment-Service"
                                );

                                responseModel.setDescription(validateAcctDe.getResponseMessage());
                                responseModel.setStatusCode(statusCode);

                                localTransFailedTransInfoRepo.save(procFailedTrans);
                                return responseModel;
                            }
                            // String dataStr = new Gson().toJson(validateAcctDe.getData());
                            //JsonObject trRp = new Gson().fromJson(dataStr, JsonObject.class);
                            account_name = validateAcctDe.getResponseData().getAccountName();

                        } else {
                            account_name = "John Joe Kecshi.";
                        }

                        // if (!environment.equals("prod")) {
                        //    }
                        if (!getKul.get().isEnabled()) {

                            PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                                    "Wallet-Bank-Transfer", "Wallet to Bank transfer, service type is disabled!",
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Payment-Service"
                            );

                            localTransFailedTransInfoRepo.save(procFailedTrans);

                            responseModel.setDescription("Wallet to Bank transfer, service type is disabled!");
                            responseModel.setStatusCode(statusCode);

                            return responseModel;

                        }

                        String flagMinAmt = "amount cannot be less than N" + getKul.get().getMinimumAmmount() + ".00, please check!";
                        if (new BigDecimal(rq.getAmount()).compareTo(new BigDecimal(getKul.get().getMinimumAmmount())) == -1) {

                            PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                                    "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + flagMinAmt,
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                                    "Payment-Service"
                            );

                            localTransFailedTransInfoRepo.save(procFailedTrans);

                            responseModel.setDescription("Wallet to Bank transfer, " + flagMinAmt);
                            responseModel.setStatusCode(statusCode);

                            return responseModel;
                        }

                        RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(getDecoded.phoneNumber);
                        logTransUp.setLTransServiceType(getKul.get().getServiceType());
                        logTransUp.setLTransSessAmount(new BigDecimal(rq.getAmount()));
                        logTransUp.setLTransSessFees(pFees);
                        logTransUp.setLTransSessReceiverName(account_name);
                        logTransUp.setLTransSessReceiverAccountNo(rq.getBankAccount());
                        logTransUp.setLTransSessSenderWalletNo(getNigDe.get(0).getAccountNumber());
                        logTransUp.setProcessId(processId);
                        logTransUp.setProcessIdStatus("1");
                        logTransUp.setLastModifiedDate(Instant.now());
                        logTransUp.setBankCode(rq.getBankCode());
                        logTransUp.setBankName(rq.getBankName());
                        logTransUp.setTheNarration(rq.getTheNarration());
                        logTransUp.setSenderVirtualAccount(getNigDe.get(0).getVirtualAccountNumber());
                        regWalletCheckLogRepo.save(logTransUp);

                        //requestForPayOutLogRepo
                        RequestForPayOutLog logTransUplOG = new RequestForPayOutLog();
                        logTransUplOG.setLTransServiceType(getKul.get().getServiceType());
                        logTransUplOG.setLTransSessAmount(rq.getAmount());
                        logTransUplOG.setLTransSessFees(pFees.toString());
                        logTransUplOG.setLTransSessReceiverName(account_name);
                        logTransUplOG.setLTransSessReceiverAccountNo(rq.getBankAccount());
                        logTransUplOG.setLTransSessSenderWalletNo(getNigDe.get(0).getAccountNumber());
                        logTransUplOG.setProcessId(processId);
                        logTransUplOG.setProcessIdStatus("1");
                        logTransUplOG.setProcessIdStatusDesc("in-progress");
                        logTransUplOG.setCreatedDate(Instant.now());
                        logTransUplOG.setBankCode(rq.getBankCode());
                        logTransUplOG.setBankName(rq.getBankName());
                        logTransUplOG.setTheNarration(rq.getTheNarration());
                        logTransUplOG.setWalletNo(getDecoded.phoneNumber);
                        logTransUplOG.setRequestChannel(channel);
                        logTransUplOG.setSenderVirtualAccount(getNigDe.get(0).getVirtualAccountNumber());
                        requestForPayOutLogRepo.save(logTransUplOG);

                        responseModel.addData("processId", processId);
                        responseModel.addData("sender", getNigDe.get(0).getAccountNumber());
                        responseModel.addData("accountName", account_name);
                        responseModel.addData("accountNumber", rq.getBankAccount());
                        responseModel.addData("transactionType", getKul.get().getServiceType());
                        responseModel.addData("bankCode", rq.getBankCode());
                        responseModel.addData("bankName", rq.getBankName());
                        responseModel.addData("fees", pFees.toString());
                        String amountToDebit = new BigDecimal(rq.getAmount()).add(pFees).toString();
                        responseModel.addData("amount", rq.getAmount());
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

    public BaseResponse processTransfer(OtherBankTransferRequest rq, String channel, String auth) {

        System.out.println(" authKey :::::::::::::::: %S " + authorization);
        System.out.println(" subKey :::::::::::::::: %S " + subscriptionKey);

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            boolean isWalletIdSender = true;
            boolean isPhonenUmberSender = false;
            String senderPhoneNumber = null;
            List<RegWalletInfo> getSenderEraly = null;

            if (!regWalletCheckLogRepo.existsByProcessId(rq.getProcessId())) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Transaction has not been initiated!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, Transaction has not been initiated!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }

            List<RegWalletCheckLog> getNameLookUpDe = regWalletCheckLogRepo.findByProcessIdList(rq.getProcessId());

            if (getNameLookUpDe.size() > 0) {
                if (getNameLookUpDe.get(0).getProcessIdStatus().equals("2")) {
                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + "Suspected fraud, this transaction no longer exists",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );

                    responseModel.setDescription("Wallet to Bank transfer, " + "Suspected fraud,this transaction no longer exists");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
            }

            /*if (!getDecoded.phoneNumber.equals(getNameLookUpDe.get(0).getLTransSessSenderWalletNo())) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + "Suspected fraud!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, " + "Suspected fraud!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }*/
            if (!rq.getBankCode().equals(getNameLookUpDe.get(0).getBankCode())) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + "Invalid Receiver's bank!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, " + "Invalid Receiver's bank!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!rq.getBankName().equals(getNameLookUpDe.get(0).getBankName())) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + "Invalid Receiver's bank name!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, " + "Invalid Receiver's bank name!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            Optional<FinWealthPayServiceConfig> getKul = kuleanPayServiceConfigRepo.findAllByServiceType(getNameLookUpDe.get(0).getLTransServiceType());
            BigDecimal kulFees = getNameLookUpDe.get(0).getLTransSessFees();
            //check minimum amount
            String flagMinAmt = "amount cannot be less than N" + getKul.get().getMinimumAmmount() + ".00, please check!";
            if (new BigDecimal(rq.getAmount()).compareTo(new BigDecimal(getKul.get().getMinimumAmmount())) == -1) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, " + flagMinAmt,
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, " + flagMinAmt);
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            if (GlobalMethods.isTenDigits(rq.getSender().trim()) == false) {
                isWalletIdSender = false;
                isPhonenUmberSender = true;

            }

            /* System.out.println("iisWalletIdSenderBool :::::::: " + "     " + isWalletIdSender);

            System.out.println("isPhonenUmberSenderBool :::::::: " + "     " + isPhonenUmberSender);

            if (isWalletIdSender == false && isPhonenUmberSender == false) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Wallet transfer, invalid Sender!",
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

                getSenderEraly = regWalletInfoRepository.findByWalletIdList(rq.getSender());

                if (getSenderEraly.size() <= 0) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
                senderPhoneNumber = getSenderEraly.get(0).getPhoneNumber();
            }*/

 /* if (isPhonenUmberSender) {

                System.out.println("isPhonenUmberSender :::::::: " + "     ");

                getSenderEraly = regWalletInfoRepository.findByPhoneNumberData(rq.getSender());

                if (getSenderEraly.size() <= 0) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Wallet transfer, Sender does not exists!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Local-Transfer-Service"
                    );

                    responseModel.setDescription("Wallet to Wallet transfer, Sender does not exists!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                senderPhoneNumber = getSenderEraly.get(0).getPhoneNumber();

            }

            rq.setSender(senderPhoneNumber);*/
            //check if sender is the same
            if (!getNameLookUpDe.get(0).getLTransSessSenderWalletNo().equals(rq.getSender())) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, sender!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, sender!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //check if receiver is the same
            if (!getNameLookUpDe.get(0).getLTransSessReceiverAccountNo().equals(rq.getReceiverBankAccount())) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, receiver!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, receiver!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            System.out.println("getNameLookUpDe.get(0).getLTransSessAmount() req " + "  ::::::::::::::::::::: " + getNameLookUpDe.get(0).getLTransSessAmount());
            System.out.println("rq.getAmount() " + "  ::::::::::::::::::::: " + rq.getAmount());
            String getNameLookUpDeAmount = getNameLookUpDe.get(0).getLTransSessAmount().toString();
            String amount = rq.getAmount().trim();
            System.out.println("getNameLookUpDeAmount " + "  ::::::::::::::::::::: " + getNameLookUpDeAmount);
            System.out.println("amount " + "  ::::::::::::::::::::: " + amount);

            // if (!getNameLookUpDe.get(0).getLTransSessAmount().toString().equals(rq.getAmount().trim())) {
            if (getNameLookUpDe.get(0).getLTransSessAmount().compareTo(new BigDecimal(rq.getAmount().trim())) != 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, amount!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, amount!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //check if fees is the same
            if (getNameLookUpDe.get(0).getLTransSessFees().compareTo(new BigDecimal(rq.getFees().trim())) != 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, fees!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, fees!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //check if receiver name is the same
            if (!getNameLookUpDe.get(0).getLTransSessReceiverName().equals(rq.getReceiverAccountName())) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, receiver name!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, receiver name!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!getNameLookUpDe.get(0).getLTransServiceType().equals(rq.getTransactionType())) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid transaction, transaction type!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, transaction type!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            System.out.println("Current time nowMillis  ::::::::::::::::               ::::: %S  " + nowMillis);
            if (getNameLookUpDe.get(0).getProcessIdStatus().equals("2")) {
                if (getNameLookUpDe.get(0).getLTransSessExpiry() < nowMillis) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer, duplicate transaction, please try again in one minute!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );

                    responseModel.setDescription("Wallet to Bank transfer, duplicate transaction, please try again in one minute!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }
            }

            //amount to send
            BigDecimal finalChrges = new BigDecimal(rq.getAmount()).add(kulFees);
            BigDecimal amountTSendToCus = new BigDecimal(rq.getAmount());
            rq.setAmount(finalChrges.toString());

            /* BaseResponse secondCheck = validateTransferOthers(rq, channel, auth);

            System.out.println("secondCheck " + "  ::::::::::::::::::::: " + secondCheck);

            if (secondCheck.getStatusCode() != 200) {

                responseModel.setDescription(secondCheck.getDescription());
                responseModel.setStatusCode(secondCheck.getStatusCode());
                return responseModel;

            }*/
            rq.setAmount(amountTSendToCus.toString());
            List<RegWalletInfo> getSender = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            String senderName;
            senderName = getSender.get(0).getFirstName() + " " + getSender.get(0).getLastName();
            System.out.println("senderName " + "  ::::::::::::::::::::: " + senderName);

            String getnarration;

            if (getNameLookUpDe.get(0).getTheNarration() == null || getNameLookUpDe.get(0).getTheNarration().isEmpty()) {
                getnarration = "Wallet to Bank Transfer";
            } else {

                getnarration = getNameLookUpDe.get(0).getTheNarration();

            }

            String narration = "TRF/" + getnarration + "/FRM " + senderName + " TO "
                    + getNameLookUpDe.get(0).getLTransSessReceiverName();

            System.out.println("narration " + "  ::::::::::::::::::::: " + narration);
            System.out.println("verify transfer in long expiry" + "   ::::::::::::::::::::: " + utilMeth.ltExistingRunningWindow());

            LocalDateTime expireMinutes = LocalDateTime.now().plusMinutes(Long.valueOf(utilMeth.ltExistingRunningWindow()));
            long expiry = Timestamp.valueOf(expireMinutes).getTime();

            System.out.println("verify locat transfer in long expiry" + "   ::::::::::::::::::::: " + expiry);

            if (wToBankTransferRepo.existsByTransactionId(rq.getProcessId())) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, transaction has already completed, please check processId!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, transaction has already completed!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //LOCAL, DEV or STAGING ENVIRONMENT 
            System.out.println("Environment" + "   ::::::::::::::::::::: " + "Local, Dev or Staging");
            //I call ledger api to debit sender 

            DebitWalletCaller rqD = new DebitWalletCaller();
            rqD.setAuth(auth);
            rqD.setFees(kulFees.toString());
            rqD.setFinalCHarges(finalChrges.toString());
            rqD.setNarration("Debit account");
            rqD.setPhoneNumber(rq.getSender());
            rqD.setTransAmount(rq.getAmount());
            rqD.setTransactionId(rq.getProcessId());
            BaseResponse debitAcct = utilMeth.debitCustomerWithType(rqD, "CUSTOMER", CCY);

            System.out.println("debitAcct" + "   ::::::::::::::::::::: " + debitAcct);

            if (debitAcct.getStatusCode() == 200) {

                DebitWalletCaller debGLCredit = new DebitWalletCaller();
                debGLCredit.setAuth("Sender");
                debGLCredit.setFees("0.00");
                debGLCredit.setFinalCHarges(amount);
                debGLCredit.setNarration("NGN_Withdrawal");
                debGLCredit.setPhoneNumber(localTransferService.decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG()));
                debGLCredit.setTransAmount(amount);
                debGLCredit.setTransactionId(rq.getProcessId());

                utilMeth.debitCustomerWithType(debGLCredit, "NGN_GL", CCY);

                //System.out.println("processKuleanPaymentTransactionLedger" + "   ::::::::::::::::::::: " + bProcLed.getDescription());
                FinWealthPaymentTransaction kTrans2 = new FinWealthPaymentTransaction();
                kTrans2.setAmmount(finalChrges);
                kTrans2.setSentAmount(amountTSendToCus.toString());
                kTrans2.setCreatedDate(Instant.now());
                kTrans2.setFees(kulFees);
                kTrans2.setPaymentType("Transfer to Bank Account");
                kTrans2.setReceiver(rq.getReceiverBankAccount());
                kTrans2.setReceiverName(rq.getReceiverAccountName());
                kTrans2.setSenderName(senderName);
                kTrans2.setReceiverBankCode(rq.getBankCode());
                kTrans2.setReceiverBankName(rq.getBankName());
                kTrans2.setCurrencyCode(CCY);

                kTrans2.setSender(getDecoded.phoneNumber);
                kTrans2.setTransactionId(rq.getProcessId());
                kTrans2.setTransactionType("Withdrawal");
                kTrans2.setWalletNo(getDecoded.phoneNumber);
                finWealthPaymentTransactionRepo.save(kTrans2);

                NipCreditAccountTransferRequest nipReq = new NipCreditAccountTransferRequest();
                nipReq.setChannelCode(channelCode);
                nipReq.setCreditAccount(rq.getReceiverBankAccount());
                nipReq.setCreditBankCode(rq.getBankCode());
                nipReq.setDebitCustomerId(kTrans2.getSender());
                nipReq.setDebitMerchantId(merchantId);
                nipReq.setDebitVirtualAccount(getNameLookUpDe.get(0).getSenderVirtualAccount());
                nipReq.setTransactionAmount(amountTSendToCus);
                nipReq.setTransactionNarration(getnarration);
                nipReq.setTransactionReference(rq.getProcessId());

                NipCredAcccTranLog nipReqLog = new NipCredAcccTranLog();
                nipReqLog.setChannelCode(channelCode);
                nipReqLog.setCreditAccount(rq.getReceiverBankAccount());
                nipReqLog.setCreditBankCode(rq.getBankCode());
                nipReqLog.setDebitCustomerId(kTrans2.getSender());
                nipReqLog.setDebitMerchantId(merchantId);
                nipReqLog.setDebitVirtualAccount(getNameLookUpDe.get(0).getSenderVirtualAccount());
                nipReqLog.setTransactionAmount(amountTSendToCus);
                nipReqLog.setTransactionNarration(getnarration);
                nipReqLog.setTransactionReference(rq.getProcessId());

                // if (environment.equals("prod") || environment.equals("pilot")) {
                if (gotoBreezeapay.equals("1")) {
                    NipCreditTransferResponse nipCre = new NipCreditTransferResponse();
                    System.out.println(" NipCreditAccountTransferRequest :::::::::::::::: %S " + new Gson().toJson(nipReq));

                    nipCre = breezePayVirtAcctProxy.makePayment(nipReq, authorization, subscriptionKey);
                    System.out.println(" NipCreditTransferResponse :::::::::::::::: %S " + new Gson().toJson(nipCre));

                    if (nipCre.getResponseCode() != "00") {
                        nipReqLog.setResponseCode(nipCre.getResponseCode());
                        nipReqLog.setResponseMessage(nipCre.getResponseMessage());
                        nipReqLog.setResponseReference(nipCre.getTransactionReference());
                        nipReqLog.setResponseCode(nipCre.getResponseCode());
                        nipReqLog.setResponseMessage(nipCre.getResponseMessage());
                        nipReqLog.setResponseReference(nipCre.getTransactionReference());
                        nipReqLog.setCreatedDate(Instant.now());
                        nipCredAcccTranLogRepo.save(nipReqLog);

                        nipCredAcccTranLogRepo.save(nipReqLog);
                        //mark for roll back
                        List<SuccessDebitLog> getSucc = successDebitLogRepo.findByTransactionId(rq.getProcessId());
                        if (getSucc.size() > 0) {

                            SuccessDebitLog getSuccUpDate = successDebitLogRepo.findByTransactionIdUpdate(rq.getProcessId());
                            getSuccUpDate.setMarkForRollBack(1);
                            getSuccUpDate.setLastModifiedDate(Instant.now());
                            successDebitLogRepo.save(getSuccUpDate);

                        }
                        responseModel.setDescription("Wallet to Bank transfer, your request is being processed, Thank you.");
                        responseModel.setStatusCode(statusCode);

                        return responseModel;
                    }
                } else {
                    NipCreditTransferResponse nipCre = new NipCreditTransferResponse();

                    nipCre.setResponseCode("00");
                    nipCre.setResponseMessage("Transaction successful");
                    nipCre.setTransactionReference("BRZ" + String.valueOf(GlobalMethods.generateTransactionId()));

                    //
                    /* if (nipCre.getResponseCode() != "00") {
                        nipReqLog.setResponseCode(nipCre.getResponseCode());
                        nipReqLog.setResponseMessage(nipCre.getResponseMessage());
                        nipReqLog.setResponseReference(nipCre.getTransactionReference());
                        nipCredAcccTranLogRepo.save(nipReqLog);
                        //mark for roll back
                        List<SuccessDebitLog> getSucc = successDebitLogRepo.findByTransactionId(rq.getProcessId());
                        if (getSucc.size() > 0) {

                            SuccessDebitLog getSuccUpDate = successDebitLogRepo.findByTransactionIdUpdate(rq.getProcessId());
                            getSuccUpDate.setMarkForRollBack(1);
                            getSuccUpDate.setLastModifiedDate(Instant.now());
                            successDebitLogRepo.save(getSuccUpDate);

                        }
                        responseModel.setDescription("Wallet to Bank transfer, your request is being processed, Thank you.");
                        responseModel.setStatusCode(statusCode);

                        return responseModel;
                    }*/
                    nipReqLog.setResponseCode(nipCre.getResponseCode());
                    nipReqLog.setResponseMessage(nipCre.getResponseMessage());
                    nipReqLog.setResponseReference(nipCre.getTransactionReference());
                    nipReqLog.setCreatedDate(Instant.now());
                    nipCredAcccTranLogRepo.save(nipReqLog);

                }

                //save to wallet to wallet log
                WToBankTransfer saveWalletT = new WToBankTransfer(
                        rq.getProcessId(), true,
                        rq.getSender(), new BigDecimal(rq.getAmount()),
                        finalChrges.add(kulFees), rq.getReceiverBankAccount(),
                        new BigDecimal(rq.getAmount()),
                        kulFees,
                        narration, getKul.get().getServiceType(), getNameLookUpDe.get(0).getLTransSessReceiverName());
                wToBankTransferRepo.save(saveWalletT);
                //update regWalletCheck
                RegWalletCheckLog logTransUp = regWalletCheckLogRepo.findByPhoneNumberId(getDecoded.phoneNumber);
                logTransUp.setProcessIdStatus("2");
                logTransUp.setLastModifiedDate(Instant.now());
                String procWalletTransferCumm = getNameLookUpDe.get(0).getWithdrawalcUMM() == null ? "0" : getNameLookUpDe.get(0).getWithdrawalcUMM();
                logTransUp.setWithdrawalcUMM(new BigDecimal(procWalletTransferCumm).add(finalChrges).toString());
                logTransUp.setLTransSessExpiry(expiry);
                regWalletCheckLogRepo.save(logTransUp);

                List<OtherBankBeneficiaries> getSavedBen = otherBankBeneficiariesRepo.findByWalletNoByBeneficiaryActive(rq.getSender(), rq.getReceiverBankAccount(), "1");
                if (getSavedBen.size() > 0) {

                    OtherBankBeneficiaries getSavedBenUp = otherBankBeneficiariesRepo.findByWalletNoByBeneficiaryActiveUpdate(rq.getSender(), rq.getReceiverBankAccount(), "1");
                    getSavedBenUp.setTransactionCount(getSavedBen.get(0).getTransactionCount() + 1);
                    getSavedBenUp.setLastModifiedDate(Instant.now());
                    otherBankBeneficiariesRepo.save(getSavedBenUp);

                }

                OtherBankBeneficiariesInd sBen = new OtherBankBeneficiariesInd();
                sBen.setBeneficiaryName(rq.getReceiverAccountName());
                sBen.setBeneficiaryNo(rq.getReceiverBankAccount());
                sBen.setBankCode(rq.getBankCode());
                sBen.setBankName(rq.getBankName());
                sBen.setBeneficiaryStatus("1");
                sBen.setCreatedDate(Instant.now());
                sBen.setWalletNo(getDecoded.phoneNumber);
                otherBankBeneficiariesIndRepo.save(sBen);

                if (getSavedBen.size() > 0) {
                    responseModel.addData("isBeneficiary", true);
                } else {
                    responseModel.addData("isBeneficiary", false);
                    responseModel.addData("receiverName", rq.getReceiverAccountName());
                }

                PushNotificationFireBase puFireSender = new PushNotificationFireBase();
                puFireSender.setBody(pushNotifyDebitWalletForWalletTransferSender(new BigDecimal(rq.getAmount()),
                        rq.getReceiverAccountName(), rq.getBankName(), getSender.get(0).getFirstName() + " " + getSender.get(0).getLastName()
                ));
                List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(getSender.get(0).getWalletId());

                puFireSender.setTitle("Wallet-To-Bank-Transfer");
                if (getDepuFireSender.size() > 0) {

                    String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                    if (getToken != "") {

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
                        messageCenterService.createAndPushToUser(getSender.get(0).getWalletId(), puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data, null, "");

                    }
                }

                responseModel.setDescription("Wallet to Bank transfer, transfer performed successfully, Thank you.");
                responseModel.setStatusCode(200);

                return responseModel;
            } else {
                responseModel.setDescription("Wallet to Bank transfer, your request is being processed, Thank you.");
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

    public static String pushNotifyDebitWalletForWalletTransferSender(BigDecimal amount, String recName, String recBank, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been debited with " + "N" + amount + " "
                + "of a transfer to " + recName + ", Bank: " + recBank + ""
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    public BaseResponse validateTransferOthers(OtherBankTransferRequest rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //validate pin (has user created pin?)
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!senderWalletdetails.get(0).isActivation()) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Customer has not created PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            //validate pin (is pin valid?)

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = senderWalletdetails.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, invalid PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, invalid transaction, invalid PIN!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;

            }
            //check if user has validate emailaddress
            if (!senderWalletdetails.get(0).isEmailVerification()) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Customer has not activated email address!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, Customer has not activated email address!");
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
                System.out.println("Customer exists in the Device Limit Tbl :::::::: " + "     ");
                System.out.println("Customer last device Date :::::::: " + "     " + deviceLimit.get(0).getLastModifiedDate() == null ? deviceLimit.get(0).getCreatedDate() : deviceLimit.get(0).getLastModifiedDate());
                System.out.println("Configured no of Day(s) :::::::: " + "     " + utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD());

                boolean isBeforeYesterday = new DateTime(deviceLimit.get(0).getLastModifiedDate() == null ? deviceLimit.get(0).getCreatedDate() : deviceLimit.get(0).getLastModifiedDate()).isBefore(DateTime.now().minusDays(Integer.valueOf(utilMeth.getSETTING_DEVICE_LIM_CHECK_PERIOD())));
                System.out.println("Customer not within Limit days?? :::::::: " + "     " + isBeforeYesterday);
                if (isBeforeYesterday != true) {
                    getActiveCat = deviceLimit.get(0).getTierCategory();
                }

            }
            List<GlobalLimitConfig> getG = globalLimitConfigRepo.findByLimitCategory(getActiveCat);
            String transType = (rq.getTransactionType() != null) ? rq.getTransactionType() : "payouttransfer";
            BigDecimal cummulative = BigDecimal.ZERO;
            BigDecimal transTypeCummulative = BigDecimal.ZERO;
            BigDecimal configAmount = BigDecimal.ZERO;
            BigDecimal singleLimit = BigDecimal.ZERO;

            BigDecimal transAmt = (!StringUtils.isEmpty(rq.getFees())) ? new BigDecimal(rq.getAmount()).add(new BigDecimal(rq.getFees())) : new BigDecimal(rq.getAmount());
            BigDecimal transAmount = BigDecimal.ZERO;
            List<RegWalletCheckLog> getNameLookUpDe = regWalletCheckLogRepo.findByProcessIdList(rq.getProcessId());

            //(receiver maximum amount limit)
            List<GlobalLimitConfig> getReciverG = null;

            switch (transType) {
                case "payouttransfer":
                    transAmount = transAmt;
                    transTypeCummulative = new BigDecimal(safeStr(getNameLookUpDe.get(0).getWithdrawalcUMM()));
                    cummulative = transTypeCummulative.add(transAmount);
                    getNameLookUpDe.get(0).setWalletTransferCumm(cummulative.toString());
                    configAmount = new BigDecimal(safeStr(getG.get(0).getDailyLimit()));
                    singleLimit = new BigDecimal(safeStr(getG.get(0).getWalletSingleTransfer()));

                    break;
                default:
                    responseModel.setStatusCode(400);
                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer, transaction not found!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );
                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    responseModel.setDescription("Wallet to Bank transfer, transaction not found");
                    return responseModel;
            }
            //(single transaction limit)
            //(daily transaction limit)

            // List<GenLedgAccountCum> genLedCum = _genLedgAccountCumRepo.findByPhoneNumberList(getDecoded.phoneNumber);
            BaseResponse getTotalBalSender = localTransferService.getTotalBalByPhoneNumb(rq.getSender());

            if (getTotalBalSender.getStatusCode() != 200) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
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
            //get account balance
            //BigDecimal accountBal;

            //a.compareTo(b) 
            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 1) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (new BigDecimal(utilMeth.minAcctBalance()).compareTo(accountBal) == 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 1) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, your account balance is insufficient. Your account balance is " + accountBal.toString());
                return responseModel;

            }

            if (transAmount.compareTo(accountBal) == 0) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry insufficient fund, Your minimum account balance is: " + utilMeth.minAcctBalance(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer - Sorry insufficient fund, Your minimum account balance is: " + utilMeth.minAcctBalance());
                return responseModel;

            }

            // ensure config amount is set
            if (configAmount.equals(BigDecimal.ZERO)) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Limit not configured!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setDescription("Limit not configured!");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            // single limit check
            if (transAmount.compareTo(singleLimit) > 0) {
                responseModel.setStatusCode(400);
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer - Sorry, your transfer amount exceed single transfer limit of " + singleLimit.toString(),
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setDescription("Wallet to Bank transfer - Sorry, your transfer amount exceed single transfer limit of " + singleLimit.toString());
                return responseModel;
            }

            //return when cummulative amount is greater than config amount
            System.out.println("other bank cummulative:::::::: req" + "   >>>>>>>>>>>>>>>>>>  " + cummulative);
            System.out.println("other bank configAmount:::::::: req" + "   >>>>>>>>>>>>>>>>>>  " + configAmount);

            if (cummulative.compareTo(configAmount) > 0) {
                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, you Have Exceeded Your Daily Transaction Limit!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );
                localTransFailedTransInfoRepo.save(procFailedTrans);
                responseModel.setStatusCode(400);
                responseModel.setDescription("Wallet to Bank transfer, you have Exceeded Your Daily Transaction Limit!");
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

    public ApiResponseModel findFrequentlyUsedBeneficiaries(WalletNoPayoutReq rq, String channel, String auth) {

        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            if (!rq.getWalletNo().equals(getDecoded.phoneNumber)) {

                PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                        "Wallet-Bank-Transfer", "Wallet to Bank transfer, Suspected fraud!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                        "Payment-Service"
                );

                responseModel.setDescription("Wallet to Bank transfer, Suspected fraud!");
                responseModel.setStatusCode(statusCode);

                localTransFailedTransInfoRepo.save(procFailedTrans);
                return responseModel;
            }
            if (getDecoded.phoneNumber != null) {

                responseModel.setDescription("Customer has no Beneficiary!");
                responseModel.setStatusCode(statusCode);

                //  procFailedRepo.save(procFailedTrans);
                return responseModel;

            }

            if (otherBankBeneficiariesIndRepo.existsByWalletNo(getDecoded.phoneNumber) == true) {
                // List<LocalBeneficiariesIndividual> getLocalBen = localBeneficiariesIndividualRepo.findByWalletNoActive(getDecoded.phoneNumber, "1");
                List<OtherBankBeneficiariesInd> getLocalBen = otherBankBeneficiariesIndRepo.findByWalletNoActive(getDecoded.phoneNumber, "1");

                if (getLocalBen.size() <= 0) {

                    PaymentsFailedTransInfo procFailedTrans = new PaymentsFailedTransInfo(
                            "Wallet-Bank-Transfer", "Wallet to Bank transfer, Customer has no Beneficiary!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Payment-Service"
                    );

                    responseModel.setDescription("Customer has no Beneficiary!");
                    responseModel.setStatusCode(statusCode);

                    localTransFailedTransInfoRepo.save(procFailedTrans);
                    return responseModel;

                }

                List<OtherBankBeneficiariesIndFind> mapAll = new ArrayList<OtherBankBeneficiariesIndFind>();
                // AllKycLevelsData allData = new AllKycLevelsData();
                if (getLocalBen.size() > 0) {

                    for (OtherBankBeneficiariesInd gConfig : getLocalBen) {

                        OtherBankBeneficiariesIndFind lData = new OtherBankBeneficiariesIndFind();

                        lData.setBeneficiaryName(gConfig.getBeneficiaryName());
                        lData.setBeneficiaryNo(gConfig.getBeneficiaryNo());
                        lData.setBankName(gConfig.getBankName());
                        lData.setBankCode(gConfig.getBankCode());

                        mapAll.add(lData);

                    }

                    responseModel.setDescription("List of Beneficiaries.");
                    responseModel.setStatusCode(200);
                    responseModel.setData(mapAll);

                }
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

    public String generateMsgId() {
        char[] buf = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            buf[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return buf.toString();
    }

    /**
     * Your requested signature and wrapper
     */
    public ApiResponseModel getAllBanks() {
        ApiResponseModel response = new ApiResponseModel();
        int statusCode = 500;
        String description = "Something went wrong";

        System.out.println("Got to before try :::::::: " + "    ::::::::::::::::::::: ");

        try {
            System.out.println("Got to try :::::::: " + "    ::::::::::::::::::::: ");

            // 1) DB first
            if (repository.count() > 0) {
                response.setData(toDtoList(repository.findAllByOrderByBankNameAsc()));
                response.setStatusCode(200);
                response.setDescription("Successful");
                // System.out.printf("adM ::::::::::::::::  %s%n", new Gson().toJson(response)); // fix %S misuse
                return response;
            }

            // 2) Filesystem seed (server path)
            if (seedFromFilesystemIfEnabledAndEmpty()) {
                System.out.println("Entered seedFromFilesystemIfEnabledAndEmpty :::::::: " + "   ");
                response.setData(toDtoList(repository.findAllByOrderByBankNameAsc()));
                response.setStatusCode(200);
                response.setDescription("Successful (seeded from filesystem)");
                return response;
            }

            // 3) Classpath seed (resources/)
            if (seedFromClasspathIfEnabledAndEmpty()) {
                response.setData(toDtoList(repository.findAllByOrderByBankNameAsc()));
                response.setStatusCode(200);
                response.setDescription("Successful (seeded from classpath)");
                return response;
            }

            // 4) Remote call once, then persist — handle BOM/encoding and hidden chars
            HttpHeaders headers = new HttpHeaders();
            if (subscriptionKey != null && !subscriptionKey.isEmpty()) {
                headers.add("Ocp-Apim-Subscription-Key", subscriptionKey);
            }
            if (authorization != null && !authorization.isEmpty()) {
                headers.add(HttpHeaders.AUTHORIZATION, authorization);
            }
            headers.add(HttpHeaders.ACCEPT_CHARSET, "UTF-8");

            // Fetch as bytes (safer for BOM/charset issues)
            ResponseEntity<byte[]> respBytes = restTemplate.exchange(
                    banksUrl + "/fetchnipbanks/fetchnipbanks",
                    HttpMethod.GET,
                    new HttpEntity<Void>(headers),
                    byte[].class
            );

            if (!respBytes.getStatusCode().is2xxSuccessful() || respBytes.getBody() == null) {
                response.setStatusCode(statusCode);
                response.setDescription("Error getting Banks!");
                return response;
            }

            // --- Begin sanitize: strip UTF-8 BOM, zero-width, control chars ---
            byte[] raw = respBytes.getBody();
            boolean hasBom = raw.length >= 3 && (raw[0] == (byte) 0xEF && raw[1] == (byte) 0xBB && raw[2] == (byte) 0xBF);
            String json = hasBom
                    ? new String(raw, 3, raw.length - 3, java.nio.charset.StandardCharsets.UTF_8)
                    : new String(raw, java.nio.charset.StandardCharsets.UTF_8);

            // Remove ZWSP/ZWJ/ZWNJ/BOM code points inside, and other control chars (keep \n\r\t)
            java.util.regex.Pattern ZERO_WIDTH = java.util.regex.Pattern.compile("[\\u200B-\\u200D\\uFEFF]");
            java.util.regex.Pattern CONTROL = java.util.regex.Pattern.compile("[\\p{Cntrl}&&[^\n\r\t]]");
            json = ZERO_WIDTH.matcher(json).replaceAll("");
            json = CONTROL.matcher(json).replaceAll("");
            json = java.text.Normalizer.normalize(json, java.text.Normalizer.Form.NFKC);
            json = json.trim();
            // --- End sanitize ---

            // Map to POJO with a slightly tolerant parser (for dirty inputs)
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

            NipBankListResponse body = mapper.readValue(json, NipBankListResponse.class);

            if (body == null) {
                response.setStatusCode(statusCode);
                response.setDescription("Error getting Banks!");
                return response;
            }
            if (!"00".equals(body.getResponseCode())) {
                response.setStatusCode(statusCode);
                response.setDescription("Request failed");
                return response;
            }

            java.util.List<NipBankItem> items = body.getGetNipBankListResponse();
            if (items == null || items.isEmpty()) {
                response.setStatusCode(statusCode);
                response.setDescription("Request failed");
                return response;
            }

            // Clean fields per item to avoid saving hidden chars into DB
            for (NipBankItem i : items) {
                if (i.getBankName() != null) {
                    String s = i.getBankName();
                    s = ZERO_WIDTH.matcher(s).replaceAll("");
                    s = CONTROL.matcher(s).replaceAll("");
                    s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC).trim();
                    i.setBankName(s);
                }
                if (i.getBankCode() != null) {
                    String s = i.getBankCode();
                    s = ZERO_WIDTH.matcher(s).replaceAll("");
                    s = CONTROL.matcher(s).replaceAll("");
                    s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC).trim();
                    i.setBankCode(s);
                }
            }

            persistDeduped(items);

            response.setStatusCode(200);
            response.setDescription("Successful");
            response.setData(toDtoList(repository.findAllByOrderByBankNameAsc()));
            // System.out.println("Final response :::::::: " + "    ::::::::::::::::::::: ");
            return response;

        } catch (org.springframework.web.client.RestClientException e) {
            e.printStackTrace();
            response.setStatusCode(statusCode);
            response.setDescription(description);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(statusCode);
            response.setDescription(description);
            response.setData(null);
            return response;
        }
    }

    /**
     * De-duplicate by bankCode and save.
     */
    private void persistDeduped(List<NipBankItem> items) {
        List<NipBankItem> deduped = dedupByCode(items);
        List<NipBank> toSave = new java.util.ArrayList<NipBank>(deduped.size());
        for (NipBankItem i : deduped) {
            if (i == null) {
                continue;
            }
            NipBank b = new NipBank();
            b.setBankCode(safe(i.getBankCode()));
            b.setBankName(safe(i.getBankName()));
            toSave.add(b);
        }
        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }
    }

    /**
     * Try seeding from a server file path when DB is empty.
     */
    private boolean seedFromFilesystemIfEnabledAndEmpty() {
        if (repository.count() > 0) {
            return false;
        }

        boolean enabled = Boolean.parseBoolean(env.getProperty("app.nipbanks.seed.fs.enabled", "false"));
        if (!enabled) {
            return false;
        }

        String pathStr = env.getProperty("app.nipbanks.seed.fs.path", "");
        if (pathStr == null || pathStr.trim().isEmpty()) {
            return false;
        }

        java.nio.file.Path path = java.nio.file.Paths.get(pathStr);
        if (!java.nio.file.Files.exists(path) || !java.nio.file.Files.isReadable(path)) {
            return false;
        }

        java.io.InputStream is = null;
        try {
            is = java.nio.file.Files.newInputStream(path);
            return loadSeedAndPersist(is);
        } catch (Exception ignore) {
            return false;
        } finally {
            if (is != null) try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Existing classpath seeding, now behind its own enable flag.
     */
    private boolean seedFromClasspathIfEnabledAndEmpty() {
        System.out.println("Entered seedFromClasspathIfEnabledAndEmpty :::::::: " + "   ");

        if (repository.count() > 0) {
            return false;
        }

        boolean enabled = Boolean.parseBoolean(env.getProperty("app.nipbanks.seed.classpath.enabled", "true"));
        System.out.println("enabled  app.nipbanks.seed.classpath.enabled :::::::: " + "   " + enabled);

        if (!enabled) {
            return false;
        }

        String cp = env.getProperty(nipBanksFilePath, "fixtures/nipbanks-dev.json");
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(cp);
        if (!resource.exists()) {
            return false;
        }

        java.io.InputStream is = null;
        try {
            is = resource.getInputStream();
            return loadSeedAndPersist(is);
        } catch (Exception ignore) {
            return false;
        } finally {
            if (is != null) try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Common loader + persist, used by both filesystem and classpath.
     */
    private boolean loadSeedAndPersist(java.io.InputStream is) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            // If your JSON uses "ResponseCode" (capital R/C), consider enabling case-insensitive props:
            mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            NipBankListResponse body = mapper.readValue(is, NipBankListResponse.class
            );
            if (body == null || !"00".equals(body.getResponseCode())) {
                return false;
            }

            List<NipBankItem> items = body.getGetNipBankListResponse();
            if (items == null || items.isEmpty()) {
                return false;
            }

            persistDeduped(items);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * De-duplicate by bankCode while keeping first seen (order-stable).
     */
    private static List<NipBankItem> dedupByCode(List<NipBankItem> items) {
        java.util.Map<String, NipBankItem> byCode = new java.util.LinkedHashMap<>();
        for (NipBankItem i : items) {
            String code = i != null ? i.getBankCode() : null;
            if (code == null) {
                continue;
            }
            if (!byCode.containsKey(code)) {
                byCode.put(code, i);
            }
        }
        return new java.util.ArrayList<>(byCode.values());
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }

    // ---- helpers
    private static List<Map<String, String>> toDtoList(List<NipBank> banks) {
        // Keep it simple for callers: [{bankCode, bankName}, ...]
        List<Map<String, String>> out = new ArrayList<Map<String, String>>();
        for (NipBank b : banks) {
            Map<String, String> m = new LinkedHashMap<String, String>(2);
            m.put("bankCode", b.getBankCode());
            m.put("bankName", b.getBankName());
            out.add(m);
        }
        return out;
    }

}
