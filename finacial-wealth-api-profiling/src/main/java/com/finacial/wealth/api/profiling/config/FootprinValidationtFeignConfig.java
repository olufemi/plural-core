/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author olufemioshin
 */
@Configuration
public class FootprinValidationtFeignConfig {

    @Bean
    public RequestInterceptor footprintInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Footprint-Secret-Key", "sk_test_VZ5s72rDgOJNUbD2O6bcPnE5SOuE75rLOYrst");
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
