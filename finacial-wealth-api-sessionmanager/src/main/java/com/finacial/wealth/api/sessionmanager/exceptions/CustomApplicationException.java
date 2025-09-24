package com.finacial.wealth.api.sessionmanager.exceptions;

import org.springframework.http.HttpStatus;

public class CustomApplicationException extends RuntimeException {

    private HttpStatus httpStatus;
    private String message;

    public CustomApplicationException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
