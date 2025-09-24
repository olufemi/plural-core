/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.utils;

import java.security.InvalidKeyException;
import java.security.Key;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author olufemioshin
 */
@Service
public class UttilityMethods {

    MemoryCache cache;

    String SETTING_KEY_GET_WALLET_USER_GROUP_ID;

    String SETTING_KEY_WALLET_SYSTEM_BASE_URL;
    String SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME;
    String SETTING_KEY_WALLET_SYSTEM_PASSWORD;
    String SETTING_KEY_WALLET_SYSTEM_EMAIL;
    String SETTING_KEY_WALLET_SYSTEM_CLEARANCEID;

    public UttilityMethods(MemoryCache cache) {
        this.cache = cache;
    }

    @PostConstruct
    public void init() {

        SETTING_KEY_GET_WALLET_USER_GROUP_ID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_WALLET_USER_GROUP_ID);
        SETTING_KEY_WALLET_SYSTEM_BASE_URL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_BASE_URL);
        SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME);
        SETTING_KEY_WALLET_SYSTEM_PASSWORD = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PASSWORD);
        SETTING_KEY_WALLET_SYSTEM_EMAIL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_EMAIL);
        SETTING_KEY_WALLET_SYSTEM_CLEARANCEID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_CLEARANCEID);

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

    public String returnWalletUserGroupId() {

        return SETTING_KEY_GET_WALLET_USER_GROUP_ID;

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

    public String decrypt(String encrypted, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // encrypt the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        return decrypted;
    }

}
