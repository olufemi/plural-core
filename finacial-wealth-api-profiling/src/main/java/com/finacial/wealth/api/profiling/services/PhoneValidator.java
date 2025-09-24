/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

/**
 *
 * @author olufemioshin
 */
import java.util.regex.Pattern;

public class PhoneValidator {
    // Strict E.164 (requires leading +)
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    // Nigeria local mobile format: 11 digits starting with 0 (e.g., 080..., 081..., 090..., 091...)
    private static final Pattern NG_LOCAL = Pattern.compile("^0\\d{10}$");

    /** Accepts either strict E.164 or NG local 11-digit format */
    public static boolean isValidPhone(String raw) {
        if (raw == null) return false;
        String s = sanitize(raw);
        return E164.matcher(s).matches() || NG_LOCAL.matcher(s).matches();
    }

    /** Convert to E.164 for NG if possible (keeps +numbers as-is) */
    public static String toE164Nigeria(String raw) {
        if (raw == null) return null;
        String s = sanitize(raw);
        if (E164.matcher(s).matches()) return s;               // already E.164
        if (NG_LOCAL.matcher(s).matches()) return "+234" + s.substring(1); // 0XXXXXXXXXX -> +234XXXXXXXXXX
        throw new IllegalArgumentException("Not a valid NG local or E.164 number: " + raw);
    }

    /** Keep leading '+' if present; strip everything else non-digit */
    private static String sanitize(String s) {
        s = s.trim();
        boolean plus = s.startsWith("+");
        String digits = s.replaceAll("\\D", "");  // remove non-digits
        return plus ? "+" + digits : digits;
    }
}

