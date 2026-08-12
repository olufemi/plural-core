package com.finacial.wealth.backoffice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    @Order(1)
    SecurityFilterChain authChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/auth/**", "/bo/auth/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> writeSecurityError(req, res,
                        HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized"))
                .accessDeniedHandler((req, res, e) -> writeSecurityError(req, res,
                        HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST,
                        "/auth/login", "/auth/mfa/verify", "/auth/refresh", "/auth/logout",
                        "/auth/password/recovery/start", "/auth/password/recovery/complete",
                        "/bo/auth/login", "/bo/auth/mfa/verify", "/bo/auth/refresh", "/bo/auth/logout",
                        "/bo/auth/password/recovery/start", "/bo/auth/password/recovery/complete"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/auth/mfa/setup", "/auth/mfa/confirm", "/auth/password/change",
                        "/bo/auth/mfa/setup", "/bo/auth/mfa/confirm", "/bo/auth/password/change"
                ).authenticated()
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> writeSecurityError(req, res,
                        HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized"))
                .accessDeniedHandler((req, res, e) -> writeSecurityError(req, res,
                        HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/bo/v3/api-docs/**",
                        "/bo/v3/api-docs.yaml",
                        "/bo/swagger-ui/**",
                        "/bo/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeSecurityError(HttpServletRequest req, HttpServletResponse res, int status, String code,
            String message) throws IOException {
        String requestId = requestId(req);
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status
                + ",\"code\":\"" + code
                + "\",\"message\":\"" + escape(message)
                + "\",\"requestId\":\"" + escape(requestId)
                + "\",\"path\":\"" + escape(req.getRequestURI()) + "\"}");
    }

    private String requestId(HttpServletRequest req) {
        Object attr = req.getAttribute("requestId");
        if (attr != null) {
            return String.valueOf(attr);
        }
        String header = req.getHeader("X-Request-Id");
        return header == null || header.trim().isEmpty() ? "-" : header;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
