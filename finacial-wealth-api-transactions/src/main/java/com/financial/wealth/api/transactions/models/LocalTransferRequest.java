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
public class LocalTransferRequest {

    private String receiver;
    private String receiverName;
    private String sender;
    private String amount;
    private String pin;
    private String processId;
    private String transactionType;
    private String fees;

}
