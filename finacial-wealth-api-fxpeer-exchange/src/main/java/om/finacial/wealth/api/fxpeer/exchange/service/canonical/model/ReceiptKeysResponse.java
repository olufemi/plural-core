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
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReceiptKeysResponse {
    private String activeKid;
    private List<KeyItem> keys;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class KeyItem {
        private String kid;
        private String publicKeySpki; // base64 DER SPKI
    }
}
