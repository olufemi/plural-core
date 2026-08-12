package com.finacial.wealth.api.profiling.testonboarding;

public class TestCadOnboardingResponse {

    private String customerId;
    private String emailAddress;
    private String phoneNumber;
    private String fullName;
    private String marketCode;
    private String countryCode;
    private String currencyCode;
    private String accountNumber;
    private String walletId;
    private boolean createdCustomer;
    private boolean createdCadAccount;
    private String cadAccountProvisionMessage;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public boolean isCreatedCustomer() {
        return createdCustomer;
    }

    public void setCreatedCustomer(boolean createdCustomer) {
        this.createdCustomer = createdCustomer;
    }

    public boolean isCreatedCadAccount() {
        return createdCadAccount;
    }

    public void setCreatedCadAccount(boolean createdCadAccount) {
        this.createdCadAccount = createdCadAccount;
    }

    public String getCadAccountProvisionMessage() {
        return cadAccountProvisionMessage;
    }

    public void setCadAccountProvisionMessage(String cadAccountProvisionMessage) {
        this.cadAccountProvisionMessage = cadAccountProvisionMessage;
    }
}
