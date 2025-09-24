/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ReceiptController {

    private final ReceiptService service;

    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

    @GetMapping(value = "/{orderId}/receipt/buyer", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> buyer(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getBuyerHtml(orderId));
    }

    @GetMapping(value = "/{orderId}/receipt/seller", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> seller(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getSellerHtml(orderId));
    }
}
