/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

/**
 *
 * @author olufemioshin
 */
public record ConsentChallengeRequest(
        String deviceId,
        String action,       // e.g. TRANSFER, ADD_BENEFICIARY, CHANGE_PROFILE
        String refId,        // your txId or any action reference id
        java.util.Map<String, Object> payload // fields to bind
) {}
