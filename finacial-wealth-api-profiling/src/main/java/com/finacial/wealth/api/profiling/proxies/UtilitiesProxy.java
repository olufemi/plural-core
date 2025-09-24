/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.proxies;

import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utilities.models.EmailRequest;
import com.finacial.wealth.api.profiling.utilities.models.OtpRequest;
import com.finacial.wealth.api.profiling.utilities.models.OtpValidateRequest;
import com.finacial.wealth.api.profiling.utilities.models.ReqRequestId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "utilities-service")
public interface UtilitiesProxy {

    @RequestMapping(value = "/email/send", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse sendEmails(@RequestBody EmailRequest rq);

    @RequestMapping(value = "/otp/send", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse sendoTP(@RequestBody OtpRequest rq);

    @RequestMapping(value = "/otp/send-sms-only", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse sendoTPSMSOnly(@RequestBody OtpRequest rq);

    @RequestMapping(value = "/otp/send-email", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse sendOtpEmail(@RequestBody OtpRequest rq);

    @RequestMapping(value = "/otp/request", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse requestReqId(@RequestBody ReqRequestId requestId);

    @RequestMapping(value = "/otp/validate", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse validateOtp(@RequestBody OtpValidateRequest rq);

}
