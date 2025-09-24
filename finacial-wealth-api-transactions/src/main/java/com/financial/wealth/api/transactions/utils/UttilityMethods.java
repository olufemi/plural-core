/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package com.financial.wealth.api.transactions.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.FailedCreditLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.repo.FailedCreditLogRepo;
import com.google.common.collect.Range;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    MemoryCache cache;

    @Qualifier("withEureka")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FailedCreditLogRepo failedCreditLogRepo;

    public UttilityMethods(MemoryCache cache, RestTemplate restTemplate,
            FailedCreditLogRepo failedCreditLogRepo) {
        this.cache = cache;
        this.restTemplate = restTemplate;
        this.failedCreditLogRepo = failedCreditLogRepo;
    }

    @PostConstruct
    public void init() {
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

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL;
    }

    public String getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG() {
        return SETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG;
    }

    public BaseResponse debitCustomer(DebitWalletCaller rq) {
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

    public BaseResponse creditCustomer(CreditWalletCaller rq) {
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

    }

    public List<String> getNO_PERMITTED_TRASANCTIONList() {
        List<String> convertedgetNO_PERMITTED_TRASANCTIONList = Stream.of(NO_PERMITTED_TRASANCTION.split(",", -1))
                .collect(Collectors.toList());
        return convertedgetNO_PERMITTED_TRASANCTIONList;
    }

    public boolean getIfNO_PERMITTED_TRASANCTION(String userNo) {
        // logger.info(String.format("channel >>>>>>=>%s", channel));

        List<String> userNos = getNO_PERMITTED_TRASANCTIONList();
        boolean setNO_PERMITTED_TRASANCTIONToF = false;
        // boolean checkOption = false;
        for (String userNosType : userNos) {
            if (userNo.equals(userNosType)) {
                setNO_PERMITTED_TRASANCTIONToF = true;
            }
        }
        return setNO_PERMITTED_TRASANCTIONToF;

    }

    public String ltExistingRunningWindow() {
        return L_TRANSFER_STILL_RUNNING_WINDOW;
    }

    public String minAcctBalance() {
        return SETTING_MIN_ACCOUNT_BAL;
    }

    public String getSETTING_DEVICE_LIM_CHECK_PERIOD() {
        return SETTING_DEVICE_LIM_CHECK_PERIOD;
    }

    public String getSETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS() {

        return SETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS;
    }

    public String getSETTING_DATE_FORMATT() {
        return SETTING_DATE_FORMATT;
    }

    public String getSETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION() {
        return SETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION;
    }

    public String getSETTING_KEY_TRANS_G_SAVINGS_LIST_PAGENATION() {
        return SETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION;
    }

    public String getSETTING_KEY_G_INVITE_CODE_URL() {
        return SETTING_KEY_G_INVITE_CODE_URL;
    }

    public static String generateRequestId(String serviceName) {
        String servicePrevix = serviceName.substring(0, 2).toUpperCase();
        return servicePrevix + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }

    public List<String> SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST() {
        List<String> convertedSETTING_KEY_G_SAVINGS_PAY_SLOT_LIST = Stream.of(SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST.split(",", -1))
                .collect(Collectors.toList());
        return convertedSETTING_KEY_G_SAVINGS_PAY_SLOT_LIST;
    }

    public boolean getSETTING_KEY_G_SAVINGS_PAY_SLOT_LIST(String MEM_LIST) {
        logger.info(String.format("G_SAVINGS_MEM_LIST >>>>>>=>%s", MEM_LIST));

        //Before saving
        //Get list of channels and interate then check against caller-channel to validate
        List<String> G_SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST = SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST();
        logger.info(String.format("cutilMeth.SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST >>>>>>=>%s", SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST));
        boolean setChannelToF = false;
        // boolean checkOption = false;
        for (String oneTypeCate : G_SETTING_KEY_G_SAVINGS_PAY_SLOT_LIST) {
            logger.info(String.format("memList >>>>>>=>%s", oneTypeCate));
            if (MEM_LIST.equals(oneTypeCate)) {
                setChannelToF = true;
            }
        }
        return setChannelToF;

    }

    public List<String> SETTING_KEY_G_SAVINGS_MEM_LIST() {
        List<String> convertedSETTING_KEY_G_SAVINGS_MEM_LIST = Stream.of(SETTING_KEY_G_SAVINGS_MEM_LIST.split(",", -1))
                .collect(Collectors.toList());
        return convertedSETTING_KEY_G_SAVINGS_MEM_LIST;
    }

    public boolean getSETTING_KEY_G_SAVINGS_MEM_LIST(String MEM_LIST) {
        logger.info(String.format("G_SAVINGS_MEM_LIST >>>>>>=>%s", MEM_LIST));

        //Before saving
        //Get list of channels and interate then check against caller-channel to validate
        List<String> G_SAVINGS_MEM_LISTList = SETTING_KEY_G_SAVINGS_MEM_LIST();
        logger.info(String.format("cutilMeth.G_SAVINGS_MEM_LIST >>>>>>=>%s", SETTING_KEY_G_SAVINGS_MEM_LIST));
        boolean setChannelToF = false;
        // boolean checkOption = false;
        for (String oneTypeCate : G_SAVINGS_MEM_LISTList) {
            logger.info(String.format("memList >>>>>>=>%s", oneTypeCate));
            if (MEM_LIST.equals(oneTypeCate)) {
                setChannelToF = true;
            }
        }
        return setChannelToF;

    }

    public String returnWalletUserGroupId() {

        return SETTING_KEY_GET_WALLET_USER_GROUP_ID;

    }

    private final Pattern CheckEmailAdd = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

    public boolean isValidEmailAddress(String email) {
        if (email == null) {
            return false;
        }
        return CheckEmailAdd.matcher(email).matches();
    }

    public String getDevice_Change() {
        return Device_Change;
    }

    public String getWALLET_SYSTEM_BASE_URL() {
        return SETTING_KEY_WALLET_SYSTEM_BASE_URL;
    }

    public String getWALLET_SYSTEM_PRODUCTNAME() {
        return SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME;
    }

    public String getWALLET_SYSTEM_PASSWORD() {

        return SETTING_KEY_WALLET_SYSTEM_PASSWORD;
    }

    public String getWALLET_SYSTEM_EMAIL() {
        return SETTING_KEY_WALLET_SYSTEM_EMAIL;
    }

    public String getWALLET_SYSTEM_CLEARANCEID() {
        return SETTING_KEY_WALLET_SYSTEM_CLEARANCEID;
    }

    public String getTier1() {
        return SETTING_KEY_GET_TIER_1;
    }

    public String getTier2() {
        return SETTING_KEY_GET_TIER_2;
    }

    public String getTier3() {
        return SETTING_KEY_GET_TIER_3;
    }

    public String getTier4() {
        return SETTING_KEY_GET_TIER_4;
    }

    public String getSETTING_REF_LINK() {
        return SETTING_REF_LINK;
    }

    public String encyrpt(String text, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String generateReferralCode(String serviceName) {
        String servicePrevix = serviceName.substring(0, 2).toUpperCase();
        return servicePrevix + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }

    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public boolean isNumeric(String strNum) {

        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{6,20}$";

    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    public boolean isPasswordValid(String password) {
        Matcher matcher = PATTERN.matcher(password);
        return matcher.matches();
    }

    private final Pattern Check11Digits = Pattern.compile("^\\d{11}$");

    public boolean isValid11Num(String strNum) {
        if (strNum == null) {
            return false;
        }
        return Check11Digits.matcher(strNum).matches();
    }

    private final Pattern Check10Digits = Pattern.compile("^\\d{10}$");

    public boolean isValid10Num(String strNum) {
        if (strNum == null) {
            return false;
        }
        return Check10Digits.matcher(strNum).matches();
    }

    private final Pattern Check4Digits = Pattern.compile("\\d{4}");

    public boolean isValid4um(String strNum) {
        if (strNum == null) {
            return false;
        }
        return Check4Digits.matcher(strNum).matches();
    }

    public boolean isValidFormat(String format, String value, Locale locale) {
        java.time.LocalDateTime ldt = null;
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

        try {
            ldt = java.time.LocalDateTime.parse(value, fomatter);
            String result = ldt.format(fomatter);
            return result.equals(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(value, fomatter);
                String result = ld.format(fomatter);
                return result.equals(value);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(value, fomatter);
                    String result = lt.format(fomatter);
                    return result.equals(value);
                } catch (DateTimeParseException e2) {
                }
            }
        }

        return false;
    }

    public static boolean amountsEqual(BigDecimal a, String b) {
        if (a == null || b == null || b.trim().isEmpty()) {
            return false;
        }
        try {
            return a.compareTo(new BigDecimal(b.trim())) == 0;
        } catch (NumberFormatException e) {
            return false; // or throw
        }
    }
}
