/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.notify;

/**
 *
 * @author olufemioshin
 */
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void push(long userId, String title, String body) {
// Hook up to FCM or your provider here.
// For now this is a no-op stub to keep the service boundary.
    }
}
