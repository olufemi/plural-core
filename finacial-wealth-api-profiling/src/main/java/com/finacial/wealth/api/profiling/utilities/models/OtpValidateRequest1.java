package com.finacial.wealth.api.profiling.utilities.models;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OtpValidateRequest1 {
	
	@NotNull(message = "RequestId can't be null")
	private String requestId;
	
	@NotNull(message = "Otp can't be null")
	private Integer otp;
	
	private String uuid;
}
