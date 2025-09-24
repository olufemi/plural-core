package com.finacial.wealth.api.gateway.filters;

import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.finacial.wealth.api.gateway.config.LogFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;


@Component
public class ResponseLogFilter extends ZuulFilter {

    private LogFilterConfiguration logFilterConfiguration;
    private Logger logger = LoggerFactory.getLogger(ResponseLogFilter.class);

    public ResponseLogFilter(LogFilterConfiguration logFilterConfiguration) {
        this.logFilterConfiguration = logFilterConfiguration;
    }

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return logFilterConfiguration.getLogResponse();
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext context = RequestContext.getCurrentContext();
        
        HttpServletRequest request = context.getRequest();

		
		String [] allowedDomains = {"localhost:4000"};
		context.addZuulResponseHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		context.addZuulResponseHeader("Access-Control-Allow-Credentials", "true");
		context.addZuulResponseHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, HEAD, PATCH, PUT");
		context.addZuulResponseHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, Authorization");
		if (request.getMethod().equals("OPTIONS")) {
			context.setResponseStatusCode(200);
		}	
       
        try (final InputStream responseDataStream = context.getResponseDataStream()) {
            if(responseDataStream == null) {
                return null;
            }

            String responseData = CharStreams.toString(new InputStreamReader(responseDataStream, "UTF-8"));

            if(logFilterConfiguration.getLogResponseBody()) {
            }
            context.setResponseBody(responseData);
        }
        catch (Throwable e) {
        }
        return null;

    }
}
