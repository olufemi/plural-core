/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author OSHIN
 */
@Entity
@Data
@NoArgsConstructor
public class RegWalletInfoLogForConfirmation implements Serializable {

    @NotNull
    @Id
    @Expose
    private String transactionId;

    private String phoneNumber;

    private String walletId;

    private String personId;

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

    private String walletCustomerType;

    private String accountNumber;

    private String accountName;

    private String accountBankCode;

    private String bvnNumber; //this is need for zenith bank transaction (flutterwaves)

    private String dateOfBirth;

    @Column(insertable = true, updatable = false)
    private LocalDateTime created;
    private LocalDateTime Modified;
    private boolean completed;
    private boolean transStatuscompleted;
    private String uuid;

    public RegWalletInfoLogForConfirmation(
            String personId, boolean activation, String firstName, String email, String channel, String lga,
            String lastName, String nationality, String phoneNumber, String accountNumber, String accountName, String accountBank,
            String stateOfResidence, String street, String regPoint, String walletCustomerType, String regPointCallerId,
            String transactionId, String walletId, boolean completed, String walletRegCount,
            String bvnNumber, String dateOfBirth, String accountBankCode, boolean transStatuscompleted, String uuid
    ) {
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
        this.transStatuscompleted = transStatuscompleted;
        this.uuid = uuid;

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
