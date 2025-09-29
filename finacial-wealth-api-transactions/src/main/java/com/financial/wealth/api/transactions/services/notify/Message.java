/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.notify;

import com.financial.wealth.api.transactions.domain.NotificationPushed;
import com.financial.wealth.api.transactions.services.notify.FcmService.Notification;
import java.util.Map;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class Message {

    public String token;       // OR topic OR condition
    public Notification notification;
    public Map<String, String> data;
    public AndroidConfig android;
    public ApnsConfig apns;
    public String topic;       // alternative to token
    public String condition;   // alternative advanced audience
}
