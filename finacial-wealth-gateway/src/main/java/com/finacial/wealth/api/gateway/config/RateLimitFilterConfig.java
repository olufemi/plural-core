package com.finacial.wealth.api.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "finacialwealth.filter.rate-limit")
public class RateLimitFilterConfig {

    private Boolean disabled;
    private Long quota;
    private Integer limit;
    private Integer timeInSeconds;
    private List<String> keys;

    public RateLimitFilterConfig() {}

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Long getQuota() {
        return quota;
    }

    public void setQuota(Long quota) {
        this.quota = quota;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(Integer timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "RateLimitFilterConfig{" +
                "disabled=" + disabled +
                ", quota=" + quota +
                ", limit=" + limit +
                ", timeInSeconds=" + timeInSeconds +
                ", keys=" + keys +
                '}';
    }
}
