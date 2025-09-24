package com.finacial.wealth.api.sessionmanager.request;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CompleteRegAuthRequest {
	@NotNull(message ="UserId is required")
	private String userId;
	
	@NotNull(message= "Password is required")
	private String password;
	
	@NotNull(message= "UUID is required")
	private String uuid;
	
	@NotNull(message= "ACCOUNT is required")
	private String account;
	
	private String action;
	
	private int otp;
	
	private String requestId;

	private String appVersion;

}
