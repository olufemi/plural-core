/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent;

/**
 *
 * @author olufemioshin
 */
public interface ConsentPayloadHasher<T> {

    /**
     * Returns the EXACT app-compatible JSON string in the same field order
     * the mobile app hashes.
     */
    String appJsonPayloadString(T request);

    /**
     * Optional backend diagnostic canonical string.
     */
    String diagnosticCanonicalPayload(T request);

    /**
     * Hash used for verification. Normally SHA-256 Base64 of appJsonPayloadString.
     */
    String payloadHashB64(T request);
}
