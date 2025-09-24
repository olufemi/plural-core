/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class ProvidusValAcctResLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "ProvidusValAcctResLog_SEQ";
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

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

    private String bankCode;
    private String accountName;
    private String transactionReference;
    private String bvn;
    private String responseMessage;
    private String accountNumber;
    private String responseCode;

}
