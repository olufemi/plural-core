/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.controllers;

import com.finacial.wealth.api.sessionmanager.request.AuthUserRequestCustomerUuid;
import com.finacial.wealth.api.sessionmanager.response.BaseResponse;
import com.finacial.wealth.api.sessionmanager.services.SessionManagerClientUserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionManagerController {

    private final SessionManagerClientUserService sessionManagerService;

    @ApiOperation(value = "Authenticate-Wallet User Mobile, This API is dev for Customers Login. The API are consumed by all channels {Mobile}.", tags = "Session managers Services")

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success code, Description"),
        @ApiResponse(code = 400, message = "Validation Error code"),
        @ApiResponse(code = 201, message = "Accepted for processing"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Server end exception"),
        @ApiResponse(code = 404, message = "Resource not available")

    })
    @PostMapping("/authenticate/customer-mobile")
    public ResponseEntity<BaseResponse> authenticateWalletUserUuid(@RequestBody AuthUserRequestCustomerUuid rq, @RequestHeader(value = "channel", required = true) String channel,
            HttpServletRequest request) {
        return sessionManagerService.authenticateWalletUserUuid(rq, request, channel);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success code, Description"),
        @ApiResponse(code = 400, message = "Validation Error code"),
        @ApiResponse(code = 201, message = "Accepted for processing"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Server end exception"),
        @ApiResponse(code = 404, message = "Resource not available")

    })
    @GetMapping("/logout")
    public ResponseEntity<BaseResponse> issueToken(@RequestHeader("Authorization") String header) {
        return sessionManagerService.destroyJwt(header);
    }
}
