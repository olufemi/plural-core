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
public class ProvidusNipPayOut {

    private String beneficiaryAccountName;
    private String transactionAmount;
    private String currencyCode;
    private String narration;
    private String sourceAccountName;
    private String beneficiaryAccountNumber;
    private String beneficiaryBank;
    private String transactionReference;
    private String userName;
    private String password;

}
