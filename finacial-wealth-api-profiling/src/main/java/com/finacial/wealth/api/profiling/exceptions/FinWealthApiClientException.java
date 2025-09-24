package com.finacial.wealth.api.profiling.exceptions;

public class FinWealthApiClientException extends RuntimeException {

    private int httpStatus;
    private String message;

    public FinWealthApiClientException(int httpStatus, String message) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
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
