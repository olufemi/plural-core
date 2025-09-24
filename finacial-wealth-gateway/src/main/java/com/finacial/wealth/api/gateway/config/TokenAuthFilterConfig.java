package com.finacial.wealth.api.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "finacialwealth.filter.token")
public class TokenAuthFilterConfig {

    private Boolean disabled;
    private List<String> whiteList;

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }
}
