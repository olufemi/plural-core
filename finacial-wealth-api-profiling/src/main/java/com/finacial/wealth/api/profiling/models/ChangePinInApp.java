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
public class ChangePinInApp {

    private String emailAddress;
    private int oldPin;
    private int newPin;
    private String uuid;

}
