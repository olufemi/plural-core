package com.financial.wealth.api.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Data
public class DeviceChangeLimitConfig implements Serializable {

    private static final String SEQ_NAME = "DEVICE_CHANGE_LIMIT_SEQ";

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

    @JsonIgnore
    @Column(name = "WALLET_NUMBER", unique = true)
    private String walletNumber;

    @JsonIgnore
    @Column(name = "CATEGORY")
    private String tierCategory;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    //private Instant createdDate;
    //@Temporal(javax.persistence.TemporalType.TIMESTAMP)
    //@Column(name = "TRANS_TIME")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

}
