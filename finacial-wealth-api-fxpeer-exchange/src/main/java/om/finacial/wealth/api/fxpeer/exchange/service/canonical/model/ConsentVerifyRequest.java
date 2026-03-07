/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

/**
 *
 * @author olufemioshin
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentVerifyRequest {

    private String method;
    private String path;
    private String refId;
    private String payloadHashB64;
    private String userId;
}