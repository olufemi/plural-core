package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.services.GroupSavingsAdminService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/groups")
    public ResponseEntity<ApiResponseModel> listGroups(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return new ResponseEntity<>(groupSavingsAdminService.listGroups(status, search, page, size), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponseModel> getGroup(@PathVariable Long groupId) {
        return new ResponseEntity<>(groupSavingsAdminService.getGroup(groupId), HttpStatus.OK);
    }

    @PostMapping("/groups/{groupId}/close")
    public ResponseEntity<ApiResponseModel> closeGroup(@PathVariable Long groupId) {
        return new ResponseEntity<>(groupSavingsAdminService.closeGroup(groupId), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}/cycle-health")
    public ResponseEntity<ApiResponseModel> getGroupCycleHealth(@PathVariable Long groupId) {
        return new ResponseEntity<>(groupSavingsAdminService.getGroupCycleHealth(groupId), HttpStatus.OK);
    }

    @GetMapping("/cycles/{cycleId}/health")
    public ResponseEntity<ApiResponseModel> getCycleHealth(@PathVariable Long cycleId) {
        return new ResponseEntity<>(groupSavingsAdminService.getCycleHealth(cycleId), HttpStatus.OK);
    }

    @PostMapping("/cycles/{cycleId}/retry-failed")
    public ResponseEntity<ApiResponseModel> retryFailedCycle(@PathVariable Long cycleId) {
        return new ResponseEntity<>(groupSavingsAdminService.retryFailedCycle(cycleId), HttpStatus.OK);
    }
}
