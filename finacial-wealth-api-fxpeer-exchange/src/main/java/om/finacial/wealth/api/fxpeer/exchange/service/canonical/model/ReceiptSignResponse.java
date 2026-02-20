/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

/**
 *
 * @author olufemioshin
 */
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReceiptSignResponse {
    private int v; // 1
    private String kid;
    private String signature;
    private String alg; // ES256
    private String fmt; // R||S
}
