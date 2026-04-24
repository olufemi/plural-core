package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import java.util.Map;
import lombok.Data;

@Data
public class FeaturedServiceConfigItem {

    private String featureKey;
    private FeatureGroup featureGroup;
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
