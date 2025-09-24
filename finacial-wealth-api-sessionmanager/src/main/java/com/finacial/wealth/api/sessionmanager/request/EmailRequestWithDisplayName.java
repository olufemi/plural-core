package com.finacial.wealth.api.sessionmanager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

import lombok.Data;

@Data
public class EmailRequestWithDisplayName implements Serializable {
	
	@JsonProperty("From")
	private String from;
	
	@JsonProperty("Recipient")
	private String recipient;
	
	@JsonProperty("Content")
	private String content;
	
	@JsonProperty("DisplayName")
	private String displayName;
	
	@JsonProperty("Subject")
	private String subject;

}
