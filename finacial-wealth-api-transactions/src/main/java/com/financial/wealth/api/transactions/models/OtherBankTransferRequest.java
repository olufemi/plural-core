/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class OtherBankTransferRequest {

    private String receiverBankAccount;
    private String receiverAccountName;
    private String sender;
    private String amount;
    private String pin;
    private String processId;
    private String transactionType;
    private String fees;
    private String bankCode;
    private String bankName;
    

}
