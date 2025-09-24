package com.finacial.wealth.api.utility.config;

import com.financial.wealth.api.utility.exceptions.ApplicationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                //.defaultHeaders(header -> header.setBasicAuth(coralPayConfig.getUsername(), coralPayConfig.getPassword()))
                .filter(this.logRequest())
                .filter(this.logResponse())
                .filter(this.errorHandler())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("WebClient request: {} {} {}", clientRequest.method(), clientRequest.url(), clientRequest.body());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("WebClient response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ApplicationException(errorBody)));
            } else if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ApplicationException(errorBody)));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }
}
