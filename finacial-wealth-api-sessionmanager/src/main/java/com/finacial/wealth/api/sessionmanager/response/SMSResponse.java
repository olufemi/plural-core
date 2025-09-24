package com.finacial.wealth.api.sessionmanager.response;

import lombok.Data;

@Data
public class SMSResponse {

    private String response_code;

    private String response_desc;

    private String response_message;

    private String applicationId;

    private String receiverNumber;

}
