package com.finacial.wealth.api.gateway.filters;

// import static com.accessbank.nextgen.service.gateway.helpers.FilterHelper.zuulErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
// import io.github.bucket4j.*;
// import io.github.bucket4j.grid.ProxyManager;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter  {

    // private RateLimitFilterConfig rateLimitFilterConfig;
    // private ObjectMapper objectMapper;
    // private Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    // private static final String BUCKET_STORAGE_PREFIX = "throttle-gateway-";
    // private ProxyManager<String> buckets;

    // @Autowired
    // public RateLimitFilter(RateLimitFilterConfig rateLimitFilterConfig, ObjectMapper objectMapper,
    //         ProxyManager<String> buckets) {
    //     this.rateLimitFilterConfig = rateLimitFilterConfig;
    //     this.objectMapper = objectMapper;
    //     this.buckets = buckets;
    // }

    // @Override
    // public String filterType() {
    //     return PRE_TYPE;
    // }

    // @Override
    // public int filterOrder() {
    //     return 0;
    // }

    // @Override
    // public boolean shouldFilter() {
    //     return !rateLimitFilterConfig.getDisabled();
    // }

    // @Override
    // public Object run() throws ZuulException {

    //     RequestContext requestContext = RequestContext.getCurrentContext();
    //     HttpServletRequest httpRequest = requestContext.getRequest();

    //     String appKey = httpRequest.getRemoteAddr().concat(httpRequest.getRequestURI());

    //     Bucket bucket = getOrCreateNewBucket(appKey);

    //     // tryConsume returns false immediately if no tokens available with the bucket
    //     ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    //     if (!probe.isConsumed()) {
    //         // the limit has been exceeded
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("data", Collections.emptyMap());
    //         response.put("statusCode", HttpStatus.TOO_MANY_REQUESTS.value());
    //         response.put("description", "Too many requests");
    //         String responseStr = Try.of(() -> objectMapper.writeValueAsString(response))
    //                 .onFailure(System.out::println)
    //                 .getOrNull();
    //         return zuulErrorResponse(requestContext, responseStr, HttpStatus.OK, probe);
    //     }
    //     return null;
    // }

    // private Bucket getOrCreateNewBucket(String key) {
    //     Refill refill = Refill.greedy(rateLimitFilterConfig.getLimit(), Duration.ofSeconds(rateLimitFilterConfig.getTimeInSeconds()));
    //     Bandwidth limit = Bandwidth.classic(rateLimitFilterConfig.getQuota(), refill);
    //     BucketConfiguration configuration = Bucket4j.configurationBuilder()
    //             .addLimit(limit).build();
    //     return buckets.getProxy(key, configuration);
    // }
}
