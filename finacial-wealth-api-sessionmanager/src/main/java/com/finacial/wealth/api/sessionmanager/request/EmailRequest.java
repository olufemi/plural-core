package com.finacial.wealth.api.sessionmanager.request;


import lombok.Data;

@Data
public class EmailRequest {
	
	
	private String to;
	
	private String cc;
	
	private String bcc;
	
	private String mail_message;
	
	private String mail_subject;
	

	@Override
	public String toString() {
		return "{ \"To\":\""+to+"\","
				+ "\"Cc\":\""+cc+"\","
				+ " \"Bcc\":\""+bcc+"\"," 
				+ "\"mail_message\":\""+mail_message+"\"," + "\"mail_subject\":\""+mail_subject+"\""+"}";
	}

}
