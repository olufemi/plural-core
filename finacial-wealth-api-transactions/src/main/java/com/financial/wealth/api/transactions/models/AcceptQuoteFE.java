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
public class AcceptQuoteFE {

    boolean accepted;
    private String quoteId;
    private String pin;
}
