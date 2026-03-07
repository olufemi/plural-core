/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

/**
 *
 * @author olufemioshin
 */
public record TxChallengeResponse(
        String challengeId,
        String nonceB64,
        String expiresAtIso,
        String txId,
        String txHashB64
) {}
