/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name = "REG_WALLET_CHECK")
@Data
public class RegWalletCheckLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "REG_WALLET_CHECK_SEQ";
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
    @Column(name = "USER_ID", unique = true)
    private String userId;
    private String accountName;
    @Column(name = "WALLET_NO", unique = true)
    private String walletNo;
    @Column(name = "CUSTOMER_ID")
    private String custID;
    @Column(name = "UUID")
    private String uuid;
    @Column(name = "PHONE_NUMBER", unique = true)
    private String phoneNumber;
    @Column(name = "APPROVAL")
    private boolean approval;
    @Column(name = "APP_VERSION")
    private String appVersion;
    @Column(name = "WALLET_TIER")
    private String walletTier;

    @Column(name = "WITHDRAWAL_CUMMULATIVE")
    private String withdrawalcUMM;

    @Column(name = "ONE_TIME_PAY_TRANS_cumm")
    private String oneTimePaymentTransfercUMM;

    @Column(name = "MILESTONE_TRANS_CUMM")
    private String milePaymentTransferCumm;

    @Column(name = "WALLET_TRANS_CUMM")
    private String walletTransferCumm;

    @Column(name = "WALLET_DEPOSIT_CUMM")
    private String walletDepositCumm;

    @Column(name = "LAST_TRANSACTION_TIME")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastTransactionTime;

    @LastModifiedDate
    @Column(name = "LAST_PIN_RESET_DATE", updatable = true, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastPinResetDate;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;
    private String lastTransKey;
    private Long lastTransTimeBuffer;
    @Column(name = "LAST_LOGIN_DAY")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastLoginDay;

}
