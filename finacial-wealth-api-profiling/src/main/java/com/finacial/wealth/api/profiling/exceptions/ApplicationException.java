package com.finacial.wealth.api.profiling.exceptions;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApplicationException() {
        super();
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(int httpStatusCode, String reason) {
        super(String.format("StatusCode: %s, message: %s", httpStatusCode, reason));
    }
}
