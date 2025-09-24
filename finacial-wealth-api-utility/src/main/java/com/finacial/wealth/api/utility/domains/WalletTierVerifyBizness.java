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
import javax.persistence.Lob;
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
@Table(name = "WALLET_TIER_VER_BIZNESS")
@Data
public class WalletTierVerifyBizness implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "WALLET_TIER_VER_BIZNESS_SEQ";
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

    @Column(name = "WALLET_NO", unique = true)
    private String walletNo;

    @Column(name = "BIZ_CERTIFICATE_STATUS")
    private Integer bizCertificateStatus = 0;
    @Column(name = "BIZ_CERTIFICATE_STATUS_DESC")
    private String bizCertificateStatusDesc;
    @Lob
    @Column(name = "BIZ_CERT_BASE64", length = 100000)
    private String bizCertificateBase64Image;

    @Column(name = "PROCESS_ID", unique = true)
    private String processId;

    private String businessName;
    private String address;
    private String biznessState;
    private String city;
    private String localGovtArea;
    private String rc;
    private String walletTier;
    private String merchantId;
    private String merchantLink;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;
}
