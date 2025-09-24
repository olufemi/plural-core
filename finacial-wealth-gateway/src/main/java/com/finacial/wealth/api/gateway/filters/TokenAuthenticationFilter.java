package com.finacial.wealth.api.gateway.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.finacial.wealth.api.gateway.helpers.FilterHelper.compileWhiteListPattern;
import static com.finacial.wealth.api.gateway.helpers.FilterHelper.zuulErrorResponse;
import com.finacial.wealth.api.gateway.clients.SessionManagerClient;
import com.finacial.wealth.api.gateway.config.TokenAuthFilterConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TokenAuthenticationFilter extends ZuulFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Pattern> whiteList;
    private final TokenAuthFilterConfig config;
    private final SessionManagerClient sessionManagerClient;
    private final ObjectMapper objectMapper;

    @Value("${zuul.prefix:/api}")
    private String apiPrefix;

    @Autowired
    public TokenAuthenticationFilter(TokenAuthFilterConfig config, SessionManagerClient sessionManagerClient,
            ObjectMapper objectMapper) {
        this.config = config;
        this.sessionManagerClient = sessionManagerClient;
        this.objectMapper = objectMapper;
        whiteList = compileWhiteListPattern(config.getWhiteList());
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     *
     * @return true of the context URI is not white listed and false otherwise
     */
    @Override
    public boolean shouldFilter() {
        System.out.println("the raw u::::::::::::::::   " + RequestContext.getCurrentContext().getRequest().getRequestURI());
        String uri = RequestContext.getCurrentContext().getRequest().getRequestURI().replaceFirst(apiPrefix, "");
        System.out.println("uri ::::::::::::::::   " + uri);
        /* 
        boolean toReturn = true;
       // System.out.println("::::::::::::::::   " + whiteList.parallelStream().noneMatch(wl -> wl.matcher(uri).matches()));
        for (Pattern urll : whiteList) {
            if (urll.matcher(uri).matches()) {
                System.out.println("::::::::::::::::   " + urll.matcher(uri).matches());
                toReturn = false;
                return toReturn;
            }

        }*/

        //run the uri against the whiteListed paths
        //if the uri is whiteListed, it will return false otherwise it will return true
        System.out.println("::::::::::::::::   " + whiteList.parallelStream().noneMatch(wl -> wl.matcher(uri).matches()));

        return whiteList.parallelStream().noneMatch(wl -> wl.matcher(uri).matches());
        // return false;
        //return toReturn;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext requestContext = RequestContext.getCurrentContext();
        String authorizationHeader = requestContext.getRequest().getHeader("Authorization");
        System.out.println("authorizationHeader ::::::::::::::::   " + authorizationHeader);

        if (requestContext.getRequest().getRequestURI().contains("notification-manager")) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.emptyMap());
            response.put("statusCode", HttpStatus.BAD_REQUEST.value());
            response.put("description", "Permission Failed");
            String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
                    .onFailure(System.out::println)
                    .getOrNull();
            return zuulErrorResponse(requestContext, responseStr, HttpStatus.OK);
        }

        if (StringUtils.isEmpty(authorizationHeader)) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.emptyMap());
            response.put("statusCode", HttpStatus.BAD_REQUEST.value());
            response.put("description", "Invalid Session");
            String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
                    .onFailure(System.out::println)
                    .getOrNull();
            return zuulErrorResponse(requestContext, responseStr, HttpStatus.OK);
        }

        Either<Throwable, Map<String, Object>> sessionManagerResponse = Try.of(() -> sessionManagerClient.verifyBearerToken(authorizationHeader)).toEither();
         System.out.println("sessionManagerResponse ::::::::::::::::   " + sessionManagerResponse);

        if (sessionManagerResponse.isLeft()) {
            Throwable exception = sessionManagerResponse.getLeft();
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.emptyMap());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("description", "Unable to validate authorization token now");
            String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
                    .onFailure(System.out::println)
                    .getOrNull();
            return zuulErrorResponse(requestContext, responseStr, HttpStatus.OK);
        }

        Map<String, Object> responseObject = sessionManagerResponse.get();
        String statusCode = responseObject.getOrDefault("statusCode", "").toString();
        if (!StringUtils.equals(statusCode, String.valueOf(HttpStatus.OK.value()))) {
            String responseStr = Try.of(() -> objectMapper.writeValueAsString(responseObject))
                    .onFailure(System.out::println)
                    .getOrNull();
            return zuulErrorResponse(requestContext, responseStr, HttpStatus.OK);
        }
        return null;

    }

}
