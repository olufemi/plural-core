/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import java.io.Serializable;

/**
 *
 * @author olufemioshin
 */
// Request to profiling limits endpoint
public class LimitsUiRequest implements Serializable {
private String accountNumber; // single input (YES: client sends accountNumber only)


// optional: if your app has multiple sub-accounts per currency, you can include currency
// but you said profiling knows mapping, so not required.


public LimitsUiRequest() {}
public String getAccountNumber() { return accountNumber; }
public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
