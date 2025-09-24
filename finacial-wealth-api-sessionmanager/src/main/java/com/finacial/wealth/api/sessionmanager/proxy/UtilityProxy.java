/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.proxy;

import com.finacial.wealth.api.sessionmanager.request.AuthUserRequest;
import com.finacial.wealth.api.sessionmanager.request.AuthUserRequestCustomerUuid;
import com.finacial.wealth.api.sessionmanager.request.EmailRequestKulean;
import com.finacial.wealth.api.sessionmanager.request.UserDeviceRequest;
import com.finacial.wealth.api.sessionmanager.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "utilities-service")
public interface UtilityProxy {

    @RequestMapping(value = "/email/send", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse sendUserEmailAndSms(@RequestBody EmailRequestKulean rq);

    @RequestMapping(value = "/checkdetails/user", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse checkIfDeviceBelongsToUser(@RequestBody UserDeviceRequest rq, @RequestHeader("channel") String channel);

    @RequestMapping(value = "/walletmgt/user/uuid", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse authenticateWalletUserUuid(@RequestBody AuthUserRequestCustomerUuid rq, @RequestHeader("channel") String channel);

    class UtilityServiceProxyImpl implements UtilityProxy {

        @Override
        public BaseResponse sendUserEmailAndSms(EmailRequestKulean rq) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BaseResponse checkIfDeviceBelongsToUser(UserDeviceRequest rq, String channel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BaseResponse authenticateWalletUserUuid(AuthUserRequestCustomerUuid rq, String channel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
