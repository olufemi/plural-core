/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.config;

/**
 *
 * @author olufemioshin
 */
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${backoffice.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${backoffice.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${backoffice.cors.allowed-headers:Authorization,Content-Type}")
    private String allowedHeaders;

    @Value("${backoffice.cors.exposed-headers:X-Request-Id}")
    private String exposedHeaders;

    @Value("${backoffice.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // IMPORTANT: if allowCredentials=true, you must NOT use "*"
        List<String> origins = Arrays.asList(allowedOrigins.split("\\s*,\\s*"));
        cfg.setAllowedOrigins(origins);

        cfg.setAllowedMethods(Arrays.asList(allowedMethods.split("\\s*,\\s*")));
        cfg.setAllowedHeaders(Arrays.asList(allowedHeaders.split("\\s*,\\s*")));
        cfg.setExposedHeaders(Arrays.asList(exposedHeaders.split("\\s*,\\s*")));

        cfg.setAllowCredentials(allowCredentials);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
