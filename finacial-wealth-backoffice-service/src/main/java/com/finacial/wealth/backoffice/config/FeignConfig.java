package com.finacial.wealth.backoffice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

  @Bean
  public RequestInterceptor backofficeFeignInterceptor(
      @Value("${bo.downstream.auth-header:Authorization}") String authHeader,
      @Value("${bo.downstream.auth-token:}") String authToken
  ) {
    return template -> {

      // pull current request (if any)
      HttpServletRequest req = currentRequest();

      if (req != null) {
        String rid = req.getHeader("X-Request-Id");
        if (rid != null && !rid.trim().isEmpty()) template.header("X-Request-Id", rid);

        String reason = req.getHeader("X-Reason");
        if (reason != null && !reason.trim().isEmpty()) template.header("X-Reason", reason);
      }

      // service-to-service token
      if (authToken != null && !authToken.trim().isEmpty()) {
        template.header(authHeader, authToken);
      }
    };
  }

  private HttpServletRequest currentRequest() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes sra) {
      return sra.getRequest();
    }
    return null;
  }
}
