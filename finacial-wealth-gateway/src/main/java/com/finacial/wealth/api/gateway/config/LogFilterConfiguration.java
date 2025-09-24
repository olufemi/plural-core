package com.finacial.wealth.api.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "finacialwealth.filter.log")
public class LogFilterConfiguration {

    private Boolean logRequest = true;
    private Boolean logResponse = true;
    private Boolean logRequestBody = true;
    private Boolean logResponseBody = true;
    private Boolean logRequestHeaders = true;

    public Boolean getLogRequest() {
        return logRequest;
    }

    public void setLogRequest(Boolean logRequest) {
        this.logRequest = logRequest;
    }

    public Boolean getLogResponse() {
        return logResponse;
    }

    public void setLogResponse(Boolean logResponse) {
        this.logResponse = logResponse;
    }

    public Boolean getLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(Boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public Boolean getLogResponseBody() {
        return logResponseBody;
    }

    public void setLogResponseBody(Boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
    }

    public Boolean getLogRequestHeaders() {
        return logRequestHeaders;
    }

    public void setLogRequestHeaders(Boolean logRequestHeaders) {
        this.logRequestHeaders = logRequestHeaders;
    }
}
