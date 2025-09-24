/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.services;

import com.finacial.wealth.api.utility.domains.CreateProvidusVirtAccount;
import com.finacial.wealth.api.utility.domains.GlobalLimitConfig;
import com.finacial.wealth.api.utility.domains.ProcessorUserFailedTransInfo;
import com.finacial.wealth.api.utility.domains.RegWalletInfo;
import com.finacial.wealth.api.utility.domains.SessionServiceLogUtil;
import com.finacial.wealth.api.utility.domains.UserDetails;
import com.finacial.wealth.api.utility.domains.WalletTierVerifyBizness;
import com.finacial.wealth.api.utility.models.AuthUserRequestCustomerUuid;
import com.finacial.wealth.api.utility.repository.CreateProvidusVirtAccountRepo;
import com.finacial.wealth.api.utility.repository.ProcessorUserFailedTransInfoRepo;
import com.finacial.wealth.api.utility.repository.RegWalletInfoRepository;
import com.finacial.wealth.api.utility.repository.SessionServiceLogUtilRepo;
import com.finacial.wealth.api.utility.repository.UserDetailsRepository;
import com.finacial.wealth.api.utility.repository.WalletTierVerifyBiznessRepo;
import com.finacial.wealth.api.utility.response.BaseResponse;
import com.finacial.wealth.api.utility.utils.GlobalMethods;
import com.finacial.wealth.api.utility.utils.UttilityMethods;
import com.google.gson.Gson;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UtilityService {
    
    private final Logger logger = LoggerFactory.getLogger(UtilityService.class);
    private final UserDetailsRepository userDeRepo;
    UserDetails userDetailsResult = new UserDetails();
    GlobalLimitConfig gLimitResult = new GlobalLimitConfig();
    private final UttilityMethods utilMeth;
    private final ProcessorUserFailedTransInfoRepo procFailedRepo;
    private final SessionServiceLogUtilRepo sessionServiceLogUtilRepo;
    private final RegWalletInfoRepository regWalletInfoRepo;
    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;
    RegWalletInfo resultWallet = new RegWalletInfo();
    private final WalletTierVerifyBiznessRepo walletTierVerifyBiznessRepo;
    private final CreateProvidusVirtAccountRepo createProvidusVirtAccountRepo;
    private static final String LOGIN_SUCCESSFUL = "Login Successful";
    
    public BaseResponse authenticateUserCustomerUuid(AuthUserRequestCustomerUuid rq, String channel) {
        
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            
            System.out.println(" util authenticateUserCustomerUuid req ::::::::::::::::  %S  " + new Gson().toJson(rq));
            
            logger.info(String.format("rq.getPushNotificationToken()>>>>>> +++++++++++++ =>%s", rq.getPushNotificationToken()));
            
            if (rq.getPushNotificationToken() == null) {
                rq.setPushNotificationToken("");
            }
            
            rq.setUserDeviceId(rq.getUuid());
            
            statusCode = 400;
            if (!channel.equals("Mobile")) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "authenticate-user", "Authenticate User failed, channel type does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                );
                
                responseModel.setDescription("Authenticate User failed, channel type does not exist!");
                responseModel.setStatusCode(statusCode);
                
                procFailedRepo.save(procFailedTrans);
                return responseModel;
                
            }

            //  logger.info(String.format("Device authenticationRequest  rq.getUserDeviceId() >>>>>> +++++++++++++ =>%s", rq.getUserDeviceId()));
            //    logger.info(String.format("Device authenticationRequest  rq.getPhoneNumbers() >>>>>> +++++++++++++ =>%s", rq.getPhoneNumber()));
            if (userDeRepo.existsByEmailAddress(rq.getEmailAddress())) {
                Optional<UserDetails> getUserDetailsResult = userDeRepo.findByEmailAddress(rq.getEmailAddress());
                userDetailsResult = getUserDetailsResult.get();
                
                if (!userDetailsResult.getUserGroup().equals(utilMeth.returnWalletUserGroupId())) {
                    SessionServiceLogUtil log = new SessionServiceLogUtil();
                    
                    log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                    
                    log.setPhoneNumber("");
                    log.setEmailAddress(rq.getEmailAddress());
                    log.setCreatedDate(Instant.now());
                    log.setMethod("Authentication-Wallet-User");
                    log.setCustomerType("Wallet");
                    log.setChannel(channel);
                    String uuid = rq.getUserDeviceId();
                    
                    log.setUuId(uuid);
                    log.setApiResponse("Authenticate User failed, User is not a wallet owner!s");
                    
                    sessionServiceLogUtilRepo.save(log);
                    
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "authenticate-user", "Authenticate User failed, User is not a wallet owner!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                    );
                    
                    responseModel.setDescription("Authenticate User failed, User is not a wallet owner!");
                    responseModel.setStatusCode(statusCode);
                    
                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                }
                
                String encodePwd = utilMeth.encyrpt(rq.getPassword(), encryptionKey);
                // String decrptSavedPassword = utilMeth.decrypt(userDetailsResult.getPassword(), encryptionKey);
                //logger.info(String.format("decrptSavedPassword >>>>>> +++++++++++++ =>%s", decrptSavedPassword));
                //logger.info(String.format("encodePwd >>>>>> +++++++++++++ =>%s", encodePwd));
                //logger.info(String.format("savedpwd >>>>>> +++++++++++++ =>%s", userDetailsResult.getPassword()));

                if (!encodePwd.equals(userDetailsResult.getPassword())) {
                    SessionServiceLogUtil log = new SessionServiceLogUtil();
                    
                    log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                    log.setEmailAddress(rq.getEmailAddress());
                    
                    log.setPhoneNumber("");
                    log.setCreatedDate(Instant.now());
                    log.setMethod("Authentication-Wallet-User");
                    log.setCustomerType("Wallet");
                    log.setChannel(channel);
                    String uuid = rq.getUserDeviceId();
                    
                    log.setUuId(uuid);
                    log.setApiResponse("Authenticate User failed, Password is invalid!");
                    
                    sessionServiceLogUtilRepo.save(log);
                    
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "authenticate-user", "Authenticate User failed, Password is invalid!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                    );
                    
                    responseModel.setDescription("Authenticate User failed, Password is invalid!");
                    responseModel.setStatusCode(statusCode);
                    
                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                    
                }

                //check if user has token and still active
                if (userDetailsResult.isEnabled() == false) {
                    SessionServiceLogUtil log = new SessionServiceLogUtil();
                    
                    log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                    log.setEmailAddress(rq.getEmailAddress());
                    log.setCreatedDate(Instant.now());
                    log.setMethod("Authentication-Wallet-User");
                    log.setCustomerType("Wallet");
                    log.setChannel(channel);
                    String uuid = rq.getUserDeviceId();
                    
                    log.setUuId(uuid);
                    log.setApiResponse("Authenticate User failed, User disabled, please contact The Administrator!");
                    logger.info(String.format("authenticationLogRepository.save(log)   ", rq.getEmailAddress()));
                    
                    sessionServiceLogUtilRepo.save(log);
                    
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "authenticate-user", "Authenticate User failed, User disabled, please contact The Administrator!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                    );
                    
                    responseModel.setDescription("Authenticate User failed, User disabled, please contact The Administrator!");
                    responseModel.setStatusCode(statusCode);
                    
                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                    
                }
                
                Optional<RegWalletInfo> getRecord = regWalletInfoRepo.findByPhoneNumber(userDetailsResult.getUniqueIdentification());
                resultWallet = getRecord.get();
                String getUUIDAllowedUser = resultWallet.getUuidAllowUser() == null ? "0" : resultWallet.getUuidAllowUser();

                //  if (resultWallet.getUerDeviceCustomer() != null) {
                if ("1".equals(resultWallet.getUerDeviceCustomer())) {
                    if (!getUUIDAllowedUser.equals("1")) {
                        if (!resultWallet.getUuid().equals(rq.getUserDeviceId().trim())) {
                            SessionServiceLogUtil log = new SessionServiceLogUtil();
                            
                            log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                            log.setEmailAddress(rq.getEmailAddress());
                            log.setCreatedDate(Instant.now());
                            log.setMethod("Authentication-Wallet-User");
                            log.setCustomerType("Wallet");
                            log.setChannel(channel);
                            String uuid = rq.getUserDeviceId();
                            
                            log.setUuId(uuid);
                            log.setApiResponse("This action will change your existing Device. Do you want to continue?!");
                            logger.info(String.format("Saving to log on request for Device Change >>>>>> +++++++++++++ =>%s", rq.getEmailAddress()));
                            
                            sessionServiceLogUtilRepo.save(log);
                            
                            ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                                    "authenticate-user", "This action will change your existing Device. Do you want to continue?",
                                    String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                            );
                            
                            responseModel.setDescription("This action will change your existing Device. Do you want to continue?");
                            responseModel.setStatusCode(406);
                            
                            procFailedRepo.save(procFailedTrans);
                            return responseModel;
                            
                        }
                    }
                    
                } else {
                    
                    if (regWalletInfoRepo.existsByUuid(rq.getUserDeviceId().trim())) {
                        if (!getUUIDAllowedUser.equals("1")) {
                            
                            if (!resultWallet.getUuid().equals(rq.getUserDeviceId().trim())) {
                                SessionServiceLogUtil log = new SessionServiceLogUtil();
                                
                                log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                                log.setEmailAddress(rq.getEmailAddress());
                                log.setCreatedDate(Instant.now());
                                log.setMethod("Authentication-Wallet-User");
                                log.setCustomerType("Wallet");
                                log.setChannel(channel);
                                String uuid = rq.getUserDeviceId();
                                
                                log.setUuId(uuid);
                                log.setApiResponse("This action will change your existing Device. Do you want to continue?!");
                                
                                sessionServiceLogUtilRepo.save(log);
                                logger.info(String.format("Saving to log on request for Device Change >>>>>> +++++++++++++ =>%s", regWalletInfoRepo.existsByUuid(rq.getUserDeviceId().trim())));
                                
                                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                                        "authenticate-user", "This action will change your existing Device. Do you want to continue?",
                                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                                );
                                
                                responseModel.setDescription("This action will change your existing Device. Do you want to continue?");
                                responseModel.setStatusCode(406);
                                
                                procFailedRepo.save(procFailedTrans);
                                return responseModel;
                                
                            }
                        }

                        /*ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                                "authenticate-user", "The Customer's Device: " + rq.getUserDeviceId().trim() + " already exist!",
                                String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Utilities-Service"
                        );

                        responseModel.setDescription("The Customer's Device already exist!");
                        responseModel.setStatusCode(400);

                        procFailedRepo.save(procFailedTrans);
                        return responseModel;*/
                    }
                    
                    RegWalletInfo updateWallet = regWalletInfoRepo.findByPhoneNumberId(resultWallet.getPhoneNumber());
                    updateWallet.setUerDeviceCustomer("1");
                    updateWallet.setUuid(rq.getUserDeviceId());
                    regWalletInfoRepo.save(updateWallet);
                    
                }
                //}
                //  logger.info(String.format("rq.getPushNotificationToken()>>>>>> +++++++++++++ =>%s", rq.getPushNotificationToken()));

                RegWalletInfo updateWallet2 = regWalletInfoRepo.findByPhoneNumberId(resultWallet.getPhoneNumber());
                updateWallet2.setPushNotificationToken(rq.getPushNotificationToken());
                regWalletInfoRepo.save(updateWallet2);
                
                List<WalletTierVerifyBizness> getBiz = walletTierVerifyBiznessRepo.findByWalletNo(resultWallet.getPhoneNumber());
                String merchantId = null;
                String merchantLink = null;
                
                if (getBiz.size() > 0) {
                    merchantId = getBiz.get(0).getMerchantId();
                    merchantLink = getBiz.get(0).getMerchantLink();
                }
                
                boolean emailAct = resultWallet.isEmailVerification();
                boolean phoneVerify = resultWallet.isCompleted();
                boolean pinCreated = resultWallet.isActivation();
                // logger.info(String.format("utilities  emailAct>>>>>> +++++++++++++ =>%s", emailAct));

                // logger.info(String.format("util phoneVerify>>>>>> +++++++++++++ =>%s", phoneVerify));
                // logger.info(String.format("util pinCreated >>>>>> +++++++++++++ =>%s", pinCreated));
                String emailActivation = (emailAct == true) ? "1" : "0";
                String phoneVerification = (phoneVerify == true) ? "1" : "0";
                String pinActivation = (pinCreated == true) ? "1" : "0";
                responseModel.addData("firstName", userDetailsResult.getFirstName());
                responseModel.addData("lastName", resultWallet.getLastName());
                responseModel.addData("mobile", userDetailsResult.getUniqueIdentification());
                responseModel.addData("email", userDetailsResult.getEmailAddress());
                responseModel.addData("username", userDetailsResult.getUserName());
                responseModel.addData("bvn", resultWallet.getBvnNumber());
                responseModel.addData("phoneNumberVerification", phoneVerification);
                responseModel.addData("emailAddressVerification", emailActivation);
                responseModel.addData("pinCreated", pinActivation);
                responseModel.addData("accountNumber", resultWallet.getAccountNumber());
                responseModel.addData("uuid", resultWallet.getUuid());
                responseModel.addData("referralCode", resultWallet.getReferralCode());
                responseModel.addData("referralCodeLink", resultWallet.getReferralCodeLink());
                responseModel.addData("merchantId", merchantId);
                responseModel.addData("merchantLink", merchantLink);
                String virtualWalletNo = null;
                List<CreateProvidusVirtAccount> getvir = createProvidusVirtAccountRepo.findByWalletNo(resultWallet.getPhoneNumber());
                if (getvir.size() > 0) {
                    virtualWalletNo = getvir.get(0).getAccountNumber();
                }
                
                responseModel.addData("virtualWalletNo", virtualWalletNo);
                
                responseModel.setStatusCode(HttpServletResponse.SC_OK);
                responseModel.setDescription(LOGIN_SUCCESSFUL);
            } else {
                
                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Customer does not exist!");
            }
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
}
