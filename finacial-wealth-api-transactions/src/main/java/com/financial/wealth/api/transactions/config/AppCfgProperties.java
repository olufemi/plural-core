/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author HRH nxg.conf.param.bankCode
 */
@ConfigurationProperties(prefix = "fin.wealth.conf.param")
@Component
public class AppCfgProperties {
    
    //nxg.conf.param.channelCodeAgb

    private String moduleName;
    private String apiHost;
    private String channelCode;
    private String encKey;
    private String branchCode;
    // private String nairaboxMovieAuth;  nxg.conf.param.modulePrefix
    //  private String nairaboxEventAuth;
    private String bet9jaAppId;
    private String nairaboxUrl;
    private String commission;
    private String vat;
    private String sochitel;
    private String quickpayAirtimeLimit;
    private String quickpayBillsLimit;
    private String appUsr;
    private String batchNo;
    private String ccyCode;
    private String nodeName;
    private String doReversalNode;
    private String doRecurringNode;
    private String accessToken;
    private String mis2;
    private String mis4;
    private String accorGl;
    private String bet9jaProductId;
    private String bet9jaBillerId;
    private String bet9jaAccount;
    private String modulePrefix;
    private Map<String, String> rewardTransType;
    private Map<String, String> rewardVasType;

    private String bankCode;
    private String quickPayTransferLimit;

    private String authKey;
    private String newDeviceLimit;
    private String tempCategoryName;
    private String temPinCummulative;
    private String baseCcy;
    private String ownAccountModuleName;
    private String aggrNipAuth;
    private String channelCodeAgb;
    private String transType;
    private String notEligibleMsg;

    @Bean
    public RestTemplate restTemplateTrusted()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*@Bean
    @LoadBalanced
    @Qualifier("withEureka")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }*/
    public String getNotEligibleMsg() {
        return notEligibleMsg;
    }

    public void setNotEligibleMsg(String notEligibleMsg) {
        this.notEligibleMsg = notEligibleMsg;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getChannelCodeAgb() {
        return channelCodeAgb;
    }

    public void setChannelCodeAgb(String channelCodeAgb) {
        this.channelCodeAgb = channelCodeAgb;
    }

    public String getSochitel() {
        return sochitel;
    }

    public void setSochitel(String sochitel) {
        this.sochitel = sochitel;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBet9jaAppId() {
        return bet9jaAppId;
    }

    public void setBet9jaAppId(String bet9jaAppId) {
        this.bet9jaAppId = bet9jaAppId;
    }

    public String getNairaboxUrl() {
        return nairaboxUrl;
    }

    public void setNairaboxUrl(String nairaboxUrl) {
        this.nairaboxUrl = nairaboxUrl;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getQuickpayAirtimeLimit() {
        return quickpayAirtimeLimit;
    }

    public void setQuickpayAirtimeLimit(String quickpayAirtimeLimit) {
        this.quickpayAirtimeLimit = quickpayAirtimeLimit;
    }

    public String getQuickpayBillsLimit() {
        return quickpayBillsLimit;
    }

    public void setQuickpayBillsLimit(String quickpayBillsLimit) {
        this.quickpayBillsLimit = quickpayBillsLimit;
    }

    public String getEncKey() {
        return encKey;
    }

    public void setEncKey(String encKey) {
        this.encKey = encKey;
    }

    public String getAppUsr() {
        return appUsr;
    }

    public void setAppUsr(String appUsr) {
        this.appUsr = appUsr;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getCcyCode() {
        return ccyCode;
    }

    public void setCcyCode(String ccyCode) {
        this.ccyCode = ccyCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDoReversalNode() {
        return doReversalNode;
    }

    public void setDoReversalNode(String doReversalNode) {
        this.doReversalNode = doReversalNode;
    }

    public String getDoRecurringNode() {
        return doRecurringNode;
    }

    public void setDoRecurringNode(String doRecurringNode) {
        this.doRecurringNode = doRecurringNode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMis2() {
        return mis2;
    }

    public void setMis2(String mis2) {
        this.mis2 = mis2;
    }

    public String getMis4() {
        return mis4;
    }

    public void setMis4(String mis4) {
        this.mis4 = mis4;
    }

    public String getAccorGl() {
        return accorGl;
    }

    public void setAccorGl(String accorGl) {
        this.accorGl = accorGl;
    }

    public String getBet9jaProductId() {
        return bet9jaProductId;
    }

    public void setBet9jaProductId(String bet9jaProductId) {
        this.bet9jaProductId = bet9jaProductId;
    }

    public String getModulePrefix() {
        return modulePrefix;
    }

    public void setModulePrefix(String modulePrefix) {
        this.modulePrefix = modulePrefix;
    }

    public String getBet9jaBillerId() {
        return bet9jaBillerId;
    }

    public void setBet9jaBillerId(String bet9jaBillerId) {
        this.bet9jaBillerId = bet9jaBillerId;
    }

    public String getBet9jaAccount() {
        return bet9jaAccount;
    }

    public void setBet9jaAccount(String bet9jaAccount) {
        this.bet9jaAccount = bet9jaAccount;
    }

    public Map<String, String> getRewardTransType() {
        return rewardTransType;
    }

    public void setRewardTransType(Map<String, String> rewardTransType) {
        this.rewardTransType = rewardTransType;
    }

    public Map<String, String> getRewardVasType() {
        return rewardVasType;
    }

    public void setRewardVasType(Map<String, String> rewardVasType) {
        this.rewardVasType = rewardVasType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getQuickPayTransferLimit() {
        return quickPayTransferLimit;
    }

    public void setQuickPayTransferLimit(String quickPayTransferLimit) {
        this.quickPayTransferLimit = quickPayTransferLimit;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getNewDeviceLimit() {
        return newDeviceLimit;
    }

    public void setNewDeviceLimit(String newDeviceLimit) {
        this.newDeviceLimit = newDeviceLimit;
    }

    public String getTempCategoryName() {
        return tempCategoryName;
    }

    public void setTempCategoryName(String tempCategoryName) {
        this.tempCategoryName = tempCategoryName;
    }

    public String getTemPinCummulative() {
        return temPinCummulative;
    }

    public void setTemPinCummulative(String temPinCummulative) {
        this.temPinCummulative = temPinCummulative;
    }

    public String getBaseCcy() {
        return baseCcy;
    }

    public void setBaseCcy(String baseCcy) {
        this.baseCcy = baseCcy;
    }

    public String getOwnAccountModuleName() {
        return ownAccountModuleName;
    }

    public void setOwnAccountModuleName(String ownAccountModuleName) {
        this.ownAccountModuleName = ownAccountModuleName;
    }

    public String getAggrNipAuth() {
        return aggrNipAuth;
    }

    public void setAggrNipAuth(String aggrNipAuth) {
        this.aggrNipAuth = aggrNipAuth;
    }

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

}
