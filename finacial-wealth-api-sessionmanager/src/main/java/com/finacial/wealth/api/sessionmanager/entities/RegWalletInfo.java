/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    private String isOnboarded;

    private boolean activation;
    private String accountBankCode;
    private String fullName;
    private String bankName;
    private String phoneNumber;
    private String middleName;

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
    /*@Column(name = "WALLET_ACCT_NO", unique = true)
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

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;

}
