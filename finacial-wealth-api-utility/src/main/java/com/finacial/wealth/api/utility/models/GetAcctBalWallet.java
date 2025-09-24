/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.models;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetAcctBalWallet {

    @ApiModelProperty(notes = "The ProductCode")
    @NotNull(message = "the field \"productCode\" is not nillable")
    private String productCode;

    @ApiModelProperty(notes = "The Phone-Number")
    @NotNull(message = "the field \"phoneNumber\" is not nillable")
    private String phoneNumber;

}
