/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

/**
 *
 * @author olufemioshin
 */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MoneyMinorUnits {

    private MoneyMinorUnits() {
    }

    private static final Map<String, Integer> FRACTION;

    static {
        Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("NGN", 2);
        map.put("USD", 2);
        map.put("CAD", 2);
        map.put("EUR", 2);
        map.put("GBP", 2);
        map.put("JPY", 0);
        map.put("KES", 2);
        map.put("GHS", 2);

        FRACTION = Collections.unmodifiableMap(map);
    }

    public static String toMinorUnits(BigDecimal amount, String currencyCode) {

        if (amount == null) {
            return "0";
        }

        String ccy = currencyCode == null ? "" : currencyCode.trim().toUpperCase();

        Integer scaleObj = FRACTION.get(ccy);

        int scale = scaleObj == null ? 2 : scaleObj;

        BigDecimal scaled = amount.setScale(scale, RoundingMode.HALF_UP);

        return scaled.movePointRight(scale).toBigIntegerExact().toString();
    }
}
