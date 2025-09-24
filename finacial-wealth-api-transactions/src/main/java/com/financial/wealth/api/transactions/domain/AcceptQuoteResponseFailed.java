/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@Table(name = "ACCEPT_QUOTE_RESPONSE_FAILED")
public class AcceptQuoteResponseFailed implements Serializable {

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

    private boolean success;
    private String message;

    private String quoteId; // use String for widest compatibility (can be UUID if you prefer)

    private String status; // ACCEPTED, etc.

    private String transactionId;

    private String type;     // e.g., INTERAC
    private String email;
    private BigDecimal amount;    // "105.00" -> BigDecimal
    private String currency;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;

}
