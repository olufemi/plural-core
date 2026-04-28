package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.services.ReversalAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reversals")
public class AdminReversalController {

    private final ReversalAdminService reversalAdminService;

    public AdminReversalController(ReversalAdminService reversalAdminService) {
        this.reversalAdminService = reversalAdminService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseModel> getSummary() {
        return new ResponseEntity<>(reversalAdminService.getSummary(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponseModel> getCases(@RequestParam(required = false) String status) {
        return new ResponseEntity<>(reversalAdminService.getCases(status), HttpStatus.OK);
    }

    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<ApiResponseModel> retry(@PathVariable String transactionId) {
        return new ResponseEntity<>(reversalAdminService.retryCase(transactionId), HttpStatus.OK);
    }
}
