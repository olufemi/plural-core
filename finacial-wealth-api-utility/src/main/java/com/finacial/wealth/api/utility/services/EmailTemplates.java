/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

import com.finacial.wealth.api.utility.models.NotificationEvent;

/**
 *
 * @author olufemioshin
 */
public class EmailTemplates {

    public static String subject(NotificationEvent evt) {
        return "FinacialWealth - " + evt.getProcess().name().replace('_', ' ');
    }

    public static String render(NotificationEvent evt) {
        return "<div style='font-family:Arial'>"
                + "<h3>" + safe(evt.getTitle()) + "</h3>"
                + "<p>" + safe(evt.getMessage()) + "</p>"
                + "</div>";
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }
}

