package com.financial.wealth.api.transactions.exceptions;

import com.financial.wealth.api.transactions.models.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class ExceptionHelper {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);

    public static BaseResponse extractAccessApiClientException(Throwable e) {

        logger.error("Exception is: " + e.getClass().getSimpleName() + " || raw Exception message: \n" + e.getMessage(), e);

        if (e instanceof FinWealthApiClientException) {
            FinWealthApiClientException restException = (FinWealthApiClientException) e;
            //parse the message to the standard model
            return new BaseResponse(restException.getHttpStatus(), restException.getMessage());
        }

        //last fallback for all unknown exception
        return new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred, please try again");
    }

    public static BaseResponse extractAccessApiClientExceptionForClaneTransferRp(Throwable e) {

        logger.error("Exception is: " + e.getClass().getSimpleName() + " || raw Exception message: \n" + e.getMessage(), e);

        if (e instanceof FinWealthApiClientException) {
            FinWealthApiClientException restException = (FinWealthApiClientException) e;
            //parse the message to the standard model
            return new BaseResponse(restException.getHttpStatus(), restException.getMessage());
        }
        //last fallback for all unknown exception
        return new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong. Please try again");
    }

}
