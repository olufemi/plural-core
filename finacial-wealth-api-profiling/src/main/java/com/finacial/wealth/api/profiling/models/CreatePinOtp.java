/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import lombok.Data;

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

}
