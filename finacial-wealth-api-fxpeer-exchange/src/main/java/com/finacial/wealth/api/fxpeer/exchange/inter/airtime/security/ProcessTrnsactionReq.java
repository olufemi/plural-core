/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ProcessTrnsactionReq {

    private String operator;
    private String product;
    private String recipient;
    private String amount;
    private String currencyCode;
    private String pin;

}
