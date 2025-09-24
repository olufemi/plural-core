/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.domain;

import com.finacial.wealth.api.profiling.utils.GlobalMethods;
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
public class UserDetails implements Serializable {

    private String id;
    @NotNull
    @Id
    private String uniqueIdentification;

    private String emailAddress;
    private String token;

    private boolean tokenStatus;
    private String password;

    private String userName;

    private String lastName;
    private String firstName;

    private String userGroup;

    private String transactionId;
    private boolean enabled;
    private boolean changePassword;
    private String oneTimePwd;
    private boolean oneTimePwdExpired;

    private String phoneNumber;

    private String middleName;
    private String dateOfBirth;
    private String country;
    private String address;
    private String apartment;
    private String city;
    private String zipCode;
    private String state;

    private String confirmPassword;
    private String govtId;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;
    private LocalDateTime oneTimePwdDate;

    public UserDetails(String uniqueIdentification, String emailAddress, String token, boolean tokenStatus, String password, String lastName,
            String userName, String userGroup, boolean enabled, boolean changePassword, String oneTimePwd, boolean oneTimePwdExpired,
            String firstName) {

        this.id = String.valueOf(GlobalMethods.generateNUBAN());
        this.uniqueIdentification = uniqueIdentification;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userName = userName;
        this.lastName = lastName;
        this.firstName = firstName;
        this.userGroup = userGroup;
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.enabled = enabled;
        this.token = token;
        this.tokenStatus = tokenStatus;
        this.changePassword = changePassword;
        this.oneTimePwd = oneTimePwd;
        this.oneTimePwdExpired = oneTimePwdExpired;

    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
        this.setOneTimePwdDate(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        // this.id = String.valueOf(GlobalMethods.generateNUBAN());
        // this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.setModified(LocalDateTime.now());
    }

}
