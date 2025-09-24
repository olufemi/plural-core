package com.finacial.wealth.api.profiling.utilities.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SMSRequest {

    @JsonProperty("ReceiverNumber")
    private String receiverNumber;

    @JsonProperty("SMSMessage")
    private String smsMessage;

    @JsonProperty("ApplicationId")
    private String applicationId;

    @JsonProperty("Priority")
    private String priority;

}
