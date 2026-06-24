package com.finacial.wealth.api.sessionmanager.services;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.api.sessionmanager.Constants.Constants;
import com.finacial.wealth.api.sessionmanager.response.BaseResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.servlet.FilterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(urlPatterns = "/session/verify")
public class AuthenticationFilter implements Filter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final String INVALID_AUTH_TOKEN = "Your Session Has Expired";
    private static final String ISSUER = "FELLOWPAY";
    private static final String SUBJECT = "Authentication";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String ISSUED_SESSION_KEY_PREFIX = "session-manager:issued-session:";
    private static final String DEFAULT_JWT_SECRET = "UdNi6IuGj+Y6jcabuyfdH4nqdNoOgcmUJPE4eAhfy0g=";

    @Autowired
    private RedisTemplate< String, Object> redisTemplate;

    @Value("${fin.wealth.jwt.secret-key}")
    private String secretKey;

    @Value("${fin.wealth.redis.enable.jwt.black-list}")
    private boolean isJwtBlackListitingEnabled;

    @Value("${fin.wealth.jwt.issued-session-validation:true}")
    private boolean issuedSessionValidationEnabled;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @PostConstruct
    public void validateJwtSecret() {
        if (DEFAULT_JWT_SECRET.equals(secretKey) && isProductionProfile()) {
            throw new IllegalStateException("SESSION_MANAGER_JWT_KEY must be configured in production");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        filter(request, response);
    }

    public void filter(ServletRequest request, ServletResponse response) throws IOException {

        // Get the Authorization header from the request
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null) {

            // Validate the Authorization header
            System.out.println("isTokenBasedAuthentication(authorizationHeader)" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + isTokenBasedAuthentication(authorizationHeader));

            if (!isTokenBasedAuthentication(authorizationHeader)) {

                setResponseBody(response, INVALID_AUTH_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);

            } else {

                // Extract the token from the Authorization header
                String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
                System.out.println("validate token" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + validateToken(token));

                if (validateToken(token)) {
                    setResponseBody(response, Constants.AUTHENTICATION_VALID, HttpServletResponse.SC_OK);

                } else {
                    setResponseBody(response, INVALID_AUTH_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);
                }
            }

        } else {
            setResponseBody(response, INVALID_AUTH_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void setResponseBody(ServletResponse response, String description, int statusCode) throws JsonProcessingException, IOException {
        BaseResponse rep = new BaseResponse();
        rep.setDescription(description);
        rep.setStatusCode(statusCode);
        ((HttpServletResponse) response).addHeader("Content-Type", "application/json");

        response.getWriter().write(new ObjectMapper().writeValueAsString(rep));
    }

    private boolean validateToken(String jwt) {
        try {
            if (isJwtBlackListitingEnabled) {
                String token = (String) redisTemplate.opsForValue().get(jwt);
                if (token != null) {
                    return false;
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        try {
            Claims claims = Jwts.parser()
                    .requireIssuer(ISSUER)
                    .requireSubject(SUBJECT)
                    .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                    .parseClaimsJws(jwt)
                    .getBody();

            return validateIssuedSession(claims);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean validateIssuedSession(Claims claims) {
        if (!issuedSessionValidationEnabled) {
            return true;
        }

        String jti = claims.getId();
        if (jti == null) {
            Object jtiClaim = claims.get("jti");
            jti = jtiClaim == null ? null : jtiClaim.toString();
        }

        String sid = claimAsString(claims, "sid");
        String tokenType = claimAsString(claims, "token_type");
        String userId = claimAsString(claims, "userId");
        String customerId = claimAsString(claims, "customerId");
        String deviceId = claimAsString(claims, "deviceId");
        String uuid = claimAsString(claims, "uuid");

        if (isBlank(jti) || isBlank(sid) || isBlank(tokenType) || isBlank(userId)) {
            return false;
        }

        if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
            return false;
        }

        Object stored = redisTemplate.opsForValue().get(ISSUED_SESSION_KEY_PREFIX + jti);
        if (!(stored instanceof Map)) {
            return false;
        }

        Map session = (Map) stored;
        if (!"ACTIVE".equals(valueAsString(session.get("status")))) {
            return false;
        }

        if (!jti.equals(valueAsString(session.get("jti")))
                || !sid.equals(valueAsString(session.get("sid")))
                || !userId.equals(valueAsString(session.get("userId")))) {
            return false;
        }

        if (!safeEquals(customerId, valueAsString(session.get("customerId")))
                || !safeEquals(deviceId, valueAsString(session.get("deviceId")))
                || !safeEquals(uuid, valueAsString(session.get("uuid")))) {
            return false;
        }

        Long expiresAt = valueAsLong(session.get("expiresAt"));
        Date expiration = claims.getExpiration();
        if (expiresAt == null || expiration == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        return expiresAt > now && expiration.getTime() > now;
    }

    private String claimAsString(Claims claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : value.toString();
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private Long valueAsLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.valueOf(value.toString());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isProductionProfile() {
        if (activeProfile == null) {
            return false;
        }

        String[] profiles = activeProfile.split(",");
        for (String profile : profiles) {
            String normalized = profile == null ? "" : profile.trim().toLowerCase();
            if ("prod".equals(normalized) || "production".equals(normalized)) {
                return true;
            }
        }
        return false;
    }

}
