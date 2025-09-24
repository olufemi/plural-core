package com.finacial.wealth.api.sessionmanager.exceptions;

import com.finacial.wealth.api.sessionmanager.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * this is a controller for handling errors on the server
 *
 */
@RestControllerAdvice
@RestController
public class GlobalExceptionHandler implements ErrorController {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<BaseResponse> handleInternalServerError(Exception e) {

        BaseResponse baseResponse = new BaseResponse();
        e.printStackTrace();
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

    @ExceptionHandler(CustomApplicationException.class)
    public ResponseEntity<BaseResponse> handleError(CustomApplicationException e) {

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription(e.getMessage());
        baseResponse.setStatusCode(e.getHttpStatus().value());
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
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage() + ",")
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

    @RequestMapping("/error")
    public ResponseEntity<BaseResponse> handleError(HttpServletRequest request, HttpServletResponse response) {

        int statusCode = getStatusCode(request);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setDescription("The resource does not exist");
            baseResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }

        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setDescription("Permission denied");
            baseResponse.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }

        if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setDescription("Too many requests");
            baseResponse.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }

        if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setDescription("Service unavailable");
            baseResponse.setStatusCode(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        }

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setDescription("Something went wrong internally. Please try again");
        baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }

    private int getStatusCode(HttpServletRequest request) {

        String code = request.getParameter("code");
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int statusCode = 500;

        if (status != null) {
            statusCode = status;
        } else if (code != null && !code.isEmpty()) {
            statusCode = Integer.parseInt(code);
        }

        return statusCode;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
