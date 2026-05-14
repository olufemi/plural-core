package com.finacial.wealth.api.profiling.market.entities;

import com.finacial.wealth.api.profiling.domain.AbstractAuditingEntity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "CUSTOMER_MARKET_PROFILE")
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerMarketProfile extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "CustomerMarketProfile_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "100"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @Column(name = "ID")
    private Long id;

    @Column(nullable = false)
    private String customerId;

    private String emailAddress;

    @Column(nullable = false)
    private String marketCode;

    private String countryCode;
    private String currencyCode;
    private String status;
    private String kycStatus;
    private String accountProvisionStatus;
    private String walletProvisionStatus;
    private String externalProviderReference;
    private String smartCoreCustomerId;
    private String smartCoreAccountId;
    private String walletId;
    private String localAccountNumber;
    private String virtualAccountNumber;
    private Boolean primaryForCustomer;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;
}
