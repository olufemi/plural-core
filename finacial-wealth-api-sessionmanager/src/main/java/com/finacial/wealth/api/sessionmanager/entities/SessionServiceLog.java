package com.finacial.wealth.api.sessionmanager.entities;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "SESSION_SERVICE_LOG")
@Data
public class SessionServiceLog implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "FELLOWPAY_LT_SEQ";
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
    private Long id;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "UUID")
    private String uuId;

    @Column(name = "LOGIN_IP")
    private String logIP;

    @Column(name = "API_RESPONSE")
    private String apiResponse;

    @Column(name = "EXCEPTIONS")
    private String exceptions;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "APP_VERSION")
    private String appVersion;
    private String channel;
    private String customerType;

    @Column(name = "AUTH_EXPERIENCE_CENTER")
    private String authExperienceCenter;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

}
