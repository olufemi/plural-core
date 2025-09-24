/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.enumm;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 * @author olufemioshin
 */
public enum PayoutPolicy {
    EACH_CYCLE,
    PERIOD_END,
    AFTER_ALL_CONTRIBUTIONS;
    /** Case-insensitive parse. Returns Optional.empty() if not recognized. */
    public static java.util.Optional<PayoutPolicy> fromString(String s) {
        if (s == null) return java.util.Optional.empty();
        switch (s.trim().toUpperCase()) {
            case "EACH_CYCLE": return java.util.Optional.of(EACH_CYCLE);
            case "PERIOD_END": return java.util.Optional.of(PERIOD_END);
            case "AFTER_ALL_CONTRIBUTIONS": return java.util.Optional.of(AFTER_ALL_CONTRIBUTIONS);
            default: return java.util.Optional.empty();
        }
    }

    /** True if the input matches one of the enum values (case-insensitive). */
    public static boolean isValid(String s) {
        return fromString(s).isPresent();
    }

    /** Parse or throw IllegalArgumentException with a helpful message. */
    public static PayoutPolicy requireValid(String s) {
        return fromString(s).orElseThrow(() ->
            new IllegalArgumentException(
                "Invalid payoutPolicy: '" + s +
                "'. Allowed: EACH_CYCLE, PERIOD_END, AFTER_ALL_CONTRIBUTIONS"
            )
        );
    }
}
