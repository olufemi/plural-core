/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class UserDeviceReqChange {

    @NotNull(message = "UUID can't be empty")
    private String uuid;

    @NotNull(message = "Phone-Number can't be empty")
    private String phoneNumber;

    private String password;
    private String appVersion;
    /*@NotNull(message = "Live-Photo can't be empty")
    private String livePhoto;*/
    private String appDeviceSig;

}
