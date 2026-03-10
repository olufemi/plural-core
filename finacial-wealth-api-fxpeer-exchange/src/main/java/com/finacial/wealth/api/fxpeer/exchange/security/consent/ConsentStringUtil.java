/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent;

/**
 *
 * @author olufemioshin
 */


public final class ConsentStringUtil {

    private ConsentStringUtil() {
    }

    public static String nz(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static String esc(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }
}
