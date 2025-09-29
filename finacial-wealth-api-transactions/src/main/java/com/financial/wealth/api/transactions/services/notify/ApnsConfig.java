/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.notify;

import java.util.Map;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ApnsConfig {
   
    public Map<String,String> headers;   // apns-priority, apns-expiration, apns-collapse-id
    public Map<String,Object> payload;   // {"aps": {...}}

}
