package com.finacial.wealth.api.fxpeer.exchange.featured;

import lombok.Data;

@Data
public class FeaturedServiceCard {

    private String featureKey;
    private FeatureGroup featureGroup;
    private FeatureType featureType;
    private FeatureStrategy strategy;
    private String title;
    private String subtitle;
    private String badge;
    private String ctaLabel;
    private FeaturedServiceTarget target;
    private Object payload;
}
