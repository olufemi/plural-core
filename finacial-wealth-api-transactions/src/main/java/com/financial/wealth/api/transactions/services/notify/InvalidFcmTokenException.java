package com.financial.wealth.api.transactions.services.notify;

public class InvalidFcmTokenException extends RuntimeException {

    public InvalidFcmTokenException(String message) {
        super(message);
    }
}
