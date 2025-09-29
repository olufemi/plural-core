/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ReceiptController {

    private final ReceiptService service;
    private final ReceiptDeliveryService delivery;

    public ReceiptController(ReceiptService service, ReceiptDeliveryService delivery) {
        this.service = service;
        this.delivery = delivery;
    }

    @GetMapping(value = "/{orderId}/receipt/buyer", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> buyer(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getBuyerHtml(orderId));
    }

    @GetMapping(value = "/{orderId}/receipt/seller", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> seller(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getSellerHtml(orderId));
    }

    @GetMapping(value = "/{orderId}/receipt.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long orderId) {
        byte[] pdf = delivery.readPdfBytes(orderId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=FXPeer-Receipt-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping(value = "/{orderId}/receipt/email")
    public ResponseEntity<Void> email(@PathVariable Long orderId,
            @RequestParam(required = false) String buyerEmail,
            @RequestParam(required = false) String sellerEmail) {
        delivery.emailBoth(orderId, buyerEmail, sellerEmail);
        return ResponseEntity.accepted().build();
    }
}
