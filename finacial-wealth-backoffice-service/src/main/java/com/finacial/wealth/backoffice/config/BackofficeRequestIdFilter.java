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
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;

    String requestId = r.getHeader("X-Request-Id");
    if (requestId == null || requestId.trim().isEmpty()) {
      requestId = java.util.UUID.randomUUID().toString();
    }
    w.setHeader("X-Request-Id", requestId);

    // --- DEBUG: check Authorization header presence ---
    String auth = r.getHeader("Authorization");
    boolean hasBearer = auth != null && auth.startsWith("Bearer ");
    System.out.println("BackofficeRequestIdFilter :: " + r.getMethod() + " " + r.getRequestURI()
        + " | requestId=" + requestId
        + " | AuthorizationPresent=" + (auth != null)
        + " | Bearer=" + hasBearer);
    // --------------------------------------------------

    chain.doFilter(req, res);
  }
}

