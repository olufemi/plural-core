package com.finacial.wealth.api.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class GatewayFallbackConfiguration implements FallbackProvider  {

    private Logger logger = LoggerFactory.getLogger(GatewayFallbackConfiguration.class);
    private ObjectMapper objectMapper;

    public GatewayFallbackConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        logger.error("handling hystrix fallback for service [ {} ]. Cause {}", route, (!Objects.isNull(cause) ? cause.getLocalizedMessage() : "is null"));
        HttpStatus httpStatus = (cause instanceof HystrixTimeoutException) ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE;

        Map<String, Object> response = new HashMap<>();
        response.put("data", Collections.emptyMap());
        response.put("statusCode", httpStatus.value());
        response.put("description", "Apologies this service is currently unavailable. Kindly try again later.");
        byte [] message = Try.of(() -> objectMapper.writeValueAsBytes(response)).getOrElse("Service is not available".getBytes());
        return new GatewayClientHttpResponse(HttpStatus.OK, message); //we're using HttpStatus.OK so we adhere to the requirement of always 200
    }



}
