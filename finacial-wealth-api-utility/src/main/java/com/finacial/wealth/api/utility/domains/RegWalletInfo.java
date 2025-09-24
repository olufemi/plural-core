/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 *
 * @author OSHIN
 */
@Entity
@Data
@NoArgsConstructor
public class RegWalletInfo implements Serializable {

    @NotNull
    @Id
    @Expose
    private String phoneNumber;

    private String walletId;

    private String personId;
    private String oldPersonId;
    private String password;

    private boolean activation;

    private String firstName;

    private String lastName;

    private String email;

    private String street;

    private String lga;

    private String stateOfResidence;

    private String nationality;

    private String channel;

    private String regPoint;

    private String regPointCallerId;
    private String walletRegCount;

    private String transactionId;

    private String walletCustomerType;

    private String accountNumber;

    private String accountName;

    private String accountBankCode;

    private String bvnNumber; //this is need for zenith bank transaction (flutterwaves)
    private String dateOfBirth;
    private String client;
    private String customerId;
    private String uuid;
    private String userName;
    private boolean emailVerification;
    private String uuidAllowUser;

    private String pushNotificationToken;
    @Column(name = "WALLET_ACCT_NO", unique = true)
    private String walletAccountNumber;

    @Column(insertable = true, updatable = false)
    private LocalDateTime created;
    private LocalDateTime Modified;
    private boolean completed;
    String walletTier;
    private String uerDeviceCustomer;
    private String webMobileDeviceCustomer;
    @Column(name = "REFERRAL_CODE", unique = true)
    private String referralCode;
    @Column(name = "REFERRAL_LINK", unique = true)
    private String referralCodeLink;

    public RegWalletInfo(String personId, boolean activation, String firstName, String email, String channel, String lga,
            String lastName, String nationality, String phoneNumber, String accountNumber, String accountName, String accountBank,
            String stateOfResidence, String street, String regPoint, String walletCustomerType, String regPointCallerId,
            String transactionId, String walletId, boolean completed, String walletRegCount, String bvnNumber,
            String dateOfBirth, String accountBankCode,
            String client, String customerId, String uuid, String userName, boolean emailVerification,
            String password, String oldPersonId, String walletTier, String walletAccountNumber,
            String uuidAllowUser) {
        this.personId = personId;
        this.activation = activation;
        this.firstName = firstName;
        this.email = email;
        this.channel = channel;
        this.lga = lga;
        this.lastName = lastName;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.stateOfResidence = stateOfResidence;
        this.street = street;
        this.regPoint = regPoint;
        this.regPointCallerId = regPointCallerId;
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.completed = completed;
        this.walletRegCount = walletRegCount;
        this.walletCustomerType = walletCustomerType;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.bvnNumber = bvnNumber; //required for uba trans
        this.dateOfBirth = dateOfBirth; //required for zenith-bank
        this.accountBankCode = accountBankCode;
        this.client = client;
        this.customerId = customerId;
        this.uuid = uuid;
        this.userName = userName;
        this.emailVerification = emailVerification;
        this.password = password;
        this.oldPersonId = oldPersonId;
        this.walletTier = walletTier;
        this.walletAccountNumber = walletAccountNumber;
        this.uuidAllowUser = uuidAllowUser;

    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.setModified(LocalDateTime.now());

    }

}
