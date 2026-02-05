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

    // ---- SUBSCRIPTION (buy/topup) lifecycle ----
    PENDING,                 // Just created, pre-hold
    HOLD_PLACED,             // Wallet hold successful
    SENT_TO_PARTNER,         // Sent to partner, awaiting callback/confirmation
    ACTIVE,                  // Subscription is live (position created/active)
    MATURED,                 // Position matured (eligible for liquidation depending on rules)

    // ---- LIQUIDATION lifecycle ----
    LIQUIDATION_PENDING_APPROVAL,   // Customer requested liquidation; reserved amount set; awaiting approval
    LIQUIDATION_PROCESSING,         // Approved/sent to partner; awaiting settlement callback
    LIQUIDATION_FAILED,             // Liquidation failed (release reserve; allow retry/new request)

    // ---- Terminal states (shared) ----
    SETTLED,                 // Completed successfully (subscription or liquidation)
    FAILED,                  // Failed (generic; prefer LIQUIDATION_FAILED for liquidation)
    CANCELLED                // Cancelled by user/admin (release holds/reserves as needed)
}