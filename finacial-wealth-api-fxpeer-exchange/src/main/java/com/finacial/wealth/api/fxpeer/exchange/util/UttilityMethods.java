/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthPayServiceConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthServiceConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.FxPeersCommissionCfg;
import com.finacial.wealth.api.fxpeer.exchange.domain.FxPeersCommissionCfgRepo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FailedCreditLog;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FailedCreditLogRepo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FailedDebitLog;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FailedDebitLogRepo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.SuccessDebitLog;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.SuccessDebitLogRepo;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UttilityMethods {

    private final Logger logger = LoggerFactory.getLogger(UttilityMethods.class);

    String SETTING_REF_LINK;
    String SETTING_MER_LINK;
    String SETTING_KEY_GET_TIER_1;
    String SETTING_KEY_GET_TIER_2;
    String SETTING_KEY_GET_TIER_3;
    String SETTING_KEY_GET_TIER_4;

    String SETTING_KEY_WALLET_SYSTEM_BASE_URL;
    String SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME;
    String SETTING_KEY_WALLET_SYSTEM_PASSWORD;
    String SETTING_KEY_WALLET_SYSTEM_EMAIL;
    String SETTING_KEY_WALLET_SYSTEM_CLEARANCEID;
    String SETTING_KEY_GET_WALLET_USER_GROUP_ID;
    String Device_Change;
    String SETTING_KEY_G_SAVINGS_MEM_LIST;
    String SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST;
    String SETTING_KEY_G_INVITE_CODE_URL;
    String SETTING_KEY_TRANS_G_SAVINGS_LIST_PAGENATION;
    String SETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION;
    String SETTING_DATE_FORMATT;
    String SETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS;
    String SETTING_DEVICE_LIM_CHECK_PERIOD;
    String SETTING_MIN_ACCOUNT_BAL;
    String L_TRANSFER_STILL_RUNNING_WINDOW;
    String NO_PERMITTED_TRASANCTION;
    String SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD;
    String SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL;
    String SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG;
    String SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_NIG;
    String SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_CAD;
    MemoryCache cache;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    private final FxPeersCommissionCfgRepo fxPeersCommissionCfgRepo;
    private final FinWealthServiceConfigRepo finWealthServiceConfigRepo;
    private final SuccessDebitLogRepo successDebitLogRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FailedDebitLogRepo failedDebitLogRepo;
    private final FailedCreditLogRepo failedCreditLogRepo;

//    @Qualifier("withEureka")
//    @Autowired
//    private RestTemplate restTemplate;
    public UttilityMethods(FxPeersCommissionCfgRepo fxPeersCommissionCfgRepo,
            FinWealthServiceConfigRepo finWealthServiceConfigRepo,
            MemoryCache cache, SuccessDebitLogRepo successDebitLogRepo,
            FailedDebitLogRepo failedDebitLogRepo, FailedCreditLogRepo failedCreditLogRepo
    //  ,RestTemplate restTemplate
    ) {
        this.fxPeersCommissionCfgRepo = fxPeersCommissionCfgRepo;
        this.finWealthServiceConfigRepo = finWealthServiceConfigRepo;
        this.cache = cache;
        this.successDebitLogRepo = successDebitLogRepo;
        this.failedDebitLogRepo = failedDebitLogRepo;
        this.failedCreditLogRepo = failedCreditLogRepo;
        //   this.restTemplate = restTemplate;

    }

    @PostConstruct
    public void init() {
        SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_CAD = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_CAD);
        SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_NIG = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_NIG);
        SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD);
        SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL);
        SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG);

        NO_PERMITTED_TRASANCTION = cache.getApplicationSetting(AppConfigConUtil.NO_PERMITTED_TRASANCTION);
        L_TRANSFER_STILL_RUNNING_WINDOW = cache.getApplicationSetting(AppConfigConUtil.L_TRANSFER_STILL_RUNNING_WINDOW);
        SETTING_MIN_ACCOUNT_BAL = cache.getApplicationSetting(AppConfigConUtil.SETTING_MIN_ACCOUNT_BAL);
        SETTING_DEVICE_LIM_CHECK_PERIOD = cache.getApplicationSetting(AppConfigConUtil.SETTING_DEVICE_LIM_CHECK_PERIOD);
        SETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS);
        SETTING_DATE_FORMATT = cache.getApplicationSetting(AppConfigConUtil.SETTING_DATE_FORMATT);
        SETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION = cache.getApplicationSetting(AppConfigConUtil.SETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION);
        SETTING_KEY_TRANS_G_SAVINGS_LIST_PAGENATION = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_TRANS_G_SAVINGS_LIST_PAGENATION);
        SETTING_KEY_GET_WALLET_USER_GROUP_ID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_WALLET_USER_GROUP_ID);

        SETTING_KEY_WALLET_SYSTEM_BASE_URL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_BASE_URL);
        SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME);
        SETTING_KEY_WALLET_SYSTEM_PASSWORD = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PASSWORD);
        SETTING_KEY_WALLET_SYSTEM_EMAIL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_EMAIL);
        SETTING_KEY_WALLET_SYSTEM_CLEARANCEID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_CLEARANCEID);

        SETTING_REF_LINK = cache.getApplicationSetting(AppConfigConUtil.SETTING_REF_LINK);
        SETTING_MER_LINK = cache.getApplicationSetting(AppConfigConUtil.SETTING_MER_LINK);
        SETTING_KEY_GET_TIER_1 = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_TIER_1);
        SETTING_KEY_GET_TIER_2 = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_TIER_2);
        SETTING_KEY_GET_TIER_3 = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_TIER_3);
        SETTING_KEY_GET_TIER_4 = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_TIER_4);
        Device_Change = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_DEVICE_CHANGE);
        SETTING_KEY_G_SAVINGS_MEM_LIST = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_G_SAVINGS_MEM_LIST);
        SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST);
        SETTING_KEY_G_INVITE_CODE_URL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST);

    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_CAD() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_CAD;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_NIG() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_BONUS_GG_NIG;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG;
    }

    public String getClaimFromJwt(String authorization, String claimName) {
        try {
            if (authorization == null) {
                return null;
            }
            String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            // Base64URL decode payload
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(payload, StandardCharsets.UTF_8);

            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
            return obj.has(claimName) && !obj.get(claimName).isJsonNull()
                    ? obj.get(claimName).getAsString()
                    : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<FxPeersCommissionCfg> findAllByTransactionType(String transType, String currencyCode) {

        List<FxPeersCommissionCfg> getAllPartActiveNoti = fxPeersCommissionCfgRepo.findAllByTransactionTypeAndCurrencyCode(transType, currencyCode);

        return getAllPartActiveNoti;
    }

    public static boolean betweenTransBand(BigDecimal i, BigDecimal minValueInclusive, BigDecimal maxValueInclusive) {
        return i.subtract(minValueInclusive).signum() >= 0 && i.subtract(maxValueInclusive).signum() <= 0;

    }

    public BigDecimal returnPerResult(BigDecimal value, BigDecimal percentage) {

        // (value * percentage) / 100
        BigDecimal result = value.multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        System.out.println(percentage + "% of " + value + " =  result:" + result);
        return result;
    }

    public BaseResponse getFeesConfig(ManageFeesConfigReq rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String description = "Something went wrong";

        try {
            System.out.println(" ManageFeesConfigReq rq ::::::::::::::::  %S  " + new Gson().toJson(rq));

            statusCode = 400;
            BigDecimal pFees = BigDecimal.ZERO;
            BigDecimal ammmount = new BigDecimal(rq.getAmount());

            List<FinWealthPayServiceConfig> getKulList = finWealthServiceConfigRepo.findByServiceTypeAndCurrencyCode(rq.getTransType(), rq.getCurrencyCode());

            List<FxPeersCommissionCfg> pullData = findAllByTransactionType(rq.getTransType(), rq.getCurrencyCode());

            for (FxPeersCommissionCfg partData : pullData) {
                if (getKulList.get(0).getServiceType().trim().equals(partData.getTransType())) {

                    if (betweenTransBand(new BigDecimal(rq.getAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {

                        //compute the fees
                        //1.8% + 100 (convenience fee)
                        System.out.println("rq.getAmount()" + "  :::::::::::::::::::::   " + rq.getAmount());
                        System.out.println("pullData.get(0).getFee()" + "  :::::::::::::::::::::   " + partData.getFee());
                        if (partData.getHasPercent().equals("1")) {
                            pFees = returnPerResult(ammmount, partData.getCommPercent());
                        } else {
                            pFees = partData.getFee();
                        }
                        System.out.println("pFees" + "  :::::::::::::::::::::   " + pFees);
                        if (getKulList.size() <= 0) {
                            responseModel.setDescription("Peer to Peer service, service type not configured!");
                            responseModel.setStatusCode(statusCode);

                            return responseModel;
                        }

                        if (!getKulList.get(0).isEnabled()) {

                            responseModel.setDescription("Peer to Peer service, service type is disabled!");
                            responseModel.setStatusCode(statusCode);

                            return responseModel;

                        }

                        Optional<FinWealthPayServiceConfig> getKul = finWealthServiceConfigRepo.findAllByServiceType(rq.getTransType(), rq.getCurrencyCode());

                        String flagMinAmt = "Minimum amount cannot be less than N" + getKul.get().getMinimumAmmount() + ".00, please check!";
                        if (new BigDecimal(rq.getAmount()).compareTo(new BigDecimal(getKul.get().getMinimumAmmount())) == -1) {

                            responseModel.setDescription("Peer to Peer service transfer, " + flagMinAmt);
                            responseModel.setStatusCode(statusCode);

                            return responseModel;
                        }

                    }

                    Map reList = new HashMap();
                    reList.put("fees", pFees);
                    responseModel.setData(reList);

                    responseModel.setDescription("Peer to Peer service, success");
                    responseModel.setStatusCode(200);
                    return responseModel;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();

            responseModel.setDescription(description);
            responseModel.setStatusCode(statusCode);
        }
        return responseModel;
    }

    public String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);

        // log.info("decryptData ::::: {} ", decryptData);
        return decryptData;

    }

    /*public BaseResponse debitCustomerWithType(DebitWalletCaller rq, String type, String countryCode) {
        BaseResponse baseResponse = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            BaseResponse reqres = restTemplate.postForObject("http://" + "utilities-service" + "/walletmgt/account/debit-Wallet-phone",
                    rq, BaseResponse.class);
            System.out.println("debitCustomer Response from core ::::::::::::::::  %S  " + new Gson().toJson(reqres));

            if (reqres.getStatusCode() == HttpServletResponse.SC_OK) {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                baseResponse.setData(reqres.getData());
                SuccessDebitLog log = new SuccessDebitLog();
                log.setPayloadType(type);
                log.setRequestJson(objectMapper.writeValueAsString(rq));
                log.setTransactionId(rq.getTransactionId());
                log.setNarration(rq.getNarration());
                log.setRetryCount(0);
                log.setResolved(true);
                log.setCountryCode(countryCode);
                log.setMarkForRollBack(0);
                log.setCreatedDate(Instant.now());
                successDebitLogRepo.save(log);
            } else {
                FailedDebitLog log = new FailedDebitLog();
                log.setPayloadType(type);
                log.setRequestJson(objectMapper.writeValueAsString(rq));
                log.setTransactionId(rq.getTransactionId());
                log.setNarration(rq.getNarration());
                log.setRetryCount(0);
                log.setMarkForRollBack(0);
                log.setResolved(false);
                log.setCreatedDate(Instant.now());
                failedDebitLogRepo.save(log);
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception ex) {
            baseResponse.setDescription(statusMessage);
            baseResponse.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return baseResponse;

    }*/

 /*public BaseResponse creditCustomer(CreditWalletCaller rq) {
        BaseResponse baseResponse = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            BaseResponse reqres = restTemplate.postForObject("http://" + "utilities-service" + "/walletmgt/account/credit-Wallet-phone",
                    rq, BaseResponse.class);
            if (reqres.getStatusCode() == HttpServletResponse.SC_OK) {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                baseResponse.setData(reqres.getData());
            } else {
                FailedCreditLog log = new FailedCreditLog();
                log.setPayloadType("LocalTransfer");
                log.setRequestJson(objectMapper.writeValueAsString(rq));
                log.setTransactionId(rq.getTransactionId());
                log.setNarration(rq.getNarration());
                log.setRetryCount(0);
                log.setResolved(false);
                log.setCreatedDate(Instant.now());
                failedCreditLogRepo.save(log);
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

    public BaseResponse creditCustomerWithType(CreditWalletCaller rq, String type) {
        BaseResponse baseResponse = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            BaseResponse reqres = restTemplate.postForObject("http://" + "utilities-service" + "/walletmgt/account/credit-Wallet-phone",
                    rq, BaseResponse.class);
            if (reqres.getStatusCode() == HttpServletResponse.SC_OK) {
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                baseResponse.setData(reqres.getData());
            } else {
                FailedCreditLog log = new FailedCreditLog();
                log.setPayloadType(type);
                log.setRequestJson(objectMapper.writeValueAsString(rq));
                log.setTransactionId(rq.getTransactionId());
                log.setNarration(rq.getNarration());
                log.setRetryCount(0);
                log.setResolved(false);
                log.setCreatedDate(Instant.now());
                failedCreditLogRepo.save(log);
                baseResponse.setDescription(reqres.getDescription());
                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception ex) {
            baseResponse.setDescription(statusMessage);
            baseResponse.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return baseResponse;

    }*/
}
