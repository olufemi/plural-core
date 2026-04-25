package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.services.GroupSavingsAdminService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/group-savings")
public class AdminGroupSavingsController {

    private final GroupSavingsAdminService groupSavingsAdminService;

    public AdminGroupSavingsController(GroupSavingsAdminService groupSavingsAdminService) {
        this.groupSavingsAdminService = groupSavingsAdminService;
    }

    @GetMapping("/contribution-payout-monitoring")
    public ResponseEntity<ApiResponseModel> getContributionPayoutMonitoring(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long groupId
    ) {
        return new ResponseEntity<>(groupSavingsAdminService.getContributionPayoutMonitoring(period, fromDate, toDate, groupId), HttpStatus.OK);
    }

    @GetMapping("/slot-assignment-tracking")
    public ResponseEntity<ApiResponseModel> getSlotAssignmentTracking(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String status
    ) {
        return new ResponseEntity<>(groupSavingsAdminService.getSlotAssignmentTracking(groupId, status), HttpStatus.OK);
    }
}
