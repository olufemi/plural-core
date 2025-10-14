/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "KUL_PAYMENT_TRANS")
@Data
public class FinWealthPaymentTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "WALLET_TIER_BVN_LOG_SEQ";
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

    Long id;

    @Column(name = "WALLET_NO")
    private String walletNo;
    private String receiver;
    private String receiverName;
    private String senderName;
    private String sender;
    private String receiverBankName;
    private String receiverBankCode;

    private String transactionType;
    private String paymentType;
    private BigDecimal paymentTypeFeeCum;
    private BigDecimal cusPaymentTypeFeeCum;
    private BigDecimal ammount;
    private BigDecimal ammountCum;
    private BigDecimal cusAmountCum;
    private BigDecimal fees;
    private String transactionId;
    private String sentAmount;
    private String theNarration;
    private String sourceAccount;
    private String senderTransactionType;
    private String receiverTransactionType;
    private String reversals;
    private String createdBy;
    private String emailAddress;
    private String requestType;
    private String airtimeReceiver;
    private String dataReceiver;
    private String billsToken;
    private String currencyCode;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;

}
