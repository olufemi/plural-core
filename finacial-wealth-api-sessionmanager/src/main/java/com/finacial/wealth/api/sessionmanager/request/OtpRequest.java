package com.finacial.wealth.api.sessionmanager.request;

import lombok.Data;

@Data
public class OtpRequest {

    private String userId;

    private String serviceName;

    private String phoneNumber;

    private String otp;

    private String newUserId;

}
