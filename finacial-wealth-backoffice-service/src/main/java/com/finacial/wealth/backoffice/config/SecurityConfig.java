package com.finacial.wealth.backoffice.config;

import jakarta.servlet.http.HttpServletResponse;
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
        .authorizeHttpRequests(auth -> auth
            // public
            .requestMatchers(HttpMethod.POST,
                "/auth/login", "/auth/mfa/verify", "/auth/refresh", "/auth/logout",
                "/bo/auth/login", "/bo/auth/mfa/verify", "/bo/auth/refresh", "/bo/auth/logout"
            ).permitAll()

            // protected
            .requestMatchers(HttpMethod.POST,
                "/auth/mfa/setup", "/auth/mfa/confirm",
                "/bo/auth/mfa/setup", "/bo/auth/mfa/confirm"
            ).authenticated()

            .anyRequest().authenticated()
        )
        // ✅ IMPORTANT: JWT filter must be here too
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
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write("{\"status\":401,\"message\":\"Unauthorized\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write("{\"status\":403,\"message\":\"Forbidden\"}");
                })
                )
                .authorizeHttpRequests(auth -> auth
                // Swagger/OpenAPI public
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
                // JWT filter only for protected endpoints
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
