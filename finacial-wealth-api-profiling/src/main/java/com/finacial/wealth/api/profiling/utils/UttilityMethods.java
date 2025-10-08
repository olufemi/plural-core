/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utils;

import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import com.finacial.wealth.api.profiling.domain.VerifyEmailAddLog;
import com.finacial.wealth.api.profiling.domain.VerifyReqIdDetailsAuth;
import com.finacial.wealth.api.profiling.proxies.UtilitiesProxy;
import com.finacial.wealth.api.profiling.repo.PinActFailedTransLogRepo;
import com.finacial.wealth.api.profiling.repo.VerifyReqIdDetailsAuthRepo;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utilities.models.OtpRequest;
import com.google.common.collect.Range;
import com.google.gson.JsonSyntaxException;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author olufemioshin
 */
@Service
public class UttilityMethods {

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

    private static final String NO_DEVICE_REGISTERED = "You dont have any device registered";
    private static final String OTP_SUCCESSFULLY_SENT = "Otp Sent SuccessFully.";
    private static final int STANDARD_SUCESS_CODE = 200;
    private final UtilitiesProxy utilitiesProxy;
    private final PinActFailedTransLogRepo pinActFailedRepo;
    private final VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo;

    MemoryCache cache;

    public UttilityMethods(MemoryCache cache,
            UtilitiesProxy utilitiesProxy, PinActFailedTransLogRepo pinActFailedRepo,
            VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo) {
        this.cache = cache;
        this.utilitiesProxy = utilitiesProxy;
        this.pinActFailedRepo = pinActFailedRepo;
        this.verifyReqIdDetailsAuthRepo = verifyReqIdDetailsAuthRepo;
    }

    @PostConstruct
    public void init() {
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

    public BaseResponse initiateSendOtp(String auth, String phoneNumber, String userName, String serviceName) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            OtpRequest otp = new OtpRequest();
            otp.setEmailAddress(emailAddress);
            otp.setUserId(userName);
            otp.setPhoneNumber(getDecoded.phoneNumber);
            otp.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");

            BaseResponse bRes = utilitiesProxy.sendOtpEmail(otp);
            if (bRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("verify-email-address",
                        bRes.getDescription(), "", "", emailAddress);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;

            }
            String otpReqId = (String) bRes.getData()
                    .get("requestId");

            VerifyReqIdDetailsAuth vDe = new VerifyReqIdDetailsAuth();
            vDe.setCreatedDate(Instant.now());
            vDe.setLastModifiedDate(Instant.now());
            vDe.setRequestId(otpReqId);
            vDe.setServiceName(serviceName);
            vDe.setUserId(phoneNumber);
            vDe.setProcessId("0");
            vDe.setExpiry(0);
            vDe.setProcessIdUsed("0");
            vDe.setProcessId(otpReqId);
            vDe.setEmailAddress(emailAddress);

            vDe.setUserIdType("phoneNumber");
            responseModel.addData("processId", otpReqId);
            //vDe.setJoinTransactionId(result.getJoinTransactionId());
            verifyReqIdDetailsAuthRepo.save(vDe);

            responseModel.addData("requestId", otpReqId);

            responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
            responseModel.setStatusCode(STANDARD_SUCESS_CODE);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

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

    private final Pattern Check10Digits = Pattern.compile("\\d{11}");

    public boolean isValid11Num(String strNum) {
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
}
