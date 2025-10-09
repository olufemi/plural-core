/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class NameLookUpInterBank {

    private String bankCode;
    private String bankAccount;
    private String bankName;
    private String sender;
    private String amount;
    private String fees;
    private String theNarration;
    private String receiver;
}
