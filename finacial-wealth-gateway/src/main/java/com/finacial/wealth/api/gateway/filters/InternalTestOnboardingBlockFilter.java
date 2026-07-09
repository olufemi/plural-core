package com.finacial.wealth.api.gateway.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.vavr.control.Try;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.finacial.wealth.api.gateway.helpers.FilterHelper.zuulErrorResponse;

@Component
public class InternalTestOnboardingBlockFilter extends ZuulFilter {

    private static final String BLOCKED_PATH = "/internal/test/onboarding";

    private final ObjectMapper objectMapper;

    @Value("${zuul.prefix:/api}")
    private String apiPrefix;

    public InternalTestOnboardingBlockFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -10;
    }

    @Override
    public boolean shouldFilter() {
        String uri = RequestContext.getCurrentContext().getRequest().getRequestURI();
        return isBlockedInternalTestOnboardingPath(uri);
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        Map<String, Object> response = new HashMap<>();
        response.put("data", Collections.emptyMap());
        response.put("statusCode", HttpStatus.NOT_FOUND.value());
        response.put("description", "Resource not found");
        String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
                .onFailure(System.out::println)
                .getOrNull();
        return zuulErrorResponse(requestContext, responseStr, HttpStatus.NOT_FOUND);
    }

    private boolean isBlockedInternalTestOnboardingPath(String uri) {
        if (uri == null) {
            return false;
        }
        String normalizedUri = stripApiPrefix(uri);
        return BLOCKED_PATH.equals(normalizedUri)
                || normalizedUri.startsWith(BLOCKED_PATH + "/")
                || normalizedUri.contains(BLOCKED_PATH + "/");
    }

    private String stripApiPrefix(String uri) {
        if (apiPrefix != null && !apiPrefix.isEmpty() && uri.startsWith(apiPrefix)) {
            return uri.substring(apiPrefix.length());
        }
        return uri;
    }
}
