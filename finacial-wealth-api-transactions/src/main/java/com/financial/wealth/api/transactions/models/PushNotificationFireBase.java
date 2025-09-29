/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

import java.util.Map;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class PushNotificationFireBase {

    private String deviceToken;   // send to one token
    private String title;
    private String body;
    private Map<String, String> data; // optional custom data

}
