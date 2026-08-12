package com.finacial.wealth.backoffice.error;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class BackofficeExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<BackofficeErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", safeMessage(ex), request, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<BackofficeErrorResponse> handleConflict(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "INVALID_STATE", safeMessage(ex), request, null);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<BackofficeErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", validationMessage(ex), request, validationDetails(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<BackofficeErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Request body is missing or invalid JSON", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<BackofficeErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_ERROR", "Request conflicts with existing data or database constraints", request, null);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<BackofficeErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR",
                "A database error occurred. Please contact support with the requestId.", request, null);
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<BackofficeErrorResponse> handleFeign(FeignException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "DOWNSTREAM_ERROR",
                "Downstream service error. Please contact support with the requestId.", request,
                new DownstreamErrorDetail(ex.status()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<BackofficeErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String code = status.is4xxClientError() ? "REQUEST_ERROR" : "SERVER_ERROR";
        return build(status, code, safeReason(ex), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<BackofficeErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<BackofficeErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", safeMessage(ex), request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<BackofficeErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected backoffice error. Please contact support with the requestId.", request, null);
    }

    private ResponseEntity<BackofficeErrorResponse> build(HttpStatus status, String code, String message,
            HttpServletRequest request, Object details) {
        return ResponseEntity.status(status).body(new BackofficeErrorResponse(
                status.value(),
                code,
                message,
                requestId(request),
                request.getRequestURI(),
                Instant.now(),
                details
        ));
    }

    private String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute("requestId");
        if (requestId != null) {
            return String.valueOf(requestId);
        }
        String header = request.getHeader("X-Request-Id");
        return header == null || header.trim().isEmpty() ? "-" : header;
    }

    private String safeReason(ResponseStatusException ex) {
        String reason = ex.getReason();
        return reason == null || reason.trim().isEmpty() ? "Request failed" : reason;
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.trim().isEmpty() ? "Request failed" : message;
    }

    private String validationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().hasErrors()) {
            return manv.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        if (ex instanceof BindException bind && bind.getBindingResult().hasErrors()) {
            return bind.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        return safeMessage(ex);
    }

    private Object validationDetails(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            return manv.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                    .toList();
        }
        if (ex instanceof BindException bind) {
            return bind.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                    .toList();
        }
        if (ex instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                    .map(error -> new FieldErrorDetail(String.valueOf(error.getPropertyPath()), error.getMessage()))
                    .toList();
        }
        return List.of();
    }

    private record FieldErrorDetail(String field, String message) {
    }

    private record DownstreamErrorDetail(int downstreamStatus) {
    }
}
