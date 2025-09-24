/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ProcNotifyFincraFundWallet {

    private String business;
    private String virtualAccount;

    private Double amountReceived;
    private Double fee;
    private Double sourceAmount;
    private String event;

    private String reference;
    private String payLoad;
    private String reason;

}
