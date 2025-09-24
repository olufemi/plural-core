/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.escrow;

/**
 *
 * @author olufemioshin
 */
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escrows")
public class EscrowController {

    private final EscrowService service;

    public EscrowController(EscrowService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Escrow> get(@PathVariable long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/{id}/fund/buyer")
    public ResponseEntity<Void> fundBuyer(@PathVariable long id,
            @RequestHeader("Idempotency-Key") @NotBlank String idemKey) {
        service.fundBuyer(id, idemKey);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/fund/seller")
    public ResponseEntity<Void> fundSeller(@PathVariable long id,
            @RequestHeader("Idempotency-Key") @NotBlank String idemKey) {
        service.fundSeller(id, idemKey);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/release/buyer")
    public ResponseEntity<Void> releaseBuyer(@PathVariable long id) {
        service.confirmRelease(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/release/seller")
    public ResponseEntity<Void> releaseSeller(@PathVariable long id) {
        service.confirmRelease(id, false);
        return ResponseEntity.noContent().build();
    }
}
