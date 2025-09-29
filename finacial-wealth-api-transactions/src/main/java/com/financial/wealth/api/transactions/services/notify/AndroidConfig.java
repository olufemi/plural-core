/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.notify;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public  class AndroidConfig {
    public String priority;    // "HIGH" or "NORMAL"
    public String ttl;         // e.g., "86400s"
    public AndroidNotification notification;
    public String collapse_key;  // dedupe on device for Android
}
