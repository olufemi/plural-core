/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
//@Data
//public class AuthUserRequestCustomerUuid {
//
//    /*@ApiModelProperty(notes = "The EmailAddress")
//    @NotNull(message = "the field \"emailAddress\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String emailAddress;
//    /*@ApiModelProperty(notes = "The JoinTransactionId")
//    @Expose*/
//    private String joinTransactionId;
//    /*@ApiModelProperty(notes = "The password")
//    @NotNull(message = "the field \"password\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String password;
//
//    /*@ApiModelProperty(notes = "The deviceType")
//    @NotNull(message = "the field \"deviceType\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String deviceType;
//
//    /*@ApiModelProperty(notes = "The userDeviceId")
//    @NotNull(message = "the field \"userDeviceId\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String userDeviceId;
//
//    //@ApiModelProperty(notes = "The browserType")
//    /* @NotNull(message = "the field \"browserType\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String browserType;
//
//    /*@ApiModelProperty(notes = "The osType")
//    @NotNull(message = "the field \"osType\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String osType;
//
//    /*@ApiModelProperty(notes = "The uuid")
//    @NotNull(message = "the field \"uuid\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String uuid;
//
//    /*@ApiModelProperty(notes = "The pushNotificationToken")
//    @NotNull(message = "the field \"pushNotificationToken\" is not nillable")
//    @NotBlank
//    @Expose*/
//    private String pushNotificationToken;
//    private String appType;
//    private String appVersion;
//
//}
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // still keep this for forward compatibility
public class AuthUserRequestCustomerUuid {

    private String emailAddress;
    private String joinTransactionId;
    private String password;
    private String deviceType;
    private String userDeviceId;
    private String browserType;
    private String osType;
    private String uuid;
    private String pushNotificationToken;

    private String appType;
    private String appVersion; // ✅ add this
}
