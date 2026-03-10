/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent;

/**
 *
 * @author olufemioshin
 */
public final class ConsentStringUtil {

    private ConsentStringUtil() {
    }

    public static String nz(Object s) {
        return s == null ? "" : String.valueOf(s).trim();
    }

    public static String esc(String s) {
        return s == null ? "" : s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String firstNonBlank(String... vals) {
        if (vals == null) {
            return null;
        }
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }
}
