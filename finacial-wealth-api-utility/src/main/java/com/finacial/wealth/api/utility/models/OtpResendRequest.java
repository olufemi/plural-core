package com.finacial.wealth.api.utility.models;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OtpResendRequest {
	
	@NotNull(message ="accountNo can't be empty")
	private String accountNo;
	
	@NotNull(message ="requestId can't be empty")
	private String requestId;
}
