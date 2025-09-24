/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */

public class SMSResponseMonty {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("OriginatingAddress")
    @Expose
    private String originatingAddress;
    @SerializedName("DestinationAddress")
    @Expose
    private String destinationAddress;
    @SerializedName("MessageCount")
    @Expose
    private Integer messageCount;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginatingAddress() {
        return originatingAddress;
    }

    public void setOriginatingAddress(String originatingAddress) {
        this.originatingAddress = originatingAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
    
    
}
