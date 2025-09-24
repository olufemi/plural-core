package com.finacial.wealth.api.sessionmanager.request;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AuthRequest {
	
	@NotNull(message ="UserId is required")
	private String userId;
	
	@NotNull(message= "Password is required")
	private String password;
	
	@NotNull(message= "UUID is required")
	private String uuid;
	
	private String action;
	
	private String appVersion;

	private String originFlag;  //A-Access D-Diamond

	private String deviceMake;

	private String deviceMakeInfo;

}
