package com.finacial.wealth.api.sessionmanager.request;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
@Data
public class UserDeviceRequest {
	
    @NotEmpty(message = "UUID can't be empty")
	private String uuid;
    
   	private String userId;
   	
   	private String customerId;
   	
   	private String appVersion;
   	
	private boolean otpChallengeNeeded;
   	
	private String phoneNumber;
	   
	private String accountNo;

	private String newUserId;

	private String deviceMake;

	private String deviceMakeInfo;
	
}