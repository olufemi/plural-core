package com.finacial.wealth.api.sessionmanager.request;


import lombok.Data;

@Data
public class OtpValidateRequest {
	
	private String requestId;
	
	private String uuid;
	
	private Integer otp;
}
