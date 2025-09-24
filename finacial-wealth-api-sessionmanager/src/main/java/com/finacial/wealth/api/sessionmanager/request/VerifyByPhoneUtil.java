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
 * @author olufemioshin
 */
@Data
public class VerifyByPhoneUtil {

    @ApiModelProperty(notes = "The Otp")
    @NotNull(message = "the field \"otp\" is not nillable")
    //@NotBlank
    @Expose
    private int otp;
    @ApiModelProperty(notes = "The RequestId")
    @NotNull(message = "the field \"requestId\" is not nillable")
    @NotBlank
    @Expose
    private String requestId;
}
