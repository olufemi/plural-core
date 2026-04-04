/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */


import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class RegWalletInfoBackofficeResponse {

    private Long id;
    private String personId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String isOnboarded;
    private boolean activation;
    private String accountBankCode;
    private String bankName;
    private String bvnNumber;
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
    private String joinTransactionId;
    private String uuidAllowUser;
    private String accountName;
    private String walletId;
    private String accountNumber;
    private LocalDateTime created;
    private LocalDateTime modified;
    private boolean completed;
    private String referralCode;
    private String referralCodeLink;
    private String isUserBlocked;
    private Instant createdDate;
    private Instant lastModifiedDate;
}
