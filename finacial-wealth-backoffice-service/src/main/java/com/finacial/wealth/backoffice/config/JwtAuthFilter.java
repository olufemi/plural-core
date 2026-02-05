package com.finacial.wealth.backoffice.config;

import com.finacial.wealth.backoffice.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;

import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.out.println("JWT_FILTER uri=" + request.getRequestURI());
        System.out.println("JWT_FILTER auth=" + request.getHeader("Authorization"));

        System.out.println("URI=" + request.getRequestURI()
                + " servletPath=" + request.getServletPath()
                + " contextPath=" + request.getContextPath());
        System.out.println("JWT_FILTER uri=" + request.getRequestURI());
        System.out.println("JWT_FILTER auth=" + request.getHeader("Authorization"));

        /*String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //String token = auth.substring(7).trim();
        String token = auth.substring("Bearer ".length()).trim();*/
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null) {
            filterChain.doFilter(request, response);
            return;
        }

        auth = auth.trim();

// Case-insensitive "Bearer "
        if (!auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7).trim();

// Defensive: strip anything appended after the JWT (comma/space)
        int comma = token.indexOf(',');
        if (comma > 0) {
            token = token.substring(0, comma).trim();
        }

        int space = token.indexOf(' ');
        if (space > 0) {
            token = token.substring(0, space).trim();
        }

        if (token.isEmpty() || token.contains("\"") || token.contains("\n")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parse(token);

            // optional: accept only ACCESS tokens
            String typ = claims.get("typ", String.class);
            if (typ != null && !"ACCESS".equals(typ)) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = Long.valueOf(claims.getSubject());
            request.setAttribute("boAdminUserId", userId); // ✅ your controller needs this

            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles", List.class);

            List<GrantedAuthority> authorities = (roles == null)
                    ? Collections.<GrantedAuthority>emptyList()
                    : roles.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            Authentication authentication
                    = new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("JWT parse failed: uri={} msg={}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Swagger/OpenAPI public
        if (path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.equals("/v3/api-docs.yaml")
                || path.startsWith("/bo/swagger-ui")
                || path.startsWith("/bo/v3/api-docs")) {
            return true;
        }

        // Only public auth endpoints skip JWT
        if ("POST".equalsIgnoreCase(method)) {
            return path.equals("/auth/login")
                    || path.equals("/auth/mfa/verify")
                    || path.equals("/auth/refresh")
                    || path.equals("/auth/logout")
                    || path.equals("/bo/auth/login")
                    || path.equals("/bo/auth/mfa/verify")
                    || path.equals("/bo/auth/refresh")
                    || path.equals("/bo/auth/logout");
        }

        return false;
    }

}
