package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgram;
import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgramAudit;
import com.finacial.wealth.api.profiling.referralprogram.model.CreateReferralProgramRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.UpdateReferralProgramRequest;
import com.finacial.wealth.api.profiling.referralprogram.service.ReferralProgramService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/referral-programs")
public class ReferralProgramController {

    private final ReferralProgramService service;

    public ReferralProgramController(ReferralProgramService service) {
        this.service = service;
    }

    private String actor(String header) {
        return (header == null || header.trim().isEmpty()) ? "UNKNOWN" : header.trim();
    }

    @PostMapping
    public ResponseEntity<ReferralProgram> create(@RequestBody CreateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.create(req, actor(userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReferralProgram> update(@PathVariable Long id,
            @RequestBody UpdateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.update(id, req, actor(userId)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ReferralProgram> activate(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.activate(id, actor(userId)));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ReferralProgram> pause(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.pause(id, actor(userId)));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ReferralProgram> end(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.end(id, actor(userId)));
    }

    @GetMapping
    public ResponseEntity<List<ReferralProgram>> list(@RequestParam(value = "productType", required = false) String productType) {
        return ResponseEntity.ok(service.listByProductType(productType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReferralProgram> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<ReferralProgramAudit>> audit(@PathVariable Long id) {
        return ResponseEntity.ok(service.auditTrail(id));
    }

    @GetMapping("/active")
    public ResponseEntity<ReferralProgram> active(@RequestParam("productType") String productType) {
        return ResponseEntity.ok(service.getActive(productType));
    }
}
