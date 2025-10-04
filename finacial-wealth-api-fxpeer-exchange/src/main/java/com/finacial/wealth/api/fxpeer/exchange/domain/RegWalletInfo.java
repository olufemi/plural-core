/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;

import java.time.LocalDateTime;

import lombok.Data;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


/**
 *
 * @author olufemioshin
 */
@Entity

public class RegWalletInfo extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "RegWalletInfo_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "300"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @Column(name = "ID")
    Long id;

    private String personId;

    private String password;
    private String firstName;
    private String lastName;
    private String email;

    private boolean activation;
    private String accountBankCode;
    private String fullName;
    private String bankName;
    private String phoneNumber;

    private String bvnNumber; //this is need for zenith bank transaction (flutterwaves)
    private String dateOfBirth;
    private String client;
    private String customerId;
    private String uuid;
    private String userName;
    private boolean emailVerification;
    private String emailCreation;
    private String livePhotoUpload;
    private String phoneVerification;
    private String walletTier;
    private String uerDeviceCustomer;
    private String joinTransactionId;
    private String pushNotificationToken;
    private String uuidAllowUser;
   /* @Column(name = "WALLET_ACCT_NO", unique = true)
    private String walletAccountNumber;*/
    private String securityQue;
    private String securityAnswer;
    private String accountName;
    private String walletId;
    private String accountNumber;

    @Column(insertable = true, updatable = false)
    private LocalDateTime created;
    private LocalDateTime Modified;
    private boolean completed;
    @Column(name = "REFERRAL_CODE", unique = true)
    private String referralCode;
    @Column(name = "REFERRAL_LINK", unique = true)
    private String referralCodeLink;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivation() {
        return activation;
    }

    public void setActivation(boolean activation) {
        this.activation = activation;
    }

    public String getAccountBankCode() {
        return accountBankCode;
    }

    public void setAccountBankCode(String accountBankCode) {
        this.accountBankCode = accountBankCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBvnNumber() {
        return bvnNumber;
    }

    public void setBvnNumber(String bvnNumber) {
        this.bvnNumber = bvnNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isEmailVerification() {
        return emailVerification;
    }

    public void setEmailVerification(boolean emailVerification) {
        this.emailVerification = emailVerification;
    }

    public String getEmailCreation() {
        return emailCreation;
    }

    public void setEmailCreation(String emailCreation) {
        this.emailCreation = emailCreation;
    }

    public String getLivePhotoUpload() {
        return livePhotoUpload;
    }

    public void setLivePhotoUpload(String livePhotoUpload) {
        this.livePhotoUpload = livePhotoUpload;
    }

    public String getPhoneVerification() {
        return phoneVerification;
    }

    public void setPhoneVerification(String phoneVerification) {
        this.phoneVerification = phoneVerification;
    }

    public String getWalletTier() {
        return walletTier;
    }

    public void setWalletTier(String walletTier) {
        this.walletTier = walletTier;
    }

    public String getUerDeviceCustomer() {
        return uerDeviceCustomer;
    }

    public void setUerDeviceCustomer(String uerDeviceCustomer) {
        this.uerDeviceCustomer = uerDeviceCustomer;
    }

    public String getJoinTransactionId() {
        return joinTransactionId;
    }

    public void setJoinTransactionId(String joinTransactionId) {
        this.joinTransactionId = joinTransactionId;
    }

    public String getPushNotificationToken() {
        return pushNotificationToken;
    }

    public void setPushNotificationToken(String pushNotificationToken) {
        this.pushNotificationToken = pushNotificationToken;
    }

    public String getUuidAllowUser() {
        return uuidAllowUser;
    }

    public void setUuidAllowUser(String uuidAllowUser) {
        this.uuidAllowUser = uuidAllowUser;
    }

    public String getSecurityQue() {
        return securityQue;
    }

    public void setSecurityQue(String securityQue) {
        this.securityQue = securityQue;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return Modified;
    }

    public void setModified(LocalDateTime Modified) {
        this.Modified = Modified;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getReferralCodeLink() {
        return referralCodeLink;
    }

    public void setReferralCodeLink(String referralCodeLink) {
        this.referralCodeLink = referralCodeLink;
    }



}
