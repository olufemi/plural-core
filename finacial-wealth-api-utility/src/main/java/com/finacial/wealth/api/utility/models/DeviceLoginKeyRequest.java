/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class DeviceLoginKeyRequest {

    private String deviceId;
    private String devicePublicSpkiB64;
    private String emailAddress;

}
