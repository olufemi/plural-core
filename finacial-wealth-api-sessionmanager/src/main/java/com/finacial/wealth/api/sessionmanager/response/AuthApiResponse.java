package com.finacial.wealth.api.sessionmanager.response;

import lombok.Data;

@Data
public class AuthApiResponse {

    private String statusCode;

    private String statusMessage;

    private String lastName;

    private String userName;

    private String customerId;

    private String firstName;

    private String mobile;

    private String emailAddress;

    private String customerAccountNo;

    private String accounts;

    private String mobileNo;
    private String phoneNumber;
    private String uuid;

    private String bvn;
    private String uniqueIdentificationNo;
    private String phoneNumberVerification;
    String emailAddressVerification;
    String deviceType;
    String osType;
    String userDeviceId;
    String browserType;
    String channel;
    String merchantId;
    String merchantLink;
    String virtualWalletNo;
    private String referralCode;
    private String walletId;

    private String referralCodeLink;

}
