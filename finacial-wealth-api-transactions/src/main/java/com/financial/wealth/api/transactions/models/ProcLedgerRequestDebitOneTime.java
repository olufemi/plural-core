/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

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
