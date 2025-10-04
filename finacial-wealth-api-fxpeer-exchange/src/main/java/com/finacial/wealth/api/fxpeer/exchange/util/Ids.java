/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.util;

/**
 *
 * @author olufemioshin
 */
import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String newIdemKey() {
        return UUID.randomUUID().toString();
    }
}
