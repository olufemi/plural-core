package com.finacial.wealth.api.gateway.filters;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.finacial.wealth.api.gateway.config.LogFilterConfiguration;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class RequestLogFilter extends ZuulFilter {

    private LogFilterConfiguration logFilterConfiguration;
    private Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Autowired
    public RequestLogFilter(LogFilterConfiguration logFilterConfiguration) {
        this.logFilterConfiguration = logFilterConfiguration;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return logFilterConfiguration.getLogRequest();
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        System.out.println("REQUEST URI: " +request.getRequestURI());
		requestContext.addZuulRequestHeader("correlation-id", UUID.randomUUID().toString());

        return null;
    }
}
