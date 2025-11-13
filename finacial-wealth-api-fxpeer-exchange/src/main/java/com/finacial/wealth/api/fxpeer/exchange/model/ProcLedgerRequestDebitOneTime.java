/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ProcLedgerRequestDebitOneTime {

    private String transactionId;
    private String phonenumber;
    private String description;
    private String finalCharges;
    private String kuleanFess;
    private String narration;
}

