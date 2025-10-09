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
public class OtherBankBeneficiariesIndFind {

    private String beneficiaryNo;

    private String beneficiaryName;

    private String bankCode;
    private String bankName;
}
