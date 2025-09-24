/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.AcceptDeclineSwapSlotReq;
import com.financial.wealth.api.transactions.models.AddedMembersFE;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.GetMemBerDe;
import com.financial.wealth.api.transactions.models.GroupSavingActivation;
import com.financial.wealth.api.transactions.models.GroupSavingConf;
import com.financial.wealth.api.transactions.models.InitiateGroupSavings;
import com.financial.wealth.api.transactions.models.InitiateGroupSavingsV2;
import com.financial.wealth.api.transactions.models.JoinGroupRequest;
import com.financial.wealth.api.transactions.models.LeaveGroupRequest;
import com.financial.wealth.api.transactions.models.ReByEmailAddress;
import com.financial.wealth.api.transactions.models.ReByInvitationCode;
import com.financial.wealth.api.transactions.models.SwapSlotReq;
import com.financial.wealth.api.transactions.services.GroupSavingsService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/group-savings")
@RequiredArgsConstructor
public class GroupSavingsControllers {

    private final GroupSavingsService groupSavingsService;

   /* @PostMapping("/leave-group")
    public ResponseEntity<BaseResponse> leaveGroup(
            @Valid @RequestBody LeaveGroupRequest request,
            BindingResult bindingResult,
            @RequestHeader(value = "channel", required = false) String channel,
            @RequestHeader(value = "authorization") String auth) {

        BaseResponse response = new BaseResponse();

        if (bindingResult.hasErrors()) {
            response.setStatusCode(400);
            response.setDescription(bindingResult.getFieldError().getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        BaseResponse result = groupSavingsService.leaveGroup(request, "", auth);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }*/

    @PostMapping("/leave-group")
    public ResponseEntity<BaseResponse> leaveGroup(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid LeaveGroupRequest rq) {

        BaseResponse baseResponse = groupSavingsService.leaveGroup(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/delete-group-saving")
    public ResponseEntity<BaseResponse> deleteGroupSaving(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid GroupSavingConf rq) {

        BaseResponse baseResponse = groupSavingsService.deleteGroupSaving(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/configued-members-number-data")
    public ResponseEntity<ApiResponseModel> configuedMembersNumberData() {

        ApiResponseModel baseResponse = groupSavingsService.configuedMembersNumberData();
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/initiate-group-savings")
    public ResponseEntity<ApiResponseModel> initiateGroupSavings(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid InitiateGroupSavingsV2 rq) {

        ApiResponseModel baseResponse = groupSavingsService.initiateGroupSavingsV2(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/confirm-create-transaction")
    public ResponseEntity<BaseResponse> confirmCreateTransaction(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid GroupSavingConf rq) {

        BaseResponse baseResponse = groupSavingsService.confirmCreateTransaction(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/activate-group")
    public ResponseEntity<BaseResponse> activateGroup(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid GroupSavingActivation rq) {

        BaseResponse baseResponse = groupSavingsService.activateGroup(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/add-members")
    public ResponseEntity<BaseResponse> addMembers(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid AddedMembersFE rq) {

        BaseResponse baseResponse = groupSavingsService.addMembers(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-user-request-details")
    public ResponseEntity<ApiResponseModel> getUserRequestDetails(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ReByEmailAddress rq) {

        ApiResponseModel baseResponse = groupSavingsService.getUserRequestDetails(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-user-initiated-group-savings")
    public ResponseEntity<ApiResponseModel> getUserInitiateGroupSavings(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ReByEmailAddress rq) {

        ApiResponseModel baseResponse = groupSavingsService.getUserInitiateGroupSavings(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-all-transactions")
    public ResponseEntity<ApiResponseModel> getAllTransactions(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ReByEmailAddress rq) {

        ApiResponseModel baseResponse = groupSavingsService.getAllTransactionsTEST(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/validate-has-pin")
    public ResponseEntity<BaseResponse> validateHasPin(@RequestHeader(value = "authorization", required = true) String auth) {

        BaseResponse baseResponse = groupSavingsService.validateHasPin("", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/get-member-swap-slot-notification-request")
    public ResponseEntity<ApiResponseModel> getMemSwapSlotNotify(@RequestHeader(value = "authorization", required = true) String auth) {

        ApiResponseModel baseResponse = groupSavingsService.getMemSwapSlotNotify(auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-group-savings-transaction-slots")
    public ResponseEntity<ApiResponseModel> getSavingsSlots(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ReByInvitationCode rq) {

        ApiResponseModel baseResponse = groupSavingsService.getSavingsSlots(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/send-swap-slot-request")
    public ResponseEntity<BaseResponse> sendSwapRequest(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid SwapSlotReq rq) {

        BaseResponse baseResponse = groupSavingsService.sendSwapRequest(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/accept-or-decline-swap-slot-request")
    public ResponseEntity<BaseResponse> acceptDeclineSwap(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid AcceptDeclineSwapSlotReq rq) {

        BaseResponse baseResponse = groupSavingsService.acceptDeclineSwap(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/join-group")
    public ResponseEntity<BaseResponse> joinGroup(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid JoinGroupRequest rq) {

        BaseResponse baseResponse = groupSavingsService.joinGroup(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-customer-info")
    public ResponseEntity<BaseResponse> getCusDetails(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid GetMemBerDe rq) {

        BaseResponse baseResponse = groupSavingsService.getCusDetails(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
