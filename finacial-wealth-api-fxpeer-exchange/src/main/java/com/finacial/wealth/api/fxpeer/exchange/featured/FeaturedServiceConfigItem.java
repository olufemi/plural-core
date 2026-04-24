package com.finacial.wealth.api.fxpeer.exchange.featured;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class FeaturedServiceConfigItem {

    @NotBlank
    private String featureKey;

    @NotNull
    private FeatureGroup featureGroup;

    @NotNull
    private FeatureStrategy strategy;

    private FeatureStrategy fallbackStrategy;
    private Boolean enabled = Boolean.TRUE;
    private Integer priority = 100;
    private String manualTargetId;
    private String titleOverride;
    private String subtitleOverride;
    private String badge = "FEATURED";
    private String ctaLabel;
    private String targetScreen;
    private Map<String, Object> filters;
}
