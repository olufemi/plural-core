package com.finacial.wealth.api.utility.controllers;

import com.finacial.wealth.api.utility.models.OtpRequest;
import com.finacial.wealth.api.utility.models.OtpValidateRequest;
import com.finacial.wealth.api.utility.models.ReqRequestId;
import com.finacial.wealth.api.utility.response.BaseResponse;
import com.finacial.wealth.api.utility.services.OtpService;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

    private static final Logger LOG = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<BaseResponse> sendOtp(@RequestBody @Valid OtpRequest request) {
        BaseResponse baseResponse = otpService.createAndSendOtp(request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/send-sms-only")
    public ResponseEntity<BaseResponse> smsOnly(@RequestBody @Valid OtpRequest request) {
        BaseResponse baseResponse = otpService.createAndSendOtpSMSOnly(request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/send-email")
    public ResponseEntity<BaseResponse> sendOtpEmail(@RequestBody @Valid OtpRequest request) {
        BaseResponse baseResponse = otpService.createAndSendOtpEmail(request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<BaseResponse> validateOtp(@RequestBody @Valid OtpValidateRequest request) {
        BaseResponse baseResponse = otpService.validateOtp(request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/request")
    public ResponseEntity<BaseResponse> getOtp(@RequestBody ReqRequestId requestId) {
        BaseResponse baseResponse = otpService.getOtpByRequestIdExist(requestId);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponse> createOtp(@RequestBody @Valid OtpRequest request) {
        BaseResponse baseResponse = otpService.createOtp(request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }
}
