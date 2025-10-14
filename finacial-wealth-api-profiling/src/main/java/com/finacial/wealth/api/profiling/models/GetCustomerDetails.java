/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetCustomerDetails {

    private String customerName;
    private String walletNo;
    private String virtualAccount;
    private String virtualAccountType;
    private String virtualAccountName;
    private String accountBalance;
    private String bookAccountBalance;
    private String maxAccountBalance;
    private String customerTier;
    private String dailyLimit;
    private String singleTransactionLimit;
    private String dailyLimitBalance;
    private String emailAddressValidation;
    private String pinCreatedValidation;
    private String bvnValidation;
    private String usedDailyLimitBalance;
    private String merchantBookedAccountBalance;
    private String walletId;
    private String currencyCode;

}
