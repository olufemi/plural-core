package com.finacial.wealth.api.sessionmanager.services;

import java.io.IOException;

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

    @Autowired
    private RedisTemplate< String, Object> redisTemplate;

    @Value("${fin.wealth.jwt.secret-key}")
    private String secretKey;

    @Value("${fin.wealth.redis.enable.jwt.black-list}")
    private boolean isJwtBlackListitingEnabled;

    private Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

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
            Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey)).parseClaimsJws(jwt).getBody();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
