package com.finacial.wealth.api.utility.models;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class OtpRequest {

    private String userId;

    // @NotEmpty(message = "serviceName can't be empty")
    private String serviceName;

    // @NotEmpty(message = "PhoneNumber can't be empty")
    private String phoneNumber;

    //  @NotEmpty(message = "Emailaddress can't be empty")
    private String emailAddress;

    private String otp;

    private String resend;

    private String requestId;

    private String newUserId;
    private String appDeviceSig;

    public String getResend() {
        return resend;
    }

    public void setResend(String resend) {
        this.resend = resend;
    }

}
