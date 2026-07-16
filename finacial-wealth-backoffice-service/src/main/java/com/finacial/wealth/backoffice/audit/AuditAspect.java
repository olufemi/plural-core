package com.finacial.wealth.backoffice.audit;

import com.finacial.wealth.backoffice.audit.entity.BoAuditLog;
import com.finacial.wealth.backoffice.audit.repo.BoAuditLogRepository;
import com.finacial.wealth.backoffice.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class AuditAspect {

  private final BoAuditLogRepository repo;
  private final HttpServletRequest request;

  @Around("@annotation(audited)")
  public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
    Long adminUserId = null;
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() != null) {
      try { adminUserId = Long.valueOf(String.valueOf(auth.getPrincipal())); } catch (Exception ignored) {}
    }

    BoAuditLog log = BoAuditLog.builder()
        .adminUserId(adminUserId)
        .action(audited.action())
        .entityType(audited.entityType())
        .entityId(audited.entityId())
        .requestId(request.getHeader("X-Request-Id"))
        .requestMethod(request.getMethod())
        .requestUri(request.getRequestURI())
        .ip(clientIp())
        .userAgent(request.getHeader("User-Agent"))
        .reason(request.getHeader("X-Reason"))
        .beforeJson(null)
        .build();

    try {
      Object result = pjp.proceed();
      log.setOutcome("SUCCESS");
      log.setAfterJson(JsonUtil.safeToJson(result));
      repo.save(log);
      return result;
    } catch (Throwable ex) {
      log.setOutcome("FAILED");
      log.setErrorMessage(ex.getClass().getSimpleName());
      repo.save(log);
      throw ex;
    }
  }

  private String clientIp() {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }

  public @interface Audited {
    String action();
    String entityType() default "";
    String entityId() default "";
  }
}
