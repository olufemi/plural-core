/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models.local.trans;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class NameLookUp {
    
    private String receiver;
    private String sender;
    private String amount;
    private String fees;
    private String theNarration;

}
