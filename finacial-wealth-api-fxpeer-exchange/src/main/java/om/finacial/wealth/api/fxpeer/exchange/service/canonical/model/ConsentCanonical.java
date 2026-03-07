/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;

/**
 *
 * @author olufemioshin
 */
public final class ConsentCanonical {

    private ConsentCanonical() {
    }

    public static String canonicalV1(DeviceChallengeEntity c, String userId, String deviceKid) {
        return "v1|CONSENT"
                + "|" + c.getAction()
                + "|" + c.getRefId()
                + "|" + c.getPayloadHashB64()
                + "|" + c.getNonceB64()
                + "|" + c.getExpiresAt().toString()
                + "|" + userId
                + "|" + c.getDeviceId()
                + "|" + deviceKid;
    }

    public static String canonicalRemoteConsentVs(String method, String path, String refId, String payloadHashB64,
            long ts, String nonce, String userId, String deviceId, String kid) {
        return "v1|CONSENT"
                + "|" + method
                + "|" + path
                + "|refId=" + refId
                + "|payloadHashB64=" + payloadHashB64
                + "|ts=" + ts
                + "|nonce=" + nonce
                + "|userId=" + userId
                + "|deviceId=" + deviceId
                + "|kid=" + kid;
    }
}
