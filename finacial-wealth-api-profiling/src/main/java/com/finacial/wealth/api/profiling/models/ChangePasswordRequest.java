/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ChangePasswordRequest {

    @ApiModelProperty(notes = "The Otp")
    @NotNull(message = "the field \"otp\" is not nillable")
    private int otp;
    @ApiModelProperty(notes = "The NewPassword")
    @NotNull(message = "the field \"newPassword\" is not nillable")
    private String newPassword;
    @ApiModelProperty(notes = "The ReequestId")
    @NotNull(message = "the field \"requestId\" is not nillable")
    private String requestId;
}
