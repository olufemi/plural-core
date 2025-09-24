package com.finacial.wealth.api.profiling.utilities.models;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class OtpRequestEmail {

    private String userId;

    @NotEmpty(message = "serviceName can't be empty")
    private String serviceName;


    @NotEmpty(message = "Emailaddress can't be empty")
    private String Emailaddress;

    private String otp;

    private String resend;

    private String requestId;

    private String newUserId;

    public String getResend() {
        return resend;
    }

    public void setResend(String resend) {
        this.resend = resend;
    }

}
