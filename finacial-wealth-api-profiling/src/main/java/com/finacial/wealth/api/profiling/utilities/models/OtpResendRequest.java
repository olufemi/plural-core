package com.finacial.wealth.api.profiling.utilities.models;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OtpResendRequest {
	
	@NotNull(message ="emailAddress can't be empty")
	private String emailAddress;
	
	@NotNull(message ="requestId can't be empty")
	private String requestId;
}
