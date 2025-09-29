/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@Table(name = "CREATE_QUOTE_RESPONSE_LOG")
public class CreateQuoteResLog implements Serializable {

    private static final String SEQ_NAME = "CREATE_QUOTE_RESPONSE_LOG_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(
            name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "4"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @JsonIgnore
    @Column(name = "ID")
    private Long id;

    public String quoteId;
    public String status;
    public String totalFees;
    public String feeCurrency;
    public String createdAt;
    public String validUntil;
    public String firstName;
    public String lastName;
    public String bankName;
    public String currencyCode;
    public String countryCode;
    public String email;
    public String amount;
    public String paymentType;
    public String isAccepted;
    public String isDebited;
    public String isDebitedDescription;
    @Lob
    @Column(name = "CREATE_QUOTE_RESPONSE", length = 300000)
    private String createQuoteResponse;

    @Lob
    @Column(name = "ACCEPT_QUOTE_RESPONSE", length = 300000)
    private String acceptQuoteResponse;

    @Lob
    @Column(name = "WEBHOOK_SUCCESS_RESPONSE", length = 300000)
    private String webHookSuccResponse;

    @JsonIgnore
    @Column(name = "WALLET_NUMBER")
    private String walletNumber;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

}
