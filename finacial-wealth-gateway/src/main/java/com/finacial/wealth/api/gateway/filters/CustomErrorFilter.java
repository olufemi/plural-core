package com.finacial.wealth.api.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import io.vavr.control.Try;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomErrorFilter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomErrorFilter.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1; // Needs to run before SendErrorFilter which has filterOrder == 0
    }

    @Override
    public boolean shouldFilter() {
        // only forward to errorPath if it hasn't been forwarded to already
        System.out.println("RequestContext.getCurrentContext() " +RequestContext.getCurrentContext().getRouteHost());
        
        if (RequestContext.getCurrentContext().getRouteHost() ==  null)
             System.out.println("RequestContext.getCurrentContext() is null" +RequestContext.getCurrentContext().getFilterExecutionSummary());


        return RequestContext.getCurrentContext().containsKey("throwable");
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            Object e = ctx.get("throwable");
            if (e != null && e instanceof ZuulException) {
                ZuulException zuulException = (ZuulException)e;
                LOG.error("Zuul failure detected: " + zuulException.getMessage(), zuulException);

                // Remove error code to prevent further error handling in follow up filters
                //and return status of code of 200 to prevent the frontend from displaying the exception
                ctx.remove("throwable");

                Map<String, Object> response = new HashMap<>();
                response.put("data", Collections.emptyMap());
                response.put("description", "Unable to conclude transaction at the moment. Please try agian later");
                response.put("statusCode", RequestContext.getCurrentContext().getRouteHost()==null ? 400: 200);
                String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
                        .onFailure(System.out::println)
                        .getOrNull();

                // Populate context with new response values
                ctx.setResponseBody(responseStr);
                ctx.getResponse().setContentType("application/json");
                ctx.setResponseStatusCode(200); //Can set any error code as excepted
            }
        }
        catch (Exception ex) {
            LOG.error("Exception filtering in custom error filter", ex);
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }
}
