package com.finacial.wealth.api.gateway.exceptions;

import com.finacial.wealth.api.gateway.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 *
 * this is a controller for
 * handling errors on the
 * server
 *
 */
@Controller
public class GlobalExceptionHandler implements ErrorController {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${error.path:/error}")
    private String errorPath;

    public GlobalExceptionHandler() { }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<BaseResponse> handleInternalServerError(Exception e) {
        
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription("Something went wrong internally. Please try again");
        baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse> handleError(MissingRequestHeaderException e) {
        
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription(e.getMessage());
        baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<BaseResponse> handleError(ServletRequestBindingException e) {
        
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription("Bad request");
        baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleError(MethodArgumentNotValidException e) {
        String  errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage()+",")
                .reduce("", String::concat);

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription(errors);
        baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse> handleError(HttpRequestMethodNotSupportedException e) {
        
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription(e.getMessage());
        baseResponse.setStatusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse> handleError(MissingServletRequestParameterException e) {
        
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription(e.getMessage());
        baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleError(HttpMessageNotReadableException e) {
        String errorMessage = e.getMessage();
        String[] split = errorMessage != null ? errorMessage.split(":") : null;
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription((split == null || split.length == 0) ? errorMessage : split[0]);
        baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @Override
    public String getErrorPath() {
        return errorPath;
    }

    @RequestMapping(value = "${error.path:/error}", produces = "application/vnd.error+json")
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        int statusCode = getErrorStatus(request);
        response.put("data", Collections.emptyMap());
        response.put("statusCode", statusCode);
        response.put("description", getErrorMessage(request, statusCode));
        return ResponseEntity.ok(response);
    }

    private int getErrorStatus(HttpServletRequest request) {
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        return statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String getErrorMessage(HttpServletRequest request, int statusCode) {

        if(statusCode == HttpStatus.NOT_FOUND.value())
            return "Requested resource/service not found";
        if(statusCode == HttpStatus.UNAUTHORIZED.value())
            return "Unauthorized! Access denied";
        if(statusCode == HttpStatus.GATEWAY_TIMEOUT.value())
            return "Gateway request timeout";

        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        
        return throwable != null ? throwable.getMessage() : "Something went wrong internally";
    }

}
