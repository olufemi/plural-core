package com.finacial.wealth.backoffice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
    r.setAttribute("requestId", requestId);
    w.setHeader("X-Request-Id", requestId);

    // Keep this deliberately non-sensitive: it helps trace gateway/body/header issues
    // without writing tokens or credentials to logs.
    String auth = r.getHeader("Authorization");
    boolean hasBearer = auth != null && auth.trim().regionMatches(true, 0, "Bearer ", 0, 7);
    System.out.println("BackofficeRequestIdFilter :: " + r.getMethod() + " " + r.getRequestURI()
        + " | requestId=" + requestId
        + " | contentType=" + nullSafe(r.getContentType())
        + " | contentLength=" + r.getContentLengthLong()
        + " | AuthorizationPresent=" + (auth != null)
        + " | Bearer=" + hasBearer
        + " | xForwardedFor=" + nullSafe(r.getHeader("X-Forwarded-For"))
        + " | xForwardedProto=" + nullSafe(r.getHeader("X-Forwarded-Proto"))
        + " | userAgent=" + nullSafe(r.getHeader("User-Agent")));

    try {
      chain.doFilter(req, res);
    } catch (IOException | ServletException | RuntimeException ex) {
      logAbnormalRequest(r, w, requestId, auth, hasBearer, ex);
      throw ex;
    } finally {
      if (w.getStatus() >= 400) {
        logAbnormalRequest(r, w, requestId, auth, hasBearer, null);
      }
    }
  }

  private String nullSafe(String value) {
    return value == null || value.trim().isEmpty() ? "-" : value;
  }

  private void logAbnormalRequest(HttpServletRequest r, HttpServletResponse w, String requestId,
      String auth, boolean hasBearer, Throwable ex) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String principal = authentication == null ? "-" : String.valueOf(authentication.getName());
    String authorities = authentication == null ? "-" : String.valueOf(authentication.getAuthorities());
    String exceptionName = ex == null ? "-" : ex.getClass().getSimpleName();
    String exceptionMessage = ex == null ? "-" : sanitize(ex.getMessage());

    System.out.println("BackofficeAbnormalRequest :: " + r.getMethod() + " " + r.getRequestURI()
        + " | requestId=" + requestId
        + " | status=" + w.getStatus()
        + " | principal=" + principal
        + " | authorities=" + authorities
        + " | contentType=" + nullSafe(r.getContentType())
        + " | contentLength=" + r.getContentLengthLong()
        + " | AuthorizationPresent=" + (auth != null)
        + " | Bearer=" + hasBearer
        + " | xForwardedFor=" + nullSafe(r.getHeader("X-Forwarded-For"))
        + " | xForwardedProto=" + nullSafe(r.getHeader("X-Forwarded-Proto"))
        + " | userAgent=" + nullSafe(r.getHeader("User-Agent"))
        + " | exception=" + exceptionName
        + " | message=" + exceptionMessage);
  }

  private String sanitize(String message) {
    if (message == null || message.trim().isEmpty()) {
      return "-";
    }
    return message.replaceAll("[\\r\\n\\t]+", " ");
  }
}
