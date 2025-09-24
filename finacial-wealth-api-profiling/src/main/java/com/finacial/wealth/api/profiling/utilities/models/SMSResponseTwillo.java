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
@Data
public class SMSResponseTwillo {
    
    @SerializedName("account_sid")
@Expose
private String accountSid;
@SerializedName("api_version")
@Expose
private String apiVersion;
@SerializedName("body")
@Expose
private String body;
@SerializedName("date_created")
@Expose
private String dateCreated;
@SerializedName("date_sent")
@Expose
private String dateSent;
@SerializedName("date_updated")
@Expose
private String dateUpdated;
@SerializedName("direction")
@Expose
private String direction;
@SerializedName("error_code")
@Expose
private Object errorCode;
@SerializedName("error_message")
@Expose
private Object errorMessage;
@SerializedName("from")
@Expose
private String from;
@SerializedName("messaging_service_sid")
@Expose
private Object messagingServiceSid;
@SerializedName("num_media")
@Expose
private String numMedia;
@SerializedName("num_segments")
@Expose
private String numSegments;
@SerializedName("price")
@Expose
private Object price;
@SerializedName("price_unit")
@Expose
private Object priceUnit;
@SerializedName("sid")
@Expose
private String sid;
@SerializedName("status")
@Expose
private String status;
@SerializedName("subresource_uris")
@Expose
private SubresourceUris subresourceUris;
@SerializedName("to")
@Expose
private String to;
@SerializedName("uri")
@Expose
private String uri;
    
}
