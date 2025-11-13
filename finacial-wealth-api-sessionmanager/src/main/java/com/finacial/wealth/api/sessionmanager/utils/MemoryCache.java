/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.utils;

import com.finacial.wealth.api.sessionmanager.entities.AppConfig;
import com.finacial.wealth.api.sessionmanager.repository.AppConfigRepo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

        //application settings
        List<AppConfig> appSettings = loadApplicationSettings();
        settingsCache = new ConcurrentHashMap<>();
        appSettings.stream().map((appConfigSetting) -> {
            settingsCache.put(appConfigSetting.getConfigName(), appConfigSetting.getConfigValue());
            return appConfigSetting;
        }).forEachOrdered((appConfigSetting) -> {
            logger.info(String.format("%s=>%s", appConfigSetting.getConfigName(), appConfigSetting.getConfigValue()));
        });
        logger.info("Done loading settings");

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
