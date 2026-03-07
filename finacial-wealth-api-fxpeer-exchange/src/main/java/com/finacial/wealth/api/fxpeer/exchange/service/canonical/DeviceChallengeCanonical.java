/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;

/**
 *
 * @author olufemioshin
 */
public final class DeviceChallengeCanonical {
    private DeviceChallengeCanonical() {}

    // Stable canonical string - do NOT change order once released
    public static String canonicalV1(DeviceChallengeEntity c, String userId, String kid) {
        return "v1|TX_APPROVAL"
                + "|" + c.getTxId()
                + "|" + c.getTxHashB64()
                + "|" + c.getNonceB64()
                + "|" + c.getExpiresAt().toString()
                + "|" + userId
                + "|" + c.getDeviceId()
                + "|" + kid;
    }
}
