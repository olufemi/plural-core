package com.finacial.wealth.backoffice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class BackofficeRequestIdFilter implements Filter {
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    String requestId = r.getHeader("X-Request-Id");
    if (requestId == null || requestId.trim().isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }
    w.setHeader("X-Request-Id", requestId);
    chain.doFilter(req, res);
  }
}
