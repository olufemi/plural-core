/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

/**
 *
 * @author olufemioshin
 */
public record ConsentApproveRequest(
        String challengeId,
        String deviceId,
        String deviceKid,
        String alg,          // ES256
        String sigB64        // DER signature B64 recommended
) {}
