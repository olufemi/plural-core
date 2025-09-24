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
public class InitiateForgetPwdDataWallet {

    @ApiModelProperty(notes = "The Phonenumber")
    @NotNull(message = "the field \"Phonenumber\" is not nillable")
    @NotBlank
    @Expose
    private String phoneNumber;
    private String appDeviceSig;
    @ApiModelProperty(notes = "The UUID")
    @NotNull(message = "the field \"uuid\" is not nillable")
    @NotBlank
    @Expose
    private String uuid;

}
