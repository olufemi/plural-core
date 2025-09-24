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
public class ProvidusValAcctRes {

    
    private String bankCode;
    private String accountName;
    private String transactionReference;
    private String bvn;
    private String responseMessage;
    private String accountNumber;
    private String responseCode;
}

