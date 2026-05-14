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
@Table(name = "MARKET_DEFINITION")
@Data
@EqualsAndHashCode(callSuper = true)
public class MarketDefinition extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "MarketDefinition_SEQ";

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

    @Column(name = "MARKET_CODE", unique = true, nullable = false)
    private String marketCode;

    @Column(name = "COUNTRY_CODE", nullable = false)
    private String countryCode;

    @Column(name = "DEFAULT_CURRENCY_CODE", nullable = false)
    private String defaultCurrencyCode;

    private String displayName;
    private String onboardingType;
    private String kycProviderType;
    private String accountProviderType;
    private String walletProvisioningType;
    private String smartCoreProfileType;
    private Boolean requiresPrimaryOnboarding;
    private Boolean requiresBvn;
    private Boolean requiresSdkCompletion;
    private Boolean supportsFxPeer;
    private Boolean supportsInvestment;
    private Boolean supportsAirtime;
    private Boolean supportsFunding;
    private Boolean supportsWithdrawal;
    private Boolean enabled;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;
}
