/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.utils;

import com.financial.wealth.api.transactions.domain.AppConfig;
import com.financial.wealth.api.transactions.repo.AppConfigRepo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 *
 * @author OSHIN
 */
@Service
public class MemoryCache {

    private final Logger logger = LoggerFactory.getLogger(MemoryCache.class);

    Map<String, Object> propertySource = new HashMap<>();
    private Map<String, String> settingsCache;

    private final AppConfigRepo appConfigRepo;

    public MemoryCache(AppConfigRepo appConfigRepo) {
        this.appConfigRepo = appConfigRepo;
    }

    @PostConstruct
    public void init() {
        logger.info("#############################################");
        logger.info("###      Setting up the CACHE INSTANCE     #####");
        logger.info("#############################################");

        List<AppConfig> appSettings = loadApplicationSettings();
        settingsCache = new ConcurrentHashMap<>();

        appSettings.forEach(appConfigSetting -> {
            settingsCache.put(
                    appConfigSetting.getConfigName(),
                    appConfigSetting.getConfigValue()
            );

            logger.info("{}=>{}",
                    appConfigSetting.getConfigName(),
                    mask(appConfigSetting.getConfigName(),
                            appConfigSetting.getConfigValue()));
        });

        logger.info("Done loading settings");
    }

    private String mask(String key, String value) {
        if (value == null) {
            return null;
        }

        String k = key.toLowerCase();

        // hard secrets
        if (k.contains("password")
                || k.contains("secret")
                || k.contains("token")
                || k.contains("clearance")
                || k.contains("privatekey")
                || k.contains("apikey")) {
            return "****";
        }

        // email masking
        if (k.contains("email")) {
            int at = value.indexOf("@");
            return at > 1 ? value.substring(0, 1) + "****" + value.substring(at) : "****";
        }

        // phone masking
        if (k.contains("phone") || k.contains("msisdn")) {
            return value.length() > 4
                    ? "****" + value.substring(value.length() - 4)
                    : "****";
        }

        return value;
    }

    private List<AppConfig> loadApplicationSettings() {
        return appConfigRepo.findAll();
    }

    /**
     * Get the application setting with the specified key
     *
     * @param key The key whose value should be fetched
     * @return
     */
    public String getApplicationSetting(String key) {

        String value = settingsCache.get(key);
        logger.info(String.format("key=>%s, value=>%s", key, value));

        return settingsCache.get(key);
    }

    @PreDestroy
    public void shutdown() {

    }

    public void invalidateCaches() {
        logger.info("********************************************************************");
        logger.info("***************      Invalidating all caches       *****************");
        logger.info("********************************************************************");

        shutdown();
        init();
    }

    public void dumpCacheStats() {
        logger.info("********************************************************************");
        logger.info("*******************    Settings stats   ****************************");
        logger.info("********************************************************************");

        logger.info("+++          Stats: " + settingsCache + " +++");
        logger.info("+++          Size: " + settingsCache.size() + "   +++");

        logger.info("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        logger.info("+++++++++++++++++++     TagMessages stats     +++++++++++++++++++++++");
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        logger.info("+++          Stats: " + 0 + " +++");
        logger.info("+++          Size: " + 0 + "   +++");

        logger.info("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        logger.info("+++++++++++++++++++     All Req-Routers stats     ++++++++++++++++++++++++++");
        logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        logger.info("********************************************************************");
    }

}
