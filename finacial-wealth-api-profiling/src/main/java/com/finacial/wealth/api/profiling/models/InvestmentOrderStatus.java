/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.finacial.wealth.api.profiling.models;

/**
 *
 * @author olufemioshin
 */
public enum InvestmentOrderStatus {
    PENDING,            // Just created, pre-hold
    HOLD_PLACED,        // Wallet hold successful
    SENT_TO_PARTNER,    // Sent to partner, awaiting callback/confirmation
    SETTLED,            // Completed successfully
    FAILED,             // Failed (funds restored)
    CANCELLED,
    ACTIVE,
    MATURED
}
