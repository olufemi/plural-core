/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.request;

import com.google.gson.annotations.Expose;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author OSHIN
 */
@Data
public class AuthUserRequest {

    @ApiModelProperty(notes = "The Emailaddress")
    @NotNull(message = "the field \"Emailaddress\" is not nillable")
    @NotBlank
    @Expose
    private String emailAddress;
    @ApiModelProperty(notes = "The password")
    @NotNull(message = "the field \"password\" is not nillable")
    @NotBlank
    @Expose
    private String password;

    @ApiModelProperty(notes = "The deviceType")
    @NotNull(message = "the field \"deviceType\" is not nillable")
    @NotBlank
    @Expose
    private String deviceType;

    @ApiModelProperty(notes = "The userDeviceId")
    @NotNull(message = "the field \"userDeviceId\" is not nillable")
    @NotBlank
    @Expose
    private String userDeviceId;

    @ApiModelProperty(notes = "The browserType")
    @NotNull(message = "the field \"browserType\" is not nillable")
    @NotBlank
    @Expose
    private String browserType;

    @ApiModelProperty(notes = "The osType")
    @NotNull(message = "the field \"osType\" is not nillable")
    @NotBlank
    @Expose
    private String osType;

}
