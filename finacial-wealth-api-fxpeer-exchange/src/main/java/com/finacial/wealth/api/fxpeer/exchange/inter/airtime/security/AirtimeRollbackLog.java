package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "airtime_rollback_log")
@Data
public class AirtimeRollbackLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String processId;

    @Column(nullable = false, length = 100)
    private String legKey;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(nullable = false, length = 100)
    private String userType;

    @Column(nullable = false, length = 100)
    private String authValue;

    @Column(nullable = false, length = 100)
    private String rollbackTransactionId;

    @Column(nullable = false, length = 100)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal finalCharges;

    @Column(length = 255)
    private String narration;

    @Column(length = 100)
    private String serviceType;

    @Column(length = 150)
    private String operatorCode;

    @Column(length = 150)
    private String productCode;

    @Column(length = 1000)
    private String providerError;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    private Integer lastResponseCode;
    private int retryCount;
    private Instant requestedAt;
    private Instant completedAt;

    @Column(length = 1000)
    private String lastError;

    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdDate;

    @Column(name = "LAST_MODIFIED_DATE", columnDefinition = "TIMESTAMP")
    private Instant lastModifiedDate;
}
