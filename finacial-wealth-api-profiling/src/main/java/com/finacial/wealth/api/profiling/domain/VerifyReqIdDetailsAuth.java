/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.domain;

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
public class VerifyReqIdDetailsAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "VER_REQ_ID_AUTH_SEQ";
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

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERIDTYPE")
    private String userIdType;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_ID_USED")
    private String processIdUsed;

    @Column(name = "SERVICE_NAME")
    private String serviceName;
    
    private String joinTransactionId;

    @Column(name = "REQUEST_ID", unique = true)
    private String requestId;
    
    private String emailAddress;
    
     @Column(name = "EXPIRY")
    private long expiry;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;

}
