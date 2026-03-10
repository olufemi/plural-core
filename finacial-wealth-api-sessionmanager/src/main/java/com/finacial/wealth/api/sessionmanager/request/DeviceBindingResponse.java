/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.request;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class DeviceBindingResponse {

    private String deviceId;
    private String status;    // NONE|PENDING|ACTIVE
    private String activeKid;
    private String devicePublicSpkiB64;
}
