/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.models.ApiResponseModel;
import com.finacial.wealth.api.profiling.models.ChangeDevice;
import com.finacial.wealth.api.profiling.models.ChangePasswordInApp;
import com.finacial.wealth.api.profiling.models.ChangePasswordRequest;
import com.finacial.wealth.api.profiling.models.ChangePinInApp;
import com.finacial.wealth.api.profiling.models.CreatePinOtp;
import com.finacial.wealth.api.profiling.models.GetActBalReq;
import com.finacial.wealth.api.profiling.models.InitiateForgetPwdDataWallet;
import com.finacial.wealth.api.profiling.models.InitiateUserOnboarding;
import com.finacial.wealth.api.profiling.models.OnBoardUserForSDK;
import com.finacial.wealth.api.profiling.models.UserDeviceReqChange;
import com.finacial.wealth.api.profiling.models.WalletNo;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.services.WalletServices;
import com.finacial.wealth.api.profiling.services.WalletSystemProxyService;
import com.finacial.wealth.api.profiling.utilities.models.OtpResendRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.UnsupportedEncodingException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/walletmgt")
@RequiredArgsConstructor
@Validated
public class WalletMgtController {

    private final WalletServices walletServices;
    private final WalletSystemProxyService walletSystemProxyService;

    /*@PostMapping("/create-user-old")
    public ResponseEntity<BaseResponse> onboardUser(
            @RequestBody @Valid InitiateUserOnboarding rq) {

        BaseResponse baseResponse = walletServices.onboardUser(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }*/
    @PostMapping("/validate-pin")
    public ResponseEntity<BaseResponse> validatePin(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid WalletNo rq) {

        BaseResponse baseResponse = walletServices.validatePin(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/create-user")
    public ResponseEntity<BaseResponse> onboardUser(
            @RequestBody @Valid OnBoardUserForSDK rq) {

        BaseResponse baseResponse = walletServices.onboardUserForSDKCaller(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/initiate-request-device-change")
    public ResponseEntity<BaseResponse> requestDeviceChange(
            // @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid UserDeviceReqChange rq) {

        BaseResponse baseResponse = walletServices.requestDeviceChange(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/change-device")
    public ResponseEntity<BaseResponse> changeDevice(
            //  @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ChangeDevice rq) {

        BaseResponse baseResponse = walletServices.changeDevice(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/initiate-create-pin")
    public ResponseEntity<BaseResponse> initaiteCreatePin(
            @RequestHeader(value = "authorization", required = true) String auth
    ) {

        BaseResponse baseResponse = walletServices.initaiteCreatePin("", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/create-pin")
    public ResponseEntity<BaseResponse> createPinOtp(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CreatePinOtp rq) {

        BaseResponse baseResponse = walletServices.createPinOtp(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/initiate-reset-pin")
    public ResponseEntity<BaseResponse> initaiteResetPin(
            @RequestHeader(value = "authorization", required = true) String auth
    ) {

        BaseResponse baseResponse = walletServices.initaitePinReset("", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/reset-pin")
    public ResponseEntity<BaseResponse> resetPinOtp(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CreatePinOtp rq) {

        BaseResponse baseResponse = walletServices.resetPinOtp(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/initiate-forget-password")
    public ResponseEntity<BaseResponse> initiateForgotPassword(
            //@RequestHeader(value = "channel", required = true) String channel,
            @RequestBody @Valid InitiateForgetPwdDataWallet rq) {

        BaseResponse baseResponse = walletServices.initiateForgotPassword(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Change Password, The API will be consumed by all channels {Web, Mobile and Api}.",
            tags = "Manage Users Services")

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success code, Description"),
        @ApiResponse(code = 400, message = "Validation Error code"),
        @ApiResponse(code = 201, message = "Accepted for processing"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Server end exception"),
        @ApiResponse(code = 404, message = "Resource not available")

    })
    @PostMapping("/change-password")
    public ResponseEntity<BaseResponse> changePassword(
            // @RequestHeader(value = "channel", required = true) String channel,
            @RequestBody @Valid ChangePasswordRequest rq) {

        BaseResponse baseResponse = walletServices.changePassword(rq, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/resend-otp-new")
    public ResponseEntity<BaseResponse> resendOtpNew(@RequestHeader(value = "channel", required = true) String channel,
            @RequestBody @Valid OtpResendRequest rq) {

        BaseResponse baseResponse = walletServices.resendOtpNew(rq, channel);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/get-customer-details")
    public ResponseEntity<ApiResponseModel> getCusDetails(
            @RequestHeader(value = "authorization", required = true) String auth
    ) {

        ApiResponseModel baseResponse = walletServices.getCustomerDetails("", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-account-bal")
    public ResponseEntity<BaseResponse> getActBal(
            // @RequestHeader(value = "channel", required = true) String channel,
            @RequestBody @Valid GetActBalReq rq) {

        BaseResponse baseResponse = walletSystemProxyService.getAccountBalanceCaller(rq.getAuth());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/change-password-in-app")
    public ResponseEntity<BaseResponse> changePasswordInApp(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ChangePasswordInApp rq) throws UnsupportedEncodingException {

        BaseResponse baseResponse = walletServices.changePasswordInApp(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/change-pin-in-app")
    public ResponseEntity<BaseResponse> changePinInApp(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ChangePinInApp rq) throws UnsupportedEncodingException {

        BaseResponse baseResponse = walletServices.changePinInApp(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
