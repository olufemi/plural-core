/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.utils;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
    String SETTING_KEY_BAD_VERSIONS;

    public UttilityMethods(MemoryCache cache) {
        this.cache = cache;
    }

    @PostConstruct
    public void init() {
        SETTING_KEY_BAD_VERSIONS = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_BAD_VERSIONS);
        SETTING_KEY_GET_WALLET_USER_GROUP_ID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_GET_WALLET_USER_GROUP_ID);
        SETTING_KEY_WALLET_SYSTEM_BASE_URL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_BASE_URL);
        SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PRODUCTNAME);
        SETTING_KEY_WALLET_SYSTEM_PASSWORD = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_PASSWORD);
        SETTING_KEY_WALLET_SYSTEM_EMAIL = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_EMAIL);
        SETTING_KEY_WALLET_SYSTEM_CLEARANCEID = cache.getApplicationSetting(AppConfigConUtil.SETTING_KEY_WALLET_SYSTEM_CLEARANCEID);

    }

    public String getAPPSETTING_KEY_BAD_VERSIONS() {
        return SETTING_KEY_BAD_VERSIONS;
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

    public List<String> getBadAppList() {
        List<String> convertedSETTING_KEY_BAD_APP_LIST = Stream.of(SETTING_KEY_BAD_VERSIONS.split(",", -1))
                .collect(Collectors.toList());
        return convertedSETTING_KEY_BAD_APP_LIST;
    }

    public boolean getIfAppExist(String appVersion) {
        // logger.info(String.format("channel >>>>>>=>%s", channel));

        //Before saving
        //Get list of channels and interate then check against caller-channel to validate
        List<String> apps = getBadAppList();
        //  logger.info(String.format("cutilMeth.getChannelList(); >>>>>>=>%s", channels));
        boolean setAppToF = false;
        // boolean checkOption = false;
        for (String appType : apps) {
            //    logger.info(String.format("channelType >>>>>>=>%s", channelType));
            if (appVersion.equals(appType)) {
                setAppToF = true;
            }
        }
        return setAppToF;

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
