/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import com.google.gson.annotations.Expose;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreatePinOtp {

    private String pin;
    private String confPin;
    private String requestId;
    private String uuid;
    private int otp;
 
    @ApiModelProperty(notes = "The DeviceId")
    @NotNull(message = "the field \"deviceId\" is not nillable")
    @NotBlank
    @Expose
    private String deviceId;

}
