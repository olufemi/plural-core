package com.finacial.wealth.api.gateway.helpers;

import com.netflix.zuul.context.RequestContext;
// import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class FilterHelper {

    private static final Logger logger = LoggerFactory.getLogger(FilterHelper.class);

    /**
     * this will compile a list of patterns
     * from the supplied string patterns.
     * The returned Pattern object will be used to
     * determine if a filter should run or not
     * @param rawWhiteList
     * @return list of pattern
     */
    public static List<Pattern> compileWhiteListPattern(List<String> rawWhiteList) {
        List<Pattern> whiteList = new ArrayList<>();

        if(Objects.isNull(rawWhiteList) || rawWhiteList.isEmpty()) return whiteList;

        rawWhiteList.parallelStream().forEach(wl -> {

            //replace the /* with .*
            wl = wl.replaceAll("\\*", ".*");

            //build pattern
            wl = "^" + wl + "$";

            whiteList.add(Pattern.compile(wl));
        });

        return whiteList;
    }

    public static Object zuulErrorResponse(RequestContext ctx, String responseBody, HttpStatus status) {
        HttpServletResponse response = ctx.getResponse();
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(status.value());
        ctx.setResponseBody(responseBody);
        ctx.setResponse(response);
        return null;
    }

    // public static Object zuulErrorResponse(RequestContext ctx, String responseBody, HttpStatus status, ConsumptionProbe probe) {
    //     HttpServletResponse response = ctx.getResponse();
    //     response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    //     response.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
    //     ctx.setSendZuulResponse(false);
    //     ctx.setResponseStatusCode(status.value());
    //     ctx.setResponseBody(responseBody);
    //     ctx.setResponse(response);
    //     return null;
    // }

}
