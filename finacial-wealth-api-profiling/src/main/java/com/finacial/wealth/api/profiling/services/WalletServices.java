/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.api.profiling.client.model.AuthUserRequest;
import com.finacial.wealth.api.profiling.client.model.WalletSystemResponse;
import com.finacial.wealth.api.profiling.client.model.WalletSystemUserDetails;
import com.finacial.wealth.api.profiling.client.model.WalletUserRequest;
import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.ChangeDeviceLogFailed;
import com.finacial.wealth.api.profiling.domain.ChangeDeviceLogSucc;
import com.finacial.wealth.api.profiling.domain.CreateVirtualAcctSucc;
import com.finacial.wealth.api.profiling.domain.DeviceChangeLimitConfig;
import com.finacial.wealth.api.profiling.domain.FootprintDecryptEntity;
import com.finacial.wealth.api.profiling.domain.FootprintResponseLog;
import com.finacial.wealth.api.profiling.domain.FootprintValidation;
import com.finacial.wealth.api.profiling.domain.FootprintValidationFailed;
import com.finacial.wealth.api.profiling.domain.GlobalLimitConfig;
import com.finacial.wealth.api.profiling.domain.InvestmentOrder;
import com.finacial.wealth.api.profiling.domain.InvestmentPosition;
import com.finacial.wealth.api.profiling.domain.InvestmentProduct;
import com.finacial.wealth.api.profiling.domain.Otp;
import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import com.finacial.wealth.api.profiling.domain.ProcessorUserFailedTransInfo;
import com.finacial.wealth.api.profiling.domain.ProcessorUserHistoryInfo;
import com.finacial.wealth.api.profiling.domain.ReceiverFailedTransInfo;
import com.finacial.wealth.api.profiling.domain.ReferralsLog;
import com.finacial.wealth.api.profiling.domain.RegWalletCheckLog;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.UserDetails;
import com.finacial.wealth.api.profiling.domain.UserLimitConfig;
import com.finacial.wealth.api.profiling.domain.VerifyEmailAddLog;
import com.finacial.wealth.api.profiling.domain.VerifyReqIdDetailsAuth;
import com.finacial.wealth.api.profiling.email.EmailPublisher;
import com.finacial.wealth.api.profiling.fx.p.p.wallet.WalletTransactionsDetailsRepo;
import com.finacial.wealth.api.profiling.models.AddNewUserToLimit;
import com.finacial.wealth.api.profiling.models.ApiResponseModel;
import com.finacial.wealth.api.profiling.models.ChangeDevice;
import com.finacial.wealth.api.profiling.models.ChangePasswordInApp;
import com.finacial.wealth.api.profiling.models.ChangePasswordRequest;
import com.finacial.wealth.api.profiling.models.ChangePinInApp;
import com.finacial.wealth.api.profiling.models.ComputeInvestmentBalance;
import com.finacial.wealth.api.profiling.models.CreatePinOtp;
import com.finacial.wealth.api.profiling.models.DecryptRequest;
import com.finacial.wealth.api.profiling.models.DecryptResponse;
import com.finacial.wealth.api.profiling.models.FootprintDecryptRequest;
import com.finacial.wealth.api.profiling.models.FootprintDecryptResponse;
import com.finacial.wealth.api.profiling.models.GetCustomerDetails;
import com.finacial.wealth.api.profiling.models.InitiateForgetPwdDataWallet;
import com.finacial.wealth.api.profiling.models.InitiateUserOnboarding;
import com.finacial.wealth.api.profiling.models.InvestmentOrderStatus;
import com.finacial.wealth.api.profiling.models.OnBoardUserForSDK;
import com.finacial.wealth.api.profiling.models.UpgradeUserToLimit;
import com.finacial.wealth.api.profiling.models.UserDetailsRequest;
import com.finacial.wealth.api.profiling.models.UserDeviceReqChange;
import com.finacial.wealth.api.profiling.models.WalletNo;
import com.finacial.wealth.api.profiling.proxies.FootprintValidationProxy;
import com.finacial.wealth.api.profiling.proxies.FrontPrintProxy;
import com.finacial.wealth.api.profiling.proxies.UtilitiesProxy;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.ChangeDeviceLogFailedRepo;
import com.finacial.wealth.api.profiling.repo.ChangeDeviceLogSuccRepo;
import com.finacial.wealth.api.profiling.repo.CreateVirtualAcctSuccRepo;
import com.finacial.wealth.api.profiling.repo.DeviceChangeLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.FootprintDecryptRepository;
import com.finacial.wealth.api.profiling.repo.FootprintResponseLogRepo;
import com.finacial.wealth.api.profiling.repo.FootprintValidationFailedRepo;
import com.finacial.wealth.api.profiling.repo.FootprintValidationRepository;
import com.finacial.wealth.api.profiling.repo.GlobalLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.profiling.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.profiling.repo.InvestmentProductRepository;
import com.finacial.wealth.api.profiling.repo.OtpRepository;
import com.finacial.wealth.api.profiling.repo.PinActFailedTransLogRepo;
import com.finacial.wealth.api.profiling.repo.ProcessorUserFailedTransInfoRepo;
import com.finacial.wealth.api.profiling.repo.ProcessorUserHistoryInfoRepo;
import com.finacial.wealth.api.profiling.repo.ReferralsLogRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletCheckLogRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.repo.UserDetailsRepository;
import com.finacial.wealth.api.profiling.repo.UserGroupRepository;
import com.finacial.wealth.api.profiling.repo.UserLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.VerifyEmailAddLogRepo;
import com.finacial.wealth.api.profiling.repo.VerifyReqIdDetailsAuthRepo;
import com.finacial.wealth.api.profiling.repo.WalletFailedTransInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utilities.models.OtpRequest;
import com.finacial.wealth.api.profiling.utilities.models.OtpResendRequest;
import com.finacial.wealth.api.profiling.utilities.models.OtpValidateRequest;
import com.finacial.wealth.api.profiling.utilities.models.ReqRequestId;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.GlobalMethods;
import com.finacial.wealth.api.profiling.utils.StrongAES;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author olufemioshin
 */
@Service
@Slf4j
public class WalletServices {

    org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

    java.time.format.DateTimeFormatter jodaFormatter = java.time.format.DateTimeFormatter
            .ofPattern("yyyy/MM/dd HH:mm:ss");
    @Value("${spring.profiles.active}")
    private String environment;
    private final Logger logger = LoggerFactory.getLogger(WalletServices.class);

    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final RegWalletInfoRepository regWalletInfoRepo;
    private final ReferralsLogRepo referralsLogRepo;
    RegWalletInfo result = new RegWalletInfo();
    private final WalletFailedTransInfoRepository wallFailTransRepo;
    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;
    private final UttilityMethods utilMeth;
    private final PinActFailedTransLogRepo pinActFailedRepo;
    private final GlobalLimitConfigRepo globalLimitConfigRepo;
    GlobalLimitConfig gLimitResult = new GlobalLimitConfig();
    private final UserLimitConfigRepo userLimitConfigRepo;
    private final WalletSystemProxyService walletSystemProxyService;
    private final ChangeDeviceLogFailedRepo changeDeviceLogFailedRepo;

    private final ChangeDeviceLogSuccRepo changeDeviceLogSuccRepo;
    private final ProcessorUserFailedTransInfoRepo procFailedRepo;
    private final VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo;
    private static final String NO_DEVICE_REGISTERED = "You dont have any device registered";
    private static final String OTP_SUCCESSFULLY_SENT = "Otp Sent SuccessFully.";
    private final UtilitiesProxy utilitiesProxy;

    private static final int USER_EXISTS_BUT_NOT_YET_PHONE_VERIFIED_STATUS_CODE = 50;

    private static final int STANDARD_SUCESS_CODE = 200;
    private static final int VERIFY_EMAIL_ADDRESS_CODE = 60;
    private static final String VERIFY_EMAIL_ADDRESS_MESSAGE = "Otp sent to you, kindly verify email addresss.";

    private static final String DEVICE_ALREADY_REGISTERED = "This device is locked to another user. Please call us or visit any of our branches to unlock this device.";
    private final DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo;
    private final VerifyEmailAddLogRepo verifyEmailAddLogRepo;
    private final UserDetailsRepository userDeRepo;
    private final ProcessorUserHistoryInfoRepo procTransRepo;
    private final UserGroupRepository userGrRepo;
    private final OtpRepository otpRepository;
    private static final String REQUEST_ID_INVALID = "This regisration session has expired. Pls start again or call our contact centre on {Contact-Centre-No} to assist you further.";
    private final CreateVirtualAcctSuccRepo createVirtualAcctSuccRepo;
    private final RegWalletCheckLogRepo regWalletCheckLogRepo;
    private final FrontPrintProxy footprintClient;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final WalletTransactionsDetailsRepo walletTransactionsDetailsRepo;
    private final InvestmentOrderRepository investmentOrderRepository;
    private final InvestmentProductRepository investmentProductRepository;
    private final InvestmentPositionRepository investmentPositionRepository;
    private final EmailPublisher emailPublisher;

    @Value("${fin.wealth.foot.print.key}")
    private String secretKeyConfoged;

    @Value("${fin.wealth.foot.print.mock.response}")
    private String footPrintMockedData;
    @Value("${fin.wealth.foot.print.base.url}")
    private String footPrintBaseUrl;

    private final FootprintValidationProxy footprintValidationProxy;

    private final FootprintValidationRepository footprintValidationRepository;

    private final FootprintValidationFailedRepo footprintValidationFailedRepo;

    private final FootprintResponseLogRepo footprintResponseLogRepo;
    private final FootprintDecryptRepository footprintDecryptRepository;

    //private ObjectMapper objectMapper;
    public WalletServices(FrontPrintProxy footprintClient, RegWalletInfoRepository regWalletInfoRepo,
            ReferralsLogRepo referralsLogRepo,
            WalletFailedTransInfoRepository wallFailTransRepo,
            UttilityMethods utilMeth,
            PinActFailedTransLogRepo pinActFailedRepo,
            GlobalLimitConfigRepo globalLimitConfigRepo,
            UserLimitConfigRepo userLimitConfigRepo, WalletSystemProxyService walletSystemProxyService,
            ChangeDeviceLogFailedRepo changeDeviceLogFailedRepo, ProcessorUserFailedTransInfoRepo procFailedRepo,
            ChangeDeviceLogSuccRepo changeDeviceLogSuccRepo, VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo,
            UtilitiesProxy utilitiesProxy,
            DeviceChangeLimitConfigRepo deviceChangeLimitConfigRepo, VerifyEmailAddLogRepo verifyEmailAddLogRepo,
            UserDetailsRepository userDeRepo, ProcessorUserHistoryInfoRepo procTransRepo,
            UserGroupRepository userGrRepo, OtpRepository otpRepository, CreateVirtualAcctSuccRepo createVirtualAcctSuccRepo,
            RegWalletCheckLogRepo regWalletCheckLogRepo, FootprintValidationProxy footprintValidationProxy,
            FootprintValidationRepository footprintValidationRepository,
            FootprintValidationFailedRepo footprintValidationFailedRepo,
            FootprintResponseLogRepo footprintResponseLogRepo, FootprintDecryptRepository footprintDecryptRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            InvestmentOrderRepository investmentOrderRepository,
            InvestmentProductRepository investmentProductRepository,
            InvestmentPositionRepository investmentPositionRepository, EmailPublisher emailPublisher) {
        this.footprintResponseLogRepo = footprintResponseLogRepo;
        this.footprintValidationFailedRepo = footprintValidationFailedRepo;
        this.footprintValidationRepository = footprintValidationRepository;
        this.footprintValidationProxy = footprintValidationProxy;
        this.footprintClient = footprintClient;
        this.regWalletInfoRepo = regWalletInfoRepo;
        this.referralsLogRepo = referralsLogRepo;
        this.wallFailTransRepo = wallFailTransRepo;
        this.utilMeth = utilMeth;
        this.pinActFailedRepo = pinActFailedRepo;
        this.globalLimitConfigRepo = globalLimitConfigRepo;
        this.userLimitConfigRepo = userLimitConfigRepo;
        this.walletSystemProxyService = walletSystemProxyService;
        this.changeDeviceLogFailedRepo = changeDeviceLogFailedRepo;
        this.changeDeviceLogSuccRepo = changeDeviceLogSuccRepo;
        this.procFailedRepo = procFailedRepo;
        this.verifyReqIdDetailsAuthRepo = verifyReqIdDetailsAuthRepo;
        this.utilitiesProxy = utilitiesProxy;
        this.deviceChangeLimitConfigRepo = deviceChangeLimitConfigRepo;
        this.verifyEmailAddLogRepo = verifyEmailAddLogRepo;
        this.userDeRepo = userDeRepo;
        this.procTransRepo = procTransRepo;
        this.userGrRepo = userGrRepo;
        this.otpRepository = otpRepository;
        this.createVirtualAcctSuccRepo = createVirtualAcctSuccRepo;
        this.regWalletCheckLogRepo = regWalletCheckLogRepo;
        this.footprintDecryptRepository = footprintDecryptRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.walletTransactionsDetailsRepo = walletTransactionsDetailsRepo;
        this.investmentOrderRepository = investmentOrderRepository;
        this.investmentProductRepository = investmentProductRepository;
        this.investmentPositionRepository = investmentPositionRepository;
        this.emailPublisher = emailPublisher;

    }

    public boolean checkGroupExistence(String userGroupId) {
        boolean userGroupExist = false;
        if (userGrRepo.existsByUserGroupId(userGroupId)) {
            userGroupExist = true;
        }
        return userGroupExist;
    }

    public BaseResponse validatePin(WalletNo rq, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            List<RegWalletInfo> getRegUsr = regWalletInfoRepo.findByEmailList(emailAddress);
            if (!rq.getWalleytId().equals(getRegUsr.get(0).getWalletId())) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("verify-email-address",
                        "Suspected fraud!", "", "", emailAddress);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getRegUsr.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("verify-email-address",
                        "The pin is not valid!", "", "", emailAddress);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("The pin is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            responseModel.setDescription("The pin is valid!");
            responseModel.setStatusCode(200);
            return responseModel;

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse initaitePinReset(String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            System.out.println(" jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + auth);

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);

            System.out.println("Otp Sent To User ----- " + result.getFirstName());
            // }
            OtpRequest otp = new OtpRequest();
            otp.setEmailAddress(emailAddress);
            otp.setUserId(result.getUserName());
            otp.setPhoneNumber(getDecoded.phoneNumber);
            otp.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");

            BaseResponse bRes = utilitiesProxy.sendOtpEmail(otp);
            if (bRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("verify-email-address",
                        bRes.getDescription(), "", channel, emailAddress);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;

            }
            String otpReqId = (String) bRes.getData()
                    .get("requestId");

            VerifyReqIdDetailsAuth vDe = new VerifyReqIdDetailsAuth();
            vDe.setCreatedDate(Instant.now());
            vDe.setLastModifiedDate(Instant.now());
            vDe.setRequestId(otpReqId);
            vDe.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");
            vDe.setUserId(result.getPhoneNumber());
            vDe.setProcessId("0");
            vDe.setExpiry(0);
            vDe.setProcessIdUsed("0");
            vDe.setProcessId(otpReqId);
            vDe.setEmailAddress(emailAddress);

            vDe.setUserIdType("phoneNumber");
            responseModel.addData("processId", otpReqId);
            vDe.setJoinTransactionId(result.getJoinTransactionId());
            verifyReqIdDetailsAuthRepo.save(vDe);

            VerifyEmailAddLog vLog = new VerifyEmailAddLog();
            vLog.setCreatedDate(Instant.now());
            vLog.setEmailAddress(emailAddress);
            vLog.setLastModifiedDate(Instant.now());
            vLog.setRequestId(otpReqId);
            vLog.setWalletNo(getDecoded.phoneNumber);
            vLog.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");
            verifyEmailAddLogRepo.save(vLog);

            responseModel.addData("requestId", otpReqId);

            responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
            responseModel.setStatusCode(STANDARD_SUCESS_CODE);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse resetPinOtp(CreatePinOtp rq, String channel) {
        log.info("CreatePin req: {}  ::::::::::::::::::::: ", rq);

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";
        try {
            statusCode = 400;

            String processId = rq.getRequestId();

            log.info("walletActivatePin req " + "  ::::::::::::::::::::: " + processId);

            List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByProcIdList(processId);
            log.info("walletActivatePin  ::::::::::: %S  " + new Gson().toJson(getInitAcPin));

            if (getInitAcPin.size() <= 0) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid process Id!", "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid process Id!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (getInitAcPin.get(0).getProcessIdUsed().equals("1")) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Add other details is already completed!",
                        "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Add other details is already completed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String phone = getInitAcPin.get(0).getUserId();

            if (!org.apache.commons.lang3.StringUtils.equals(rq.getPin(), rq.getConfPin())) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("activate-wallet",
                        "The PINs entered are not identical, kindly confirm the PIN",
                        "", channel,
                        phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The PINs entered are not identical, kindly confirm the PIN");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (!utilMeth.isValid4um(rq.getPin())) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("activate-wallet",
                        "Invalid PIN, kindly check number of digits!",
                        "", channel, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("Invalid PIN, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> getRecordDevice = regWalletInfoRepo.findByEmailByUuid(getInitAcPin.get(0).getEmailAddress(),
                    rq.getUuid().trim());

            if (getRecordDevice.size() <= 0) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid User and uuid!",
                        "", channel, phone);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid User and uuid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            log.info("CreatePin :: encryptionKey: {}  ::::::::::::::::::::: ", encryptionKey);

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);

            log.info("CreatePin :: encyrptedPin: {}  ::::::::::::::::::::: ", encyrptedPin);

            result = getRecordDevice.get(0);
            log.info("Current Wallet Info: {}", result);

            if (!getRecordDevice.get(0).getEmail().equals(result.getEmail())) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid email address!",
                        "", channel, phone);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid email address!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (getRecordDevice.size() <= 0) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Device and Phone-Number do not match!",
                        "", "",
                        "", getInitAcPin.get(0).getUserId());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Device and Phone-Number do not match!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            OtpValidateRequest request1 = new OtpValidateRequest();
            request1.setOtp(rq.getOtp());
            request1.setRequestId(rq.getRequestId());

            BaseResponse bRes = utilitiesProxy.validateOtp(request1);

            if (bRes.getStatusCode() != 200) {

                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;
            }
            // update result
            result.setActivation(true);
            result.setPersonId(encyrptedPin);
            result.setLastModifiedDate(Instant.now());
            // result.setLastModifiedDate(Instant.now());
            RegWalletInfo savedWalletInfo = regWalletInfoRepo.save(result);

            log.info("After PIN Creation: {}", savedWalletInfo);

            VerifyReqIdDetailsAuth updateVeri = getInitAcPin.get(0);
            updateVeri.setProcessIdUsed("0");
            updateVeri.setLastModifiedDate(Instant.now());
            updateVeri.setProcessId(processId);
            //updateVeri.setRequestId(otpReqId);
            verifyReqIdDetailsAuthRepo.save(updateVeri);

            responseModel.setDescription("Wallet PIN reset was successful, Thank you.");
            responseModel.setStatusCode(200);
            responseModel.addData("processId", processId);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            log.error("Error in createPin. Reason: ", ex);
        }
        return responseModel;
    }

    public BaseResponse createNewWalletUser(UserDetailsRequest rq, String channel) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            String phone = rq.getPhoneNumber().trim();

            String userId = null;
            String transId = String.valueOf(GlobalMethods.generateTransactionId());
            String userName = rq.getUserName().trim() + GlobalMethods.generate6Digits();

            if (!this.checkGroupExistence(rq.getUserGroup().trim())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User, User-Group does not exist",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User, User-Group does not exist!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            /*if (!utilMeth.isNumeric(phone)) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The Unique phone Number is not valid, kindly check!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                responseModel
                        .setDescription("Creating User failed, The Unique phone Number is not valid, kindly check!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }*/

 /* if (!utilMeth.isValid11Num(phone)) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The Unique Identification Number is not valid, kindly check number of digits!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription(
                        "Creating User failed, The Unique Identification Number is not valid, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }*/
            String stripedPhoneNumber = phone;

            logger.info(String.format("user-id >>>>>>=>%s", userId));
            logger.info(String.format("caller-channel >>>>>>=>%s", channel));
            logger.info(String.format("TransactionId >>>>>>=>%s", transId));
            logger.info(String.format("userDetails.getEmaillAddress() >>>>>>=>%s", rq.getEmailAddress().trim()));
            logger.info(String.format("stripedPhoneNumber >>>>>>=>%s", stripedPhoneNumber));

            if (userDeRepo.existsByUniqueIdentification(stripedPhoneNumber)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The Unique Identification Number already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User failed, The Unique Identification Number already exist!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (!utilMeth.isValidEmailAddress(rq.getEmailAddress().trim())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The User's EmailAddress is invlaid!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User failed, The User's EmailAddress is invlaid!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (userDeRepo.existsByUniqueIdentification(phone)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The User's Phone Number already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User failed, The User's Phone Number already exist!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (userDeRepo.existsByEmailAddress(rq.getEmailAddress().trim())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The User's EmailAddress already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User failed, The User's EmailAddress already exist!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (userDeRepo.existsByUserName(userName)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User failed, The User's Username already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User failed, The User's Username already exist!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (!rq.getPassword().equals(rq.getConfPassword())) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-user",
                        "Creating User Details, please confirm password!",
                        String.valueOf(GlobalMethods.generateTransactionId()), userId, channel, "Profiling-Service");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Creating User Details, please confirm password!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            // send mail
            // Save UserDetails
            String encodedPwd = rq.getPassword();
            UserDetails userDeInfo = new UserDetails(stripedPhoneNumber, rq.getEmailAddress(), "Authenticated",
                    true, encodedPwd, rq.getLastName(), rq.getUserName(), rq.getUserGroup(), true, true,
                    "No One Time Password", true, rq.getFirstName());
            userDeRepo.save(userDeInfo);

            // Send API response
            ProcessorUserHistoryInfo procSucessTrans = new ProcessorUserHistoryInfo("create-user",
                    "User Details created successfully", String.valueOf(GlobalMethods.generateTransactionId()), userId,
                    channel, "Processor-Channel");
            procTransRepo.save(procSucessTrans);

            responseModel.setDescription("User Details created successfully");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse onboardUser(InitiateUserOnboarding rq) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            String phone = rq.getPhoneNumber();
            String getUUID = rq.getUuid()
                    .trim();
            String processId = String.valueOf(GlobalMethods.generateTransactionId());
            String genUsername = "";
            if (rq.getUuid() == null) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "User Device-id cannot be null!", "", "", "", "");
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("User Device-id cannot be null!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!utilMeth.isNumeric(phone)) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!utilMeth.isPasswordValid(rq.getPassword())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Password is not valid: &,-,_,% are not allowed!", "", "", phone,
                        rq.getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Password is not valid: &,-,_,% are not allowed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!utilMeth.isValid11Num(rq.getPhoneNumber())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid, kindly check number of digits!", "", "", phone,
                        rq.getPhoneNumber());

                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (regWalletInfoRepo.existsByUuid(rq.getUuid()
                    .trim())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Device: " + rq.getUuid()
                                .trim() + " already exist!", "", "", phone,
                        rq.getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Device already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (regWalletInfoRepo.existsByPhoneNumber(rq.getPhoneNumber()
                    .trim())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Phonenumber already exist!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Phonenumber already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (userDeRepo.existsByUniqueIdentification(rq.getPhoneNumber())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phonenumber already exist!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phonenumber already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }
            String encyrptedPassword = utilMeth.encyrpt(String.valueOf(rq.getPassword()), encryptionKey);
            result.setCompleted(true);
            //result.setWalletRegCount("1");
            //result.setTransactionId(processId);
            //result.setRegPoint("");
            //result.setChannel("");
            //result.setRegPointCallerId(phone);
            result.setActivation(false);
            //result.setWalletCustomerType(utilMeth.returnWalletSimpleType());
            result.setAccountName(rq.getFirstName() + " " + rq.getMiddleName() + " " + rq.getLastName());
            genUsername = result.getAccountName();
            result.setFirstName(rq.getFirstName());
            result.setLastName(rq.getLastName());
            result.setMiddleName(rq.getMiddleName());
            result.setFullName(genUsername);
            result.setEmail(rq.getEmailAddress());
            result.setPhoneNumber(rq.getPhoneNumber());
            result.setSecurityAnswer("");
            result.setSecurityQue("");
            String getRefreCode = utilMeth.generateReferralCode("Customer-Onboarding");
            String refLink = utilMeth.getSETTING_REF_LINK() + getRefreCode;
            if (rq.getReferralCode() != null) {

                // validate referralCode
                List<RegWalletInfo> findByReferralCode = regWalletInfoRepo.findByReferralCode(rq.getReferralCode());

                if (findByReferralCode.size() <= 0) {

                    ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                            "Referral Code is invalid, please check!", processId, "", "", rq
                                    .getPhoneNumber());
                    wallFailTransRepo.save(recFailTrans);
                    responseModel.setDescription("Referral Code is invalid, please check!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;

                }

                if (!"1".equals(findByReferralCode.get(0)
                        .getEmailCreation())) {
                    ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                            "Referral Code Owner has not completed onboarding process!", processId, "", "",
                            rq.getPhoneNumber());
                    wallFailTransRepo.save(recFailTrans);
                    responseModel.setDescription("Referral Code Owner has not completed onboarding process!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                ReferralsLog refLog = new ReferralsLog();
                refLog.setCreatedDate(Instant.now());
                refLog.setReceiverNumber(rq.getPhoneNumber());
                refLog.setReferralCode(rq.getReferralCode());
                refLog.setReferralCodeLink(findByReferralCode.get(0)
                        .getReferralCodeLink());
                refLog.setSenderName(
                        findByReferralCode.get(0)
                                .getFirstName() + " " + findByReferralCode.get(0)
                                .getLastName());
                refLog.setSenderNumber(findByReferralCode.get(0)
                        .getPhoneNumber());
                referralsLogRepo.save(refLog);

            }
            result.setReferralCode(getRefreCode);
            result.setReferralCodeLink(refLink);
            result.setAccountBankCode("");
            result.setBvnNumber("");
            result.setDateOfBirth(rq.getDateOfBirth());
            // result.setNationality("");
            result.setCustomerId("");
            result.setUuid(getUUID);
            result.setUserName(genUsername);
            result.setPersonId("");
            result.setPassword(encyrptedPassword);
            result.setPhoneVerification("1");
            result.setEmailCreation("1");
            result.setLivePhotoUpload("0");
            result.setUerDeviceCustomer("1");
            result.setEmailVerification(true);
            result.setWalletTier("Tier 2");
            String walletId = GlobalMethods.generateNUBAN();
            result.setWalletId(walletId);

            result.setCreatedBy("System");
            result.setCreatedDate(Instant.now());

            UserDetailsRequest cUser = new UserDetailsRequest();
            cUser.setConfPassword(result.getPassword());
            cUser.setEmailAddress(result.getEmail());
            cUser.setFirstName(result.getFirstName());
            cUser.setLastName(result.getLastName());
            cUser.setPassword(result.getPassword());
            cUser.setPhoneNumber(result.getPhoneNumber());
            cUser.setUserGroup(utilMeth.returnWalletUserGroupId());
            cUser.setUserName(result.getFirstName());

            BaseResponse getRes = this
                    .createNewWalletUser(cUser, "");

            if (getRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        getRes
                                .getDescription(), "", "", rq.getEmailAddress());
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(getRes.getDescription());
                responseModel.setStatusCode(getRes.getStatusCode());
                return responseModel;
            }

            AddNewUserToLimit addLimit = new AddNewUserToLimit();
            addLimit.setCategory(utilMeth.getTier2());
            addLimit.setWalletNumber(walletId);
            BaseResponse bAddLimitRes = this.addTierToWallet(addLimit);
            //BaseResponse bAddLimitRes = null;

            //ALSO CREATE WALLET HERE
            if (bAddLimitRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        bAddLimitRes.getDescription(), "", "", rq.getPhoneNumber());
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(bAddLimitRes.getDescription());
                responseModel.setStatusCode(bAddLimitRes.getStatusCode());
                return responseModel;
            }
            System.out.println("Validate emailAdd :::::::: add tier successfully"
                    + "  :::::::::::::::::::::  " + bAddLimitRes);

            WalletSystemResponse addUserToWalletSystem = addUserToWalletSystem(rq.getPhoneNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                return responseModel;
            }

            regWalletInfoRepo.save(result);

            responseModel.setDescription(
                    "Customer onboarded successfully, kindly create PIN to be used for transactions. Thank you.");
            responseModel.setStatusCode(80);
            // responseModel.setStatusCode(STANDARD_SUCESS_CODE);
            // return responseModel;

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", rq.getPhoneNumber());
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse validate(String validationToken) {
        Map<String, String> request = new HashMap<>();
        request.put("validation_token", validationToken);
        //validation_token
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";

        String fp_id = null;

        System.out.println("validate request :::::::: "
                + "  :::::::::::::::::::::  " + request);

        String rawResponse = footprintValidationProxy.validateToken(request);
        System.out.println("validate response :::::::: "
                + "  :::::::::::::::::::::  " + rawResponse);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode userNode = root.path("user");

            String fpId = userNode.path("fp_id").asText();
            String onboardingId = userNode.path("onboarding_id").asText();
            String status = userNode.path("status").asText();
            fp_id = fpId;
            FootprintValidation fv = new FootprintValidation();
            fv.setFpId(fpId);
            fv.setOnboardingId(onboardingId);
            fv.setFullResponse(rawResponse);
            fv.setStatus(status);
            fv.setCreatedAt(LocalDateTime.now());
            fv.setCreatedDate(Instant.now());
            fv.setToken(validationToken);

            footprintValidationRepository.save(fv);
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(200);
            Map mp = new HashMap();
            mp.put("fpId", fpId);
            mp.put("status", status);
            mp.put("onboardingId", onboardingId);
            responseModel.setData(mp);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", "");
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;

    }

    public FootprintDecryptResponse decryptUserData(String userId, String secretKey) {
        // String userId = "fp_id_test_Qq1DKRo86aHkjDQfQdczvf";
        //  String secretKey = "sk_test_uRO3Iv4cQJ2witaba3cgYiQh1Mxn8B2iFEyEB";

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("validate userId :::::::: " + "  :::::::::::::::::::::  " + userId);
        System.out.println("validate secretKey :::::::: " + "  :::::::::::::::::::::  " + secretKey);

        // String url = "https://api.onefootprint.com/users/" + userId + "/vault/decrypt";
        String url = footPrintBaseUrl + "/users/" + userId + "/vault/decrypt";

        FootprintDecryptRequest requestBody = new FootprintDecryptRequest();
        requestBody.setFields(Arrays.asList(
                "id.dob",
                "id.last_name",
                "id.ssn4",
                "id.first_name",
                "id.country",
                "id.phone_number",
                "id.email",
                "id.middle_name"
        //,"document.drivers_license.front.image"
        ));
        requestBody.setReason("onboarding process");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Footprint-Secret-Key", secretKey);

        HttpEntity<FootprintDecryptRequest> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String body = response.getBody();
                JsonNode root = objectMapper.readTree(body);

                FootprintDecryptResponse dto = new FootprintDecryptResponse();
                dto.setDob(root.path("id.dob").asText());
                dto.setLastName(root.path("id.last_name").asText());
                dto.setSsn4(root.path("id.ssn4").asText());
                dto.setFirstName(root.path("id.first_name").asText());
                dto.setCountry(root.path("id.country").asText());
                dto.setEmail(root.path("id.email").asText());
                dto.setMobile(root.path("id.phone_number").asText());
                dto.setMiddleName(root.path("id.middle_name").asText());

                // Save to DB
                FootprintDecryptEntity entityObj = new FootprintDecryptEntity();
                entityObj.setFpId(userId);
                entityObj.setDob(dto.getDob());
                entityObj.setLastName(dto.getLastName());
                entityObj.setSsn4(dto.getSsn4());
                entityObj.setFirstName(dto.getFirstName());
                entityObj.setCountry(dto.getCountry());
                entityObj.setRawJson(body);
                entityObj.setEmail(dto.getEmail());
                entityObj.setMobile(dto.getMobile());
                entityObj.setMiddleName(dto.getMiddleName());
                footprintDecryptRepository.save(entityObj);

                return dto;

            } catch (Exception e) {
                throw new RuntimeException("Error parsing response", e);
            }
        }

        throw new RuntimeException("Decryption failed with status: " + response.getStatusCode());

        //  return response.getBody();
        // return footprintClient.decryptVaultFields(secretKey, userId, request);
    }

    public BaseResponse onboardUserForSDKCaller(OnBoardUserForSDK rq) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            //get request from front end
            //call the cloud-kyc with token to get records
            //phoneNumber, email...

            System.out.println("onboardUserForSDKCaller :::::::::: ::::: %S " + new Gson().toJson(rq));

            String ssn4 = "ssn4";
            String last_name = "last_name";
            String first_name = "first_name";
            String country = "country";
            String mobile = String.valueOf(GlobalMethods.generateNUBAN());
            String email = "email";
            String dob = "dob";
            String drivers_license = "drivers_license";
            String middle_Name = "middle_name";

            String fpId = null;
            String status;
            String onboardingId = null;

            System.out.println("onboardUserForSDKCaller :::::::: "
                    + "  :::::::::::::::::::::  " + rq.getPlugin());

            BaseResponse getRes = this.validate(rq.getPlugin());

            if (getRes.getStatusCode() == 200) {
                Map<String, Object> data = getRes.getData();
                if (data instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) data;

                    fpId = (String) map.get("fpId");
                    status = (String) map.get("status");

                    System.out.println("fpId: " + fpId);
                    System.out.println("status: " + status);
                    System.out.println("onboardingId: " + onboardingId);
                }
            } else {
                //log failed request

                FootprintValidationFailed getF = new FootprintValidationFailed();

                getF.setCreatedDate(Instant.now());
                getF.setOnboardingId("");
                getF.setFpId("");
                getF.setStatus("faile");
                getF.setCreatedAt(LocalDateTime.now());
                footprintValidationFailedRepo.save(getF);
                responseModel.setDescription("Onboardinging failed");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!footPrintMockedData.equals("1")) {
                FootprintDecryptResponse getOnboardedUser = this.decryptUserData(fpId, secretKeyConfoged);
                // Map<String, String> decryptedFields = getOnboardedUser.getData();

                // if (decryptedFields != null && decryptedFields.containsKey("id.ssn4")) {
                if (getOnboardedUser.getSsn4() != null) {
                    // ssn4 = decryptedFields.get("id.ssn4");
                    ssn4 = getOnboardedUser.getSsn4();
                    // Use the decrypted value
                }
                // if (decryptedFields != null && decryptedFields.containsKey("id.last_name")) {
                if (getOnboardedUser.getLastName() != null) {
                    // last_name = decryptedFields.get("id.last_name");
                    last_name = getOnboardedUser.getLastName();
                    // Use the decrypted value
                }
                // if (decryptedFields != null && decryptedFields.containsKey("id.first_name")) {
                if (getOnboardedUser.getFirstName() != null) {
                    // first_name = decryptedFields.get("id.first_name");
                    first_name = getOnboardedUser.getFirstName();
                    // Use the decrypted value
                }
                // if (decryptedFields != null && decryptedFields.containsKey("id.country")) {
                if (getOnboardedUser.getCountry() != null) {
                    //  country = decryptedFields.get("id.country");
                    country = getOnboardedUser.getCountry();
                    // Use the decrypted value
                }
                // if (decryptedFields != null && decryptedFields.containsKey("id.mobile")) {
                if (getOnboardedUser.getMobile() != null) {
                    // mobile = decryptedFields.get("id.mobile");
                    mobile = getOnboardedUser.getMobile();
                    // Use the decrypted value
                }
                /* if (decryptedFields != null && decryptedFields.containsKey("document.drivers_license.front.image")) {
                    drivers_license = decryptedFields.get("document.drivers_license.front.image");
                    // Use the decrypted value
                }*/
                // if (decryptedFields != null && decryptedFields.containsKey("id.email")) {
                if (getOnboardedUser.getDob() != null) {
                    // dob = decryptedFields.get("id.dob");
                    dob = getOnboardedUser.getDob();
                    // Use the decrypted value
                }

                if (getOnboardedUser.getEmail() != null) {
                    // dob = decryptedFields.get("id.dob");
                    email = getOnboardedUser.getEmail();
                    // Use the decrypted value
                }

                if (getOnboardedUser.getMiddleName() != null) {
                    middle_Name = getOnboardedUser.getLastName();
                }
                FootprintResponseLog fLog = new FootprintResponseLog();
                fLog.setCreatedDate(Instant.now());
                fLog.setFpId(fpId);
                fLog.setFullResponse(getOnboardedUser.toString());
                fLog.setOnboardingId(fpId);
                fLog.setStatus(onboardingId);
                fLog.setToken(rq.getPlugin());
                fLog.setCreatedAt(LocalDateTime.now());
                fLog.setDob(dob);
                fLog.setMobile(mobile);
                fLog.setMiddleName(middle_Name);
                footprintResponseLogRepo.save(fLog);
            }
            InitiateUserOnboarding mapInit = new InitiateUserOnboarding();
            //  mapInit.setAddress(email);
            // mapInit.setApartment(environment);
            //mapInit.setCity(country);
            mapInit.setConfirmPassword(rq.getPassword());
            mapInit.setCountry(country);
            //mapInit.setDateOfBirth();
            mapInit.setEmailAddress(email);
            mapInit.setFirstName(first_name);
            // mapInit.setGovtId();
            mapInit.setLastName(last_name);
            mapInit.setMiddleName(middle_Name);
            mapInit.setPassword(rq.getPassword());
            mapInit.setPhoneNumber(mobile);
            //mapInit.setReferralCode();
            mapInit.setState("");
            mapInit.setUuid(rq.getUuid());

            mapInit.setZipCode("");

            System.out.println("OnBoardUserForSDK :::::::::: ::::: %S " + new Gson().toJson(mapInit));

            BaseResponse getBase = onboardUserForSDK(mapInit);
            responseModel.setDescription(getBase.getDescription());
            responseModel.setStatusCode(getBase.getStatusCode());

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", "");
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse onboardUserForSDK(InitiateUserOnboarding rq) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            String phone = rq.getPhoneNumber();
            String getUUID = rq.getUuid()
                    .trim();
            String processId = String.valueOf(GlobalMethods.generateTransactionId());
            String genUsername = "";
            if (rq.getUuid() == null) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "User Device-id cannot be null!", "", "", "", "");
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("User Device-id cannot be null!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            /*if (!utilMeth.isNumeric(phone)) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!utilMeth.isPasswordValid(rq.getPassword())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Password is not valid: &,-,_,% are not allowed!", "", "", phone,
                        rq.getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Password is not valid: &,-,_,% are not allowed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/
 /* if (!utilMeth.isValid11Num(rq.getPhoneNumber())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid, kindly check number of digits!", "", "", phone,
                        rq.getPhoneNumber());

                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/
            if (regWalletInfoRepo.existsByUuid(rq.getUuid()
                    .trim())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Device: " + rq.getUuid()
                                .trim() + " already exist!", "", "", phone,
                        rq.getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Device already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (regWalletInfoRepo.existsByPhoneNumber(rq.getPhoneNumber()
                    .trim())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Phonenumber already exist!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Phonenumber already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (userDeRepo.existsByUniqueIdentification(rq.getPhoneNumber())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phonenumber already exist!", "", "", phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phonenumber already exist!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }
            RegWalletInfo resultOnboard = new RegWalletInfo();
            String encyrptedPassword = utilMeth.encyrpt(String.valueOf(rq.getPassword()), encryptionKey);
            resultOnboard.setCompleted(true);
            //result.setWalletRegCount("1");
            //result.setTransactionId(processId);
            //result.setRegPoint("");
            //result.setChannel("");
            //result.setRegPointCallerId(phone);
            resultOnboard.setActivation(false);
            //result.setWalletCustomerType(utilMeth.returnWalletSimpleType());
            resultOnboard.setAccountName(rq.getFirstName() + " " + rq.getMiddleName() + " " + rq.getLastName());
            genUsername = resultOnboard.getAccountName();
            resultOnboard.setFirstName(rq.getFirstName());
            resultOnboard.setLastName(rq.getLastName());
            resultOnboard.setMiddleName(rq.getMiddleName());
            resultOnboard.setFullName(genUsername);
            resultOnboard.setEmail(rq.getEmailAddress());
            resultOnboard.setPhoneNumber(rq.getPhoneNumber());
            resultOnboard.setSecurityAnswer("");
            resultOnboard.setSecurityQue("");
            String getRefreCode = utilMeth.generateReferralCode("Customer-Onboarding");
            String refLink = utilMeth.getSETTING_REF_LINK() + getRefreCode;
            if (rq.getReferralCode() != null) {

                // validate referralCode
                List<RegWalletInfo> findByReferralCode = regWalletInfoRepo.findByReferralCode(rq.getReferralCode());

                if (findByReferralCode.size() <= 0) {

                    ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                            "Referral Code is invalid, please check!", processId, "", "", rq
                                    .getPhoneNumber());
                    wallFailTransRepo.save(recFailTrans);
                    responseModel.setDescription("Referral Code is invalid, please check!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;

                }

                if (!"1".equals(findByReferralCode.get(0)
                        .getEmailCreation())) {
                    ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                            "Referral Code Owner has not completed onboarding process!", processId, "", "",
                            rq.getPhoneNumber());
                    wallFailTransRepo.save(recFailTrans);
                    responseModel.setDescription("Referral Code Owner has not completed onboarding process!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                ReferralsLog refLog = new ReferralsLog();
                refLog.setCreatedDate(Instant.now());
                refLog.setReceiverNumber(rq.getPhoneNumber());
                refLog.setReferralCode(rq.getReferralCode());
                refLog.setReferralCodeLink(findByReferralCode.get(0)
                        .getReferralCodeLink());
                refLog.setSenderName(
                        findByReferralCode.get(0)
                                .getFirstName() + " " + findByReferralCode.get(0)
                                .getLastName());
                refLog.setSenderNumber(findByReferralCode.get(0)
                        .getPhoneNumber());
                referralsLogRepo.save(refLog);

            }
            resultOnboard.setReferralCode(getRefreCode);
            resultOnboard.setReferralCodeLink(refLink);
            resultOnboard.setAccountBankCode("");
            resultOnboard.setBvnNumber("");
            resultOnboard.setDateOfBirth(rq.getDateOfBirth());
            // result.setNationality("");
            resultOnboard.setCustomerId("");
            resultOnboard.setUuid(getUUID);
            resultOnboard.setUserName(genUsername);
            resultOnboard.setPersonId("");
            resultOnboard.setPassword(encyrptedPassword);
            resultOnboard.setPhoneVerification("1");
            resultOnboard.setEmailCreation("1");
            resultOnboard.setLivePhotoUpload("0");
            resultOnboard.setUerDeviceCustomer("1");
            resultOnboard.setEmailVerification(true);
            resultOnboard.setWalletTier("Tier 2");
            if (footPrintMockedData.equals("0")) {
                resultOnboard.setIsOnboarded("0");
            } else {
                resultOnboard.setIsOnboarded("1");
            }
            String walletId = GlobalMethods.generateNUBAN();
            resultOnboard.setWalletId(walletId);

            resultOnboard.setCreatedBy("System");
            resultOnboard.setCreatedDate(Instant.now());

            UserDetailsRequest cUser = new UserDetailsRequest();
            cUser.setConfPassword(resultOnboard.getPassword());
            cUser.setEmailAddress(resultOnboard.getEmail());
            cUser.setFirstName(resultOnboard.getFirstName());
            cUser.setLastName(resultOnboard.getLastName());
            cUser.setPassword(resultOnboard.getPassword());
            cUser.setPhoneNumber(resultOnboard.getPhoneNumber());
            cUser.setUserGroup(utilMeth.returnWalletUserGroupId());
            cUser.setUserName(resultOnboard.getFirstName());

            WalletSystemResponse addUserToWalletSystem = addUserToWalletSystem(rq.getPhoneNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        responseModel.getDescription(), processId, "", "", rq
                        .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription(responseModel.getDescription());
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            BaseResponse getRes = this
                    .createNewWalletUser(cUser, "");

            if (getRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        getRes
                                .getDescription(), "", "", rq.getEmailAddress());
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(getRes.getDescription());
                responseModel.setStatusCode(getRes.getStatusCode());
                return responseModel;
            }

            AddNewUserToLimit addLimit = new AddNewUserToLimit();
            addLimit.setCategory(utilMeth.getTier2());
            addLimit.setWalletNumber(walletId);
            BaseResponse bAddLimitRes = this.addTierToWallet(addLimit);
            //BaseResponse bAddLimitRes = null;

            //ALSO CREATE WALLET HERE
            if (bAddLimitRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        bAddLimitRes.getDescription(), "", "", rq.getPhoneNumber());
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(bAddLimitRes.getDescription());
                responseModel.setStatusCode(bAddLimitRes.getStatusCode());
                return responseModel;
            }
            System.out.println("Validate emailAdd :::::::: add tier successfully"
                    + "  :::::::::::::::::::::  " + bAddLimitRes);

            regWalletInfoRepo.save(resultOnboard);

            responseModel.setDescription(
                    "Customer onboarded successfully, kindly login to create PIN. Thank you.");
            responseModel.setStatusCode(200);

            Map<String, Object> data = new HashMap<>();
            data.put("email", rq.getEmailAddress());
            data.put("firstName", resultOnboard.getFirstName());

            /*emailPublisher.publish(
                    "ONBOARDING",
                    "SUCCESSFUL_SIGNUP",
                    rq.getEmailAddress(),
                    resultOnboard.getFirstName(),
                    data
            );
            */
            // responseModel.setStatusCode(STANDARD_SUCESS_CODE);
            // return responseModel;

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", rq.getPhoneNumber());
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse upGradeWalletTier(UpgradeUserToLimit rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {

            statusCode = 400;

            List<GlobalLimitConfig> glocalConfig = globalLimitConfigRepo.findByLimitCategory(rq.getCategory());

            if (glocalConfig.size() <= 0) {

                ReceiverFailedTransInfo procFailedTrans = new ReceiverFailedTransInfo(
                        "add-user-tier", "Update User Tier, Limit Category does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", "", "Utilities-Service"
                );

                responseModel.setDescription("Update User Tier, Limit Category does not exist!");
                responseModel.setStatusCode(statusCode);

                wallFailTransRepo.save(procFailedTrans);
                return responseModel;

            }

            gLimitResult = glocalConfig.get(0);

            List<UserLimitConfig> getUserConfig = userLimitConfigRepo.findByWalletNumber(rq.getWalletNumber());

            if (getUserConfig.size() <= 0) {

                ReceiverFailedTransInfo procFailedTrans = new ReceiverFailedTransInfo(
                        "add-user-tier", "Update User Tier, Wallet Number does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", "", "Utilities-Service"
                );

                responseModel.setDescription("Update User Tier, Wallet Number does not exist!");
                responseModel.setStatusCode(statusCode);

                wallFailTransRepo.save(procFailedTrans);
                return responseModel;

            }

            if (getUserConfig.get(0).getTierCategory().equals(rq.getCategory())) {
                // && !getUserConfig.get(0).getTierCategory().equals(utilMeth.getTier1())) {

                ReceiverFailedTransInfo procFailedTrans = new ReceiverFailedTransInfo(
                        "add-user-tier", "Update User Tier, UserLimit already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", "", "Utilities-Service"
                );

                responseModel.setDescription("Update User Tier, UserLimit already exist!");
                responseModel.setStatusCode(statusCode);

                wallFailTransRepo.save(procFailedTrans);
                return responseModel;

            }

            UserLimitConfig userLimit = userLimitConfigRepo.findByWalletNumberQuery(getUserConfig.get(0).getWalletNumber());
            userLimit.setTierCategory(gLimitResult.getCategory());
            userLimit.setLastModifiedDate(Instant.now());

            userLimitConfigRepo.save(userLimit);

            responseModel.setDescription("Update User Tier, UserLimit set successfully.");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse upgradeUserLimitCaller(String actionType, String walletNo, String newCategory, String channel,
            String userId) {
        BaseResponse responseModel = new BaseResponse();
        UserLimitConfig currentUser = userLimitConfigRepo.findByWalletNumberQuery(walletNo);

        if (currentUser != null && StringUtils.equals(currentUser.getTierCategory(), newCategory)) {
            responseModel.setDescription("User: " + walletNo + " is already on Tier Level: " + newCategory + "!");
            responseModel.setStatusCode(200);
            return responseModel;
        }

        UpgradeUserToLimit upToLimit1 = new UpgradeUserToLimit();
        upToLimit1.setCategory(newCategory);
        upToLimit1.setWalletNumber(walletNo);
        BaseResponse bResToLimit2 = this.upGradeWalletTier(upToLimit1);
        //BaseResponse bResToLimit2 = null;
        if (bResToLimit2.getStatusCode() != 200) {

            PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog(actionType, bResToLimit2.getDescription(),
                    "", channel, userId);
            pinActFailedRepo.save(pinActTransFailed);
            responseModel.setDescription(bResToLimit2.getDescription());
            responseModel.setStatusCode(bResToLimit2.getStatusCode());
            return responseModel;
        }
        return bResToLimit2;
    }

    public BaseResponse addTierToWallet(AddNewUserToLimit rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            GlobalLimitConfig gLimit = new GlobalLimitConfig();
            UserLimitConfig userLimit = new UserLimitConfig();

            List<GlobalLimitConfig> glocalConfig = globalLimitConfigRepo.findByLimitCategory(rq.getCategory());

            if (glocalConfig.size() <= 0) {

                ReceiverFailedTransInfo procFailedTrans = new ReceiverFailedTransInfo(
                        "add-user-tier", "Add User Tier, Limit Category does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", "", "Utilities-Service"
                );

                responseModel.setDescription("Add User Tier, Limit Category does not exist!");
                responseModel.setStatusCode(statusCode);

                wallFailTransRepo.save(procFailedTrans);
                return responseModel;

            }

            gLimitResult = glocalConfig.get(0);

            List<UserLimitConfig> getUserConfig = userLimitConfigRepo.findByWalletNumber(rq.getWalletNumber());

            if (getUserConfig.size() > 0) {

                ReceiverFailedTransInfo procFailedTrans = new ReceiverFailedTransInfo(
                        "add-user-tier", "Add User Tier, UserLimit already exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", "", "Utilities-Service"
                );

                responseModel.setDescription("Add User Tier, UserLimit already exist!");
                responseModel.setStatusCode(statusCode);

                wallFailTransRepo.save(procFailedTrans);
                return responseModel;

            }

            userLimit.setTierCategory(gLimitResult.getCategory());
            userLimit.setLastModifiedDate(Instant.now());
            userLimit.setCreatedDate(Instant.now());
            userLimit.setWalletNumber(rq.getWalletNumber());

            userLimitConfigRepo.save(userLimit);

            responseModel.setStatusCode(HttpServletResponse.SC_OK);
            responseModel.setDescription("User limit Category created successfully.");

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    private WalletSystemResponse authenticateUser() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        WalletSystemResponse wsResponse = new WalletSystemResponse(500, "Error creating FellowPay User on the Wallet-System!");
        AuthUserRequest authUserRequest = new AuthUserRequest();
        log.info("utilMeth.getWALLET_SYSTEM_PASSWORD() :: {}  ", utilMeth.getWALLET_SYSTEM_PASSWORD());
        log.info("utilMeth.getWALLET_SYSTEM_EMAIL() :: {}  ", utilMeth.getWALLET_SYSTEM_EMAIL());

        authUserRequest.setPassword(decryptData(utilMeth.getWALLET_SYSTEM_PASSWORD()));
        authUserRequest.setEmailAddress(utilMeth.getWALLET_SYSTEM_EMAIL());

        try {
            ResponseEntity<WalletSystemResponse> resEntity = walletSystemProxyService.authenticateUser(authUserRequest);
            if (resEntity != null && resEntity.hasBody()) {
                return resEntity.getBody();
            }
        } catch (Exception e) {
            log.error("Error invoking the authenticate/user endpoint.", e);
            return wsResponse;
        }
        return wsResponse;
    }

    public WalletSystemResponse addUserToWalletSystem(String walletNo) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        WalletSystemResponse response = new WalletSystemResponse(500, "Adding user to wallet system failed!");
        WalletSystemResponse walletSystemResponse = authenticateUser();
        String token = null;

        if (walletSystemResponse != null && walletSystemResponse.getStatusCode() == 200) {
           // log.info("authenticateUser response :: {}", walletSystemResponse);
            WalletSystemUserDetails data = walletSystemResponse.getData();
            String productCode = data.getProductCode();
            if (data != null) {
                token = data.getIdToken();
                WalletUserRequest walletUserRequest = new WalletUserRequest(walletNo, productCode, token);
                try {
                    ResponseEntity<WalletSystemResponse> userStatusEntity = walletSystemProxyService.checkIfWalletNoExists(walletUserRequest);
                    if (userStatusEntity != null && userStatusEntity.hasBody()) {
                        WalletSystemResponse userStatus = userStatusEntity.getBody();
                        //log.info("userStatus :: {}", userStatus);
                        if (userStatus.getStatusCode() == 200) {
                            return new WalletSystemResponse(200, "Wallet user: " + walletNo + " exists already!");
                        } else {
                            ResponseEntity<WalletSystemResponse> addWalletStatusEntity = walletSystemProxyService.addWalletNo(walletUserRequest);
                            if (addWalletStatusEntity != null && addWalletStatusEntity.hasBody()) {
                                WalletSystemResponse addWalletStatus = addWalletStatusEntity.getBody();
                             //   log.info("addWalletStatus :: {}", addWalletStatus);
                                if (addWalletStatus.getStatusCode() == 200) {
                                    return new WalletSystemResponse(200, "Wallet user: " + walletNo + " added successfully!");
                                } else {
                                    return new WalletSystemResponse(500, "Error occurred adding Wallet user to wallet system!");
                                }
                            } else {
                                return new WalletSystemResponse(500, "Error occurred adding Wallet user to wallet system!");
                            }
                        }
                    } else {
                        return new WalletSystemResponse(500, "Error occurred getting Wallet user status!");
                    }
                } catch (MalformedURLException ex) {
                    log.error("error invoking the check-wallet system user API: ", ex);
                    return response;
                }
            } else {
                log.error("authenticate/user no data returned!");
                return response;
            }
        } else {
            log.error("authenticate/user failed!");
            return walletSystemResponse;
        }
    }

    private final Pattern Check10Digits = Pattern.compile("^\\d{10}$");

    private String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);
        return decryptData;

    }

    public boolean isValid10Num(String strNum) {
        if (strNum == null) {
            return false;
        }
        return Check10Digits.matcher(strNum).matches();
    }

    public BaseResponse changeDevice(ChangeDevice rq, String channel) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            String transId = String.valueOf(GlobalMethods.generateTransactionId());
            String regPoint = "Direct Onboarding";

            String phone = rq.getPhoneNumber()
                    .trim();
            /*if (!channel.equals("Mobile")) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "Channel is not valid!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("Creating Wallet failed, Channel is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/

            if (rq.getUuid() == null) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "User Device-id cannot be null!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("User Device-id cannot be null!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            boolean isWalletId = true;
            boolean isPhonenUmber = false;

            if (!isValid10Num(rq.getPhoneNumber().trim())) {
                isWalletId = false;
                isPhonenUmber = true;

            }

            /*if (!utilMeth.isValid11Num(rq.getMemberId())) {

                isPhonenUmber = false;

            }*/
            // System.out.println("isWalletId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + isWalletId);
            if (isWalletId == false && isPhonenUmber == false) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("create-wallet",
                        "Invalid MemberId!", "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);

                responseModel.setDescription("Invalid MemberId!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (isWalletId) {

                List<RegWalletInfo> wallDe = regWalletInfoRepo.findByPhoneNumberData(rq.getPhoneNumber());
                if (wallDe.size() <= 0) {

                    PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("create-wallet",
                            "MemberId does not exist!", "", channel, "");
                    pinActFailedRepo.save(pinActTransFailed);

                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;

                }

                phone = wallDe.get(0).getPhoneNumber();

            }

            if (isPhonenUmber) {

                List<RegWalletInfo> wallDe = regWalletInfoRepo.findByPhoneNumberData(rq.getPhoneNumber());
                if (wallDe.size() <= 0) {

                    PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("create-wallet",
                            "MemberId does not exist!", "", channel, "");
                    pinActFailedRepo.save(pinActTransFailed);

                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;

                }

                phone = wallDe.get(0).getPhoneNumber();

            }

            rq.setPhoneNumber(phone);

            // DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            // String phoneNumber = getDecoded.phoneNumber;
            OtpValidateRequest request1 = new OtpValidateRequest();
            request1.setOtp(rq.getOtp());
            request1.setRequestId(rq.getRequestId());

            List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByRequestId(rq.getRequestId());

            if (getInitAcPin.size() <= 0) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("create-wallet",
                        "RequestId is invalid!", "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("RequestId is invalid!");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            List<ChangeDeviceLogSucc> getLogCh = changeDeviceLogSuccRepo.findByProcessId(rq.getRequestId());

            if (!getLogCh.get(0)
                    .getNewUuid()
                    .equals(rq.getUuid())) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("create-wallet", "UUID is invalid!",
                        "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("UUID is invalid!");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            if (!getLogCh.get(0)
                    .getPhoneNumber()
                    .equals(phone)) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "Phone number is invalid!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("Phone number is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }
            RegWalletInfo updateWallet;

            BaseResponse bRes = utilitiesProxy.validateOtp(request1);

            if (bRes.getStatusCode() == HttpServletResponse.SC_OK) {

                updateWallet = regWalletInfoRepo.findByPhoneNumberId(getInitAcPin.get(0)
                        .getUserId());
                updateWallet.setUuid(rq.getUuid());
                regWalletInfoRepo.save(updateWallet);

                List<DeviceChangeLimitConfig> getDevList = deviceChangeLimitConfigRepo
                        .findByWalletNumberList(rq.getPhoneNumber());

                if (getDevList.size() > 0) {

                    DeviceChangeLimitConfig getDevDetails = deviceChangeLimitConfigRepo
                            .findByWalletNumber(updateWallet.getPhoneNumber());
                    getDevDetails.setLastModifiedDate(new Date());
                    deviceChangeLimitConfigRepo.save(getDevDetails);
                    responseModel.setDescription("Device change was sucessful!");
                    responseModel.setStatusCode(200);
                    return responseModel;

                } else {
                    updateWallet = regWalletInfoRepo.findByPhoneNumberId(getInitAcPin.get(0)
                            .getUserId());

                    DeviceChangeLimitConfig getDevDetails = new DeviceChangeLimitConfig();
                    getDevDetails.setCreatedDate(new Date());
                    getDevDetails.setLastModifiedDate(new Date());
                    getDevDetails.setTierCategory(utilMeth.getDevice_Change());
                    getDevDetails.setWalletNumber(updateWallet.getPhoneNumber());
                    deviceChangeLimitConfigRepo.save(getDevDetails);
                }

                responseModel.setDescription("Device change was sucessful!");
                responseModel.setStatusCode(200);
                return responseModel;

            }

            PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet", bRes.getDescription(),
                    "", channel, getInitAcPin.get(0)
                            .getUserId());
            pinActFailedRepo.save(pinActTransFailed);
            responseModel.setDescription(bRes.getDescription());
            responseModel.setStatusCode(bRes.getStatusCode());
            return responseModel;
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", rq.getPhoneNumber());
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse requestDeviceChange(UserDeviceReqChange rq, String channel) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            String transId = String.valueOf(GlobalMethods.generateTransactionId());
            String regPoint = "Direct Onboarding";

            String phone = rq.getPhoneNumber()
                    .trim();
            /*if (!channel.equals("Mobile")) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "Channel is not valid!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("Creating Wallet failed, Channel is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/

            if (rq.getUuid() == null) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "User Device-id cannot be null!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("User Device-id cannot be null!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            String getUUID = rq.getUuid()
                    .trim();

            /* if (!utilMeth.isNumeric(phone)) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid!", transId, regPoint, phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/
            if (!utilMeth.isPasswordValid(rq.getPassword())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Password is not valid!", transId, regPoint, phone, rq
                                .getPhoneNumber());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Password is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            /*  if (!utilMeth.isValid11Num(rq.getPhoneNumber())) {

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Phone-Number is not valid, kindly check number of digits!", transId, regPoint, phone,
                        rq.getPhoneNumber());

                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Phone-Number is not valid, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/
            //System.out.println("Requests :::::::::: ::::: %S " + new Gson().toJson(rq));
            // 
            String encodePwd = utilMeth.encyrpt(rq.getPassword(), encryptionKey);
            List<RegWalletInfo> getRecord = regWalletInfoRepo.findByPhoneNumberData(phone);
            if (getRecord.size() <= 0) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-wallet",
                        "Authenticate User failed, Phonenumber does not exists!",
                        String.valueOf(GlobalMethods
                                .generateTransactionId()), "", channel, "create-wallet");

                responseModel.setDescription("Authenticate User failed, Phonenumber does not exists!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;

            }

            if (!encodePwd.equals(getRecord.get(0)
                    .getPassword())) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo("create-wallet",
                        "Authenticate User failed, Password is invalid!",
                        String.valueOf(GlobalMethods
                                .generateTransactionId()), "", channel, "create-wallet");

                responseModel.setDescription("Authenticate User failed, Password is invalid!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;

            }

            ChangeDeviceLogFailed LogFailed = new ChangeDeviceLogFailed();
            LogFailed.setCreatedDate(Instant.now());
            LogFailed.setEmailAddress(getRecord.get(0)
                    .getEmail());
            LogFailed.setPhoneNumber(phone);
            LogFailed.setProcessId(transId);
            LogFailed.setUuid(rq.getUuid());

            if (regWalletInfoRepo.existsByUuid(rq.getUuid())) {
                LogFailed.setDescription(DEVICE_ALREADY_REGISTERED);
                changeDeviceLogFailedRepo.save(LogFailed);
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        DEVICE_ALREADY_REGISTERED, transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription(DEVICE_ALREADY_REGISTERED);
                responseModel.setStatusCode(statusCode);
                return responseModel;
                // NO_DEVICE_REGISTERED
            }

            if (getRecord.get(0)
                    .getUuid()
                    .equals(rq.getUuid())) {
                LogFailed.setDescription("This device already exists with you!");
                changeDeviceLogFailedRepo.save(LogFailed);

                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "This device already exists with you!", transId, regPoint, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("This device already exists with you!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            OtpRequest otp = new OtpRequest();
            otp.setPhoneNumber(getRecord.get(0)
                    .getPhoneNumber());
            otp.setUserId(getRecord.get(0)
                    .getUserName());
            otp.setServiceName("Create-Wallet-Profiling-Service");
            otp.setEmailAddress(getRecord.get(0)
                    .getEmail());
            otp.setAppDeviceSig(rq.getAppDeviceSig());
            // System.out.println("profiling sms otp req :::::::: " + "::::: " + new
            // Gson().toJson(otp));

            BaseResponse bRes = utilitiesProxy.sendoTPSMSOnly(otp);
            if (bRes.getStatusCode() != 200) {

                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;
            }
            String otpReqId = (String) bRes.getData()
                    .get("requestId");
            String pNumb = (String) bRes.getData()
                    .get("phoneNumber");

            responseModel.setStatusCode(200);

            ChangeDeviceLogSucc LogSuc = new ChangeDeviceLogSucc();
            LogSuc.setCreatedDate(Instant.now());
            LogSuc.setEmailAddress(getRecord.get(0)
                    .getEmail());
            LogSuc.setPhoneNumber(phone);
            LogSuc.setProcessId(otpReqId);
            LogSuc.setNewUuid(rq.getUuid());
            LogSuc.setExistingUuid(getRecord.get(0)
                    .getUuid());
            changeDeviceLogSuccRepo.save(LogSuc);

            VerifyReqIdDetailsAuth vDe = new VerifyReqIdDetailsAuth();
            vDe.setCreatedDate(Instant.now());
            vDe.setLastModifiedDate(Instant.now());
            vDe.setRequestId(otpReqId);
            vDe.setServiceName("Create-Wallet-Profiling-Service");
            vDe.setUserId(pNumb);
            vDe.setProcessId("0");
            vDe.setProcessIdUsed("0");
            vDe.setExpiry(0);
            vDe.setUserIdType("phoneNumber");
            vDe.setJoinTransactionId(result.getJoinTransactionId());

            verifyReqIdDetailsAuthRepo.save(vDe);

            responseModel.addData("requestId", otpReqId);
            responseModel.addData("phoneNumber", pNumb);
            // responseModel.addData("emailAddress", getRecord.get(0).getEmail());
            responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
            LogSuc.setDescription(OTP_SUCCESSFULLY_SENT);
            changeDeviceLogSuccRepo.save(LogSuc);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet", statusMessage, "", "",
                    "", rq.getPhoneNumber());
            wallFailTransRepo.save(recFailTrans);
            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse initaiteCreatePin(String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            // }
            OtpRequest otp = new OtpRequest();
            otp.setEmailAddress(emailAddress);
            otp.setUserId(result.getUserName());
            otp.setPhoneNumber(getDecoded.phoneNumber);
            otp.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");

            BaseResponse bRes = utilitiesProxy.sendOtpEmail(otp);
            if (bRes.getStatusCode() != 200) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("verify-email-address",
                        bRes.getDescription(), "", channel, emailAddress);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;

            }
            String otpReqId = (String) bRes.getData()
                    .get("requestId");

            VerifyReqIdDetailsAuth vDe = new VerifyReqIdDetailsAuth();
            vDe.setCreatedDate(Instant.now());
            vDe.setLastModifiedDate(Instant.now());
            vDe.setRequestId(otpReqId);
            vDe.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");
            vDe.setUserId(result.getPhoneNumber());
            vDe.setProcessId("0");
            vDe.setExpiry(0);
            vDe.setProcessIdUsed("0");
            vDe.setProcessId(otpReqId);
            vDe.setEmailAddress(emailAddress);

            vDe.setUserIdType("phoneNumber");
            responseModel.addData("processId", otpReqId);
            vDe.setJoinTransactionId(result.getJoinTransactionId());
            verifyReqIdDetailsAuthRepo.save(vDe);

            VerifyEmailAddLog vLog = new VerifyEmailAddLog();
            vLog.setCreatedDate(Instant.now());
            vLog.setEmailAddress(emailAddress);
            vLog.setLastModifiedDate(Instant.now());
            vLog.setRequestId(otpReqId);
            vLog.setWalletNo(getDecoded.phoneNumber);
            vLog.setServiceName("Create-Wallet-Profiling-Service-Send-Otp_By-Email");
            verifyEmailAddLogRepo.save(vLog);

            responseModel.addData("requestId", otpReqId);

            responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
            responseModel.setStatusCode(STANDARD_SUCESS_CODE);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse createPinOtp(CreatePinOtp rq, String channel) {
        log.info("CreatePin req: {}  ::::::::::::::::::::: ", rq);

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";
        try {
            statusCode = 400;

            String processId = rq.getRequestId();

            log.info("walletActivatePin req " + "  ::::::::::::::::::::: " + processId);

            List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByProcIdList(processId);
            log.info("walletActivatePin  ::::::::::: %S  " + new Gson().toJson(getInitAcPin));

            if (getInitAcPin.size() <= 0) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid process Id!", "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid process Id!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (getInitAcPin.get(0).getProcessIdUsed().equals("1")) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Add other details is already completed!",
                        "", channel, "");
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Add other details is already completed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String phone = getInitAcPin.get(0).getUserId();

            if (!org.apache.commons.lang3.StringUtils.equals(rq.getPin(), rq.getConfPin())) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("activate-wallet",
                        "The PINs entered are not identical, kindly confirm the PIN",
                        "", channel,
                        phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The PINs entered are not identical, kindly confirm the PIN");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (!utilMeth.isValid4um(rq.getPin())) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("activate-wallet",
                        "Invalid PIN, kindly check number of digits!",
                        "", channel, phone, phone);
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("Invalid PIN, kindly check number of digits!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> getRecordDevice = regWalletInfoRepo.findByEmailByUuid(getInitAcPin.get(0).getEmailAddress(),
                    rq.getUuid().trim());

            if (getRecordDevice.size() <= 0) {

                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid User and uuid!",
                        "", channel, phone);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid User and uuid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            log.info("CreatePin :: encryptionKey: {}  ::::::::::::::::::::: ", encryptionKey);

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);

            log.info("CreatePin :: encyrptedPin: {}  ::::::::::::::::::::: ", encyrptedPin);

            result = getRecordDevice.get(0);
            log.info("Current Wallet Info: {}", result);
            if (!org.apache.commons.lang3.StringUtils.isBlank(result.getPersonId())) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Wallet user already created PIN!",
                        "", channel, phone);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("PIN already created!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (!getRecordDevice.get(0).getEmail().equals(result.getEmail())) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Invalid email address!",
                        "", channel, phone);
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Invalid email address!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (getRecordDevice.size() <= 0) {
                ReceiverFailedTransInfo recFailTrans = new ReceiverFailedTransInfo("create-wallet",
                        "The Customer's Device and Phone-Number do not match!",
                        "", "",
                        "", getInitAcPin.get(0).getUserId());
                wallFailTransRepo.save(recFailTrans);
                responseModel.setDescription("The Customer's Device and Phone-Number do not match!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            // check phone activation
            if (result.isActivation()) {
                PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("activate-wallet",
                        "Wallet already created PIN. Thank you.", "",
                        channel, getRecordDevice.get(0).getPhoneNumber());
                pinActFailedRepo.save(pinActTransFailed);
                responseModel.setDescription("Wallet already created PIN. Thank you.");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            OtpValidateRequest request1 = new OtpValidateRequest();
            request1.setOtp(rq.getOtp());
            request1.setRequestId(rq.getRequestId());

            BaseResponse bRes = utilitiesProxy.validateOtp(request1);

            if (bRes.getStatusCode() != 200) {

                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                return responseModel;
            }
            // update result
            result.setActivation(true);
            result.setPersonId(encyrptedPin);
            // result.setLastModifiedDate(Instant.now());
            RegWalletInfo savedWalletInfo = regWalletInfoRepo.save(result);

            log.info("After PIN Creation: {}", savedWalletInfo);

            VerifyReqIdDetailsAuth updateVeri = getInitAcPin.get(0);
            updateVeri.setProcessIdUsed("0");
            updateVeri.setLastModifiedDate(Instant.now());
            updateVeri.setProcessId(processId);
            //updateVeri.setRequestId(otpReqId);
            verifyReqIdDetailsAuthRepo.save(updateVeri);

            responseModel.setDescription("Wallet PIN Activation was successful, Thank you.");
            responseModel.setStatusCode(200);
            responseModel.addData("processId", processId);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            log.error("Error in createPin. Reason: ", ex);
        }
        return responseModel;
    }

    public BaseResponse initiateForgotPassword(InitiateForgetPwdDataWallet req, String channel) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";
        try {
            statusCode = 400;
            boolean isPhoneNumber = false;
            String resDesc;
            String processId = String.valueOf(GlobalMethods.generateTransactionId());

            String isPhnNumb;
            String userId = req.getPhoneNumber();

            List<RegWalletInfo> wallDe = regWalletInfoRepo.findByPhoneNumberData(userId);
            if (wallDe.size() <= 0) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "User does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                responseModel.setDescription("User does not exist!");
                responseModel.setStatusCode(statusCode);
                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!wallDe.get(0).getUuid().equals(req.getUuid())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Invalid device!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");
                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid device!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            /* if (!utilMeth.isNumeric(userId)) {
                resDesc = "User initiate change password failed, The details is not a Phonenumber!";

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "user-initiate-forget-password", resDesc, String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel, "Profiling-Service");

                responseModel.setDescription(resDesc);
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;

            } else {
                isPhnNumb = userId;
                isPhoneNumber = true;
            }*/
            isPhnNumb = userId;
            isPhoneNumber = true;

            if (isPhoneNumber & !userDeRepo.existsByUniqueIdentification(isPhnNumb)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "user-initiate-forget-password",
                        "User initiate chnage password failed, Phonenumber does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Profiling-Service");

                responseModel.setDescription("Authenticate User failed, Username does not exist!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            if (isPhoneNumber == true) {
                userId = isPhnNumb;
                Optional<UserDetails> getUserDetailsResult = userDeRepo.findByUserId(isPhnNumb);
                UserDetails userDetailsResult = getUserDetailsResult.get();
                log.info("Otp Sent To User ----- {}", userDetailsResult.getFirstName());

                OtpRequest otp = new OtpRequest();
                otp.setEmailAddress(userDetailsResult.getEmailAddress());
                otp.setUserId(userDetailsResult.getUniqueIdentification());
                otp.setPhoneNumber(userDetailsResult.getUniqueIdentification());
                otp.setServiceName("Initiate-Forget-Password-Profiling-Service");

                log.info("forgot-password sms otp req sendoTPSMSOnly: {} ::::::: ", otp);

                BaseResponse bRes = utilitiesProxy.sendoTPSMSOnly(otp);

                if (bRes.getStatusCode() != 200) {
                    PinActFailedTransLog pinActTransFailed = new PinActFailedTransLog("forget-password-request",
                            bRes.getDescription(),
                            "", channel,
                            userDetailsResult.getEmailAddress());
                    pinActFailedRepo.save(pinActTransFailed);
                    responseModel.setDescription(bRes.getDescription());
                    responseModel.setStatusCode(bRes.getStatusCode());
                    return responseModel;

                }
                String otpReqId = (String) bRes.getData().get("requestId");
                log.info(" req.getPhoneNumber() :::::  >>>>>>>>>>>> ::::::::::::: {} ", req.getPhoneNumber());

                responseModel.addData("requestId", otpReqId);
                responseModel.addData("phoneNumber", req.getPhoneNumber());
                responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
                responseModel.setStatusCode(200);

                VerifyReqIdDetailsAuth vDe = new VerifyReqIdDetailsAuth();
                vDe.setCreatedDate(Instant.now());
                vDe.setRequestId(otpReqId);
                vDe.setServiceName("Initiate-Forget-Password-Profiling-Service");
                vDe.setUserId(userDetailsResult.getEmailAddress());
                vDe.setUserIdType("emailAddress");
                vDe.setProcessId(processId);
                verifyReqIdDetailsAuthRepo.save(vDe);
            }

            ProcessorUserHistoryInfo procSucessTrans = new ProcessorUserHistoryInfo("user-initiate-forget-password",
                    "Customer initiate change password was successful",
                    String.valueOf(GlobalMethods.generateTransactionId()),
                    userId, channel, "Processor-Channel");
            procTransRepo.save(procSucessTrans);

            responseModel.setDescription("Customer initiate change password was successful");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            log.error("", ex);
        }
        return responseModel;
    }

    public BaseResponse changePassword(ChangePasswordRequest rq, String channel) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";

        try {
            statusCode = 400;

            //   try {
            System.out.println("ChangePasswordRequest :::::  " + new Gson().toJson(rq));
            OtpValidateRequest request1 = new OtpValidateRequest();
            request1.setOtp(rq.getOtp());
            request1.setRequestId(rq.getRequestId());

            System.out.println(" validateOtp req :::::  " + new Gson().toJson(request1));

            BaseResponse bRes = utilitiesProxy.validateOtp(request1);

            System.out.println("validateOtp bRes :::::  " + new Gson().toJson(bRes));

            if (bRes.getStatusCode() != HttpServletResponse.SC_OK) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "user-initiate-forget-password",
                        "User initiate chnage password failed, Phonenumber does not exist!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());
                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }
            /*} catch (NumberFormatException e) {
                responseModel.setDescription("Error validating OTP!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/

            if (!utilMeth.isPasswordValid(rq.getNewPassword())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Password is invalid!",
                        String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel,
                        "Profiling-Service");

                responseModel.setDescription("Password is invalid!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            String newPwd = utilMeth.encyrpt(rq.getNewPassword(), encryptionKey);
            List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByRequestId(rq.getRequestId());
            VerifyReqIdDetailsAuth reqIdAuthDetail = getInitAcPin.get(0);
            String userId = reqIdAuthDetail.getUserId();

            if (getInitAcPin.get(0).getUserIdType().equals("phoneNumber")) {
                List<RegWalletInfo> getWalletRecords = regWalletInfoRepo.findByPhoneNumberData(userId);
                if (getWalletRecords != null && !getWalletRecords.isEmpty()) {
                    RegWalletInfo resultWallet = getWalletRecords.get(0);

                    if (newPwd.equals(resultWallet.getPassword())) {
                        ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                                "change-password",
                                "Please, use a new password!",
                                String.valueOf(GlobalMethods.generateTransactionId()),
                                "",
                                channel, "Profiling-Service");
                        responseModel.setDescription("Please, use a new password!");
                        responseModel.setStatusCode(statusCode);

                        procFailedRepo.save(procFailedTrans);
                        return responseModel;
                    }

                    resultWallet.setPassword(newPwd);
                    regWalletInfoRepo.save(resultWallet);

                    UserDetails updateUser = userDeRepo.findByUniqueIdentification(userId);
                    updateUser.setPassword(newPwd);
                    userDeRepo.save(updateUser);

                    responseModel.setDescription("Password changed successfully.");
                    responseModel.setStatusCode(200);
                } else {
                    responseModel.setDescription("User has not been on-boarded!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            } else {
                Optional<UserDetails> getUserDetailsResult = userDeRepo.findByUserEmailId(userId);
                if (getUserDetailsResult == null || !getUserDetailsResult.isPresent()) {
                    responseModel.setDescription("User has not been on-boarded!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                UserDetails userDetailsResult = getUserDetailsResult.get();

                if (newPwd.equals(userDetailsResult.getPassword())) {
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "change-password",
                            "Please, use a new password!",
                            String.valueOf(GlobalMethods.generateTransactionId()),
                            "",
                            channel, "Profiling-Service");
                    responseModel.setDescription("Please, use a new password!");
                    responseModel.setStatusCode(statusCode);

                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                }
                log.info("registerUser ::::: getInitAcPin.get(0).getUserId() :::::::: {} ", userId);

                userDetailsResult.setPassword(newPwd);
                log.info("usertype is email, change password for::: userDetailsResult.getUniqueIdentification() :::::::::::: {}",
                        userDetailsResult.getUniqueIdentification());

                List<RegWalletInfo> getWalletRecords = regWalletInfoRepo
                        .findByPhoneNumberData(userDetailsResult.getUniqueIdentification());
                if (getWalletRecords == null || getWalletRecords.isEmpty()) {
                    responseModel.setDescription("User has not been on-boarded!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                RegWalletInfo resultWallet = getWalletRecords.get(0);

                if (newPwd.equals(resultWallet.getPassword())) {
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "change-password", "Please, use a new password!",
                            String.valueOf(GlobalMethods.generateTransactionId()), "", channel,
                            "Profiling-Service");

                    responseModel.setDescription("Please, use a new password!");
                    responseModel.setStatusCode(statusCode);

                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                }

                resultWallet.setPassword(newPwd);
                regWalletInfoRepo.save(resultWallet);

                userDeRepo.save(userDetailsResult);

                responseModel.setDescription("Password changed successfully.");
                responseModel.setStatusCode(200);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            log.error("error while executing changePassword: reason::", ex);
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            return responseModel;
        }
        return responseModel;
    }

    public BaseResponse changePasswordInApp(ChangePasswordInApp rq, String channel, String auth) throws UnsupportedEncodingException {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";

        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String email = getDecoded.getEmailAddress();

            if (!rq.getEmailAddress().equals(email)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Suspected fraud!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");
                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> wallDe = regWalletInfoRepo.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!wallDe.get(0).isActivation()) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Customer has not created PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!wallDe.get(0).getUuid().equals(rq.getUuid())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Invalid device!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");
                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid device!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = wallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Invalid PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);

                return responseModel;

            }

            String encyrptedPwd = utilMeth.encyrpt(String.valueOf(rq.getOldPassword()), encryptionKey);
            String pwd = wallDe.get(0).getPassword();
            if (!encyrptedPwd.equals(pwd)) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Invalid Old Password!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid Old Password!");
                responseModel.setStatusCode(statusCode);

                return responseModel;

            }

            if (!utilMeth.isPasswordValid(rq.getNewPassword())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-password",
                        "Password is invalid!",
                        String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel,
                        "Profiling-Service");

                responseModel.setDescription("Password is invalid!");
                responseModel.setStatusCode(statusCode);

                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            String newPwd = utilMeth.encyrpt(rq.getNewPassword(), encryptionKey);

            List<RegWalletInfo> getWalletRecords = regWalletInfoRepo.findByPhoneNumberData(getDecoded.phoneNumber);
            if (getWalletRecords != null && !getWalletRecords.isEmpty()) {
                RegWalletInfo resultWallet = getWalletRecords.get(0);

                if (newPwd.equals(resultWallet.getPassword())) {
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "change-password",
                            "Please, use a new password!",
                            String.valueOf(GlobalMethods.generateTransactionId()),
                            "",
                            channel, "Profiling-Service");
                    responseModel.setDescription("Please, use a new password!");
                    responseModel.setStatusCode(statusCode);

                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                }

                resultWallet.setPassword(newPwd);
                regWalletInfoRepo.save(resultWallet);

                UserDetails updateUser = userDeRepo.findByUniqueIdentification(getWalletRecords.get(0).getPhoneNumber());
                updateUser.setPassword(newPwd);
                userDeRepo.save(updateUser);

                responseModel.setDescription("Password changed successfully.");
                responseModel.setStatusCode(200);
            } else {
                responseModel.setDescription("User has not been on-boarded!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            log.error("error while executing changePassword: reason::", ex);
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            return responseModel;
        }
        return responseModel;
    }

    public BaseResponse changePinInApp(ChangePinInApp rq, String channel, String auth) throws UnsupportedEncodingException {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";

        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String email = getDecoded.getEmailAddress();

            if (!rq.getEmailAddress().equals(email)) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-pin",
                        "Suspected fraud!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");
                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Suspected fraud!!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> wallDe = regWalletInfoRepo.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!wallDe.get(0).isActivation()) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-pin",
                        "Customer has not created PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            if (!wallDe.get(0).getUuid().equals(rq.getUuid())) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-pin",
                        "Invalid device!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");
                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid device!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getOldPin()), encryptionKey);
            String pin = wallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "change-pin",
                        "Invalid PIN!",
                        String.valueOf(GlobalMethods.generateTransactionId()), "", channel, "Processor-Channel");

                procFailedRepo.save(procFailedTrans);
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);

                return responseModel;

            }

            String newPin = utilMeth.encyrpt(String.valueOf(rq.getNewPin()), encryptionKey);

            List<RegWalletInfo> getWalletRecords = regWalletInfoRepo.findByPhoneNumberData(getDecoded.phoneNumber);
            if (getWalletRecords != null && !getWalletRecords.isEmpty()) {
                RegWalletInfo resultWallet = getWalletRecords.get(0);

                if (newPin.equals(resultWallet.getPersonId())) {
                    ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                            "change-pin",
                            "Please, use a new pin!",
                            String.valueOf(GlobalMethods.generateTransactionId()),
                            "",
                            channel, "Profiling-Service");
                    responseModel.setDescription("Please, use a new pin!");
                    responseModel.setStatusCode(statusCode);

                    procFailedRepo.save(procFailedTrans);
                    return responseModel;
                }

                resultWallet.setPersonId(newPin);
                regWalletInfoRepo.save(resultWallet);

                responseModel.setDescription("Pin changed successfully.");
                responseModel.setStatusCode(200);
            } else {
                responseModel.setDescription("User has not been on-boarded!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            log.error("error while executing changePassword: reason::", ex);
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            return responseModel;
        }
        return responseModel;
    }

    public BaseResponse resendOtpNew(OtpResendRequest rq, String channel) {

        log.info("resendOtpNew :: OtpResendRequest: {}  ::::::::::::::::::::: ", rq);

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again";
        try {
            statusCode = 400;

            ReqRequestId reRe = new ReqRequestId();
            reRe.setRequestId(rq.getRequestId());
            BaseResponse bResId = this.getOtpByRequestIdExist(reRe);

            if (bResId.getStatusCode() != 200) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "resend-otp",
                        bResId.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel,
                        "Profiling-Service");

                responseModel.setDescription(bResId.getDescription());
                responseModel.setStatusCode(bResId.getStatusCode());

                procFailedRepo.save(procFailedTrans);
                return responseModel;
            }

            String userId = (String) bResId.getData().get("userId");
            OtpRequest otp = new OtpRequest();
            otp.setEmailAddress(userId);
            otp.setUserId(userId);
            otp.setServiceName("Resend-Send-Otp");
            otp.setResend("1");
            otp.setRequestId(rq.getRequestId());

            log.info("profiling sms otp req sendoTPSMSOnly with parameters: {}::::::: ", otp);

            BaseResponse bRes = utilitiesProxy.sendoTPSMSOnly(otp);

            if (bRes.getStatusCode() != 200) {
                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "resend-otp",
                        bRes.getDescription(),
                        String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel, "Profiling-Service");

                responseModel.setDescription(bRes.getDescription());
                responseModel.setStatusCode(bRes.getStatusCode());

                procFailedRepo.save(procFailedTrans);
                return responseModel;

            }
            String otpReqId = (String) bRes.getData().get("requestId");
            responseModel.addData("requestId", otpReqId);
            responseModel.setStatusCode(bRes.getStatusCode());
            responseModel.setDescription(OTP_SUCCESSFULLY_SENT);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            log.error("exception in resendOtpNew : Reason:::", ex);
        }
        return responseModel;
    }

    public BaseResponse getOtpByRequestIdExist(ReqRequestId rq) {
        BaseResponse baseResponse = new BaseResponse();
        List<Otp> otp = otpRepository.findByReqId(rq.getRequestId());
        if (otp.size() <= 0) {
            baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            baseResponse.setDescription(REQUEST_ID_INVALID);
            return baseResponse;
        }
        // long nowMillis = System.currentTimeMillis();
        if (!otp.get(0).isUsed()) {
            baseResponse.addData("userId", otp.get(0).getUserId());
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            baseResponse.setDescription(REQUEST_ID_INVALID);
        }
        return baseResponse;
    }

    private String toPlain(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).toPlainString();
    }

    private String escrowBalance(String email, String currency) {
        if (email == null || email.isEmpty() || currency == null || currency.isEmpty()) {
            return "0";
        }
        BigDecimal sum = walletTransactionsDetailsRepo
                .sumEscrowAvailableByEmailAndCurrency(email, currency);
        return toPlain(sum);
    }

    private String computeTotalAccuredAmount(ComputeInvestmentBalance rq) {

        System.out.println("computeTotalAccuredAmount req  ::::: " + new Gson().toJson(rq));

        //Get data
        // BigDecimal principal = BigDecimal.ZERO;
        List<InvestmentOrder> getData = investmentOrderRepository
                .findByEmailAdressWaaletIdCurrencyStatus(
                        rq.getEmail(),
                        rq.getWalletId(),
                        rq.getCurrencyCode(),
                        InvestmentOrderStatus.ACTIVE
                );

        BigDecimal totalAccruedInterest = BigDecimal.ZERO;

        /*if (getData.size() <= 0) {
            principal = BigDecimal.ZERO;
        }*/
        // 2) Sum accrued interest from positions for same email + currency
        // You currently have: findByEmailAddress(...)
        List<InvestmentOrder> orders = investmentOrderRepository.findByEmailAddress(rq.getEmail());

        if (orders != null && !orders.isEmpty()) {
            for (InvestmentOrder ord : orders) {

                InvestmentProduct product = ord.getProduct();
                if (product == null) {
                    continue;
                }

                // Match only this currency
                if (!rq.getCurrencyCode().equalsIgnoreCase(product.getCurrency())) {
                    continue;
                }

                // (Optional) If you only want active orders, add:
                if (ord.getStatus() != InvestmentOrderStatus.ACTIVE) {
                    continue;
                }

                // Find linked position by orderRef
                Optional<InvestmentPosition> posOpt = investmentPositionRepository.findByOrderRef(ord.getOrderRef());
                if (!posOpt.isPresent()) {
                    continue;
                }

                InvestmentPosition pos = posOpt.get();
                BigDecimal accrued = pos.getTotalAccruedInterest();
                if (accrued != null) {
                    // BigDecimal is immutable – you MUST reassign
                    totalAccruedInterest = totalAccruedInterest.add(accrued);
                }
            }
        }

        // System.out.println("Total principal              ::::: " + principal);
        System.out.println("Total accrued interest       ::::: " + totalAccruedInterest);

        BigDecimal finalTotal = totalAccruedInterest;

        // Normalise to 2dp (or whatever you prefer)
        finalTotal = finalTotal.setScale(2, RoundingMode.HALF_UP);

        return finalTotal.toPlainString();
    }

    private String computeInvestmentBalance(ComputeInvestmentBalance rq) {

        System.out.println("ComputeInvestmentBalance req  ::::: " + new Gson().toJson(rq));

        // 1) Sum principal (capital) from orders – handle null safely
        BigDecimal principal = investmentOrderRepository
                .sumInvestmentBalanceByEmailWalletAndCurrency(
                        rq.getEmail(),
                        rq.getWalletId(),
                        rq.getCurrencyCode(),
                        InvestmentOrderStatus.ACTIVE
                );

        if (principal == null) {
            principal = BigDecimal.ZERO;
        }

        // 2) Sum accrued interest from positions for same email + currency
        BigDecimal totalAccruedInterest = BigDecimal.ZERO;

        // You currently have: findByEmailAddress(...)
        List<InvestmentOrder> orders = investmentOrderRepository.findByEmailAddress(rq.getEmail());

        if (orders != null && !orders.isEmpty()) {
            for (InvestmentOrder ord : orders) {

                InvestmentProduct product = ord.getProduct();
                if (product == null) {
                    continue;
                }

                // Match only this currency
                if (!rq.getCurrencyCode().equalsIgnoreCase(product.getCurrency())) {
                    continue;
                }

                // (Optional) If you only want active orders, add:
                if (ord.getStatus() != InvestmentOrderStatus.ACTIVE) {
                    continue;
                }

                // Find linked position by orderRef
                Optional<InvestmentPosition> posOpt = investmentPositionRepository.findByOrderRef(ord.getOrderRef());
                if (!posOpt.isPresent()) {
                    continue;
                }

                InvestmentPosition pos = posOpt.get();
                BigDecimal accrued = pos.getTotalAccruedInterest();
                if (accrued != null) {
                    // BigDecimal is immutable – you MUST reassign
                    totalAccruedInterest = totalAccruedInterest.add(accrued);
                }
            }
        }

        System.out.println("Total principal              ::::: " + principal);
        System.out.println("Total accrued interest       ::::: " + totalAccruedInterest);

        BigDecimal finalTotal = principal.add(totalAccruedInterest);

        // Normalise to 2dp (or whatever you prefer)
        finalTotal = finalTotal.setScale(2, RoundingMode.HALF_UP);

        return finalTotal.toPlainString();
    }

    public ApiResponseModel getCustomerDetailsWorkingold(String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";

        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            // 1) Fetch main customer
            List<RegWalletInfo> getCus = regWalletInfoRepo.findByPhoneNumberData(getDecoded.phoneNumber);
            if (getCus == null || getCus.isEmpty()) {

                ProcessorUserFailedTransInfo procFailedTrans = new ProcessorUserFailedTransInfo(
                        "resend-otp",
                        "Wallet to Wallet transfer, Customer is invalid!",
                        String.valueOf(GlobalMethods.generateTransactionId()),
                        "", channel, "Profiling-Service");
                procFailedRepo.save(procFailedTrans);

                responseModel.setDescription("Wallet to Wallet transfer, Customer is invalid!");
                responseModel.setStatusCode(statusCode);
                responseModel.setData(java.util.Collections.emptyList()); // ← ALWAYS an array
                return responseModel;
            }

            RegWalletInfo main = getCus.get(0);
            String emailValidation = main.isEmailVerification() ? "1" : "0";
            String pinCreation = main.isActivation() ? "1" : "0";
            String walletNo = main.getPhoneNumber();
            String walletId = main.getWalletId();

            // 2) Virtual account info
            String virtAcctNo = null, virtAcctName = null, virtAcctType = null;
            List<CreateVirtualAcctSucc> getVirt = createVirtualAcctSuccRepo.findByWallettNoList(walletNo);
            if (getVirt != null && !getVirt.isEmpty()) {
                CreateVirtualAcctSucc v = getVirt.get(0);
                virtAcctNo = v.getAccountNumber() == null ? "" : v.getAccountNumber();
                virtAcctName = v.getAccountName() == null ? "" : v.getAccountName();
                virtAcctType = v.getAccountType() == null ? "" : v.getAccountType();
            }
            String cusName = (virtAcctName != null && !virtAcctName.isEmpty())
                    ? virtAcctName
                    : (String.valueOf(main.getFirstName()) + " " + String.valueOf(main.getLastName())).trim();

            // 3) Primary account balance (by phone number)
            BaseResponse balRes = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(getDecoded.phoneNumber);
            BigDecimal acctBal = toBigDecimalSafe(balRes != null && balRes.getData() != null
                    ? balRes.getData().get("accountBalance") : null);
            String balance = acctBal.toString();
            String bookBalance = null;
            String merchantBookedBalance = null;

            // 4) Limits & tier for primary
            String customerTier = "0";
            String customerMaxBal = "0";
            String dailyLimit = "0";
            String singleTransactionLimit = "0";

            List<UserLimitConfig> userLimit = userLimitConfigRepo.findByWalletNumber(getDecoded.phoneNumber);
            if (userLimit != null && !userLimit.isEmpty()) {
                List<GlobalLimitConfig> g = globalLimitConfigRepo.findByLimitCategory(userLimit.get(0).getTierCategory());
                if (g != null && !g.isEmpty()) {
                    GlobalLimitConfig gl = g.get(0);
                    customerTier = nv(gl.getCategory());
                    customerMaxBal = nv(gl.getMaximumBalance());
                    dailyLimit = nv(gl.getDailyLimit());
                    singleTransactionLimit = nv(gl.getSingleTransactionLimit());
                }
            }

            List<RegWalletCheckLog> nameLookup = regWalletCheckLogRepo.findByPhoneNumberList(getDecoded.phoneNumber);
            String dailyCum = (nameLookup != null && !nameLookup.isEmpty() && nameLookup.get(0).getWalletTransferCumm() != null)
                    ? nameLookup.get(0).getWalletTransferCumm() : "0";

            String dailyLimitBalance = safeBigDecimal(dailyLimit).subtract(safeBigDecimal(dailyCum)).toString();
            String usedDailyLimitBalance = dailyCum;

            // 5) Build array result
            List<GetCustomerDetails> allDetails = new java.util.ArrayList<>();

            // Primary details
            GetCustomerDetails primary = new GetCustomerDetails();
            primary.setAccountBalance(balance);
            primary.setBookAccountBalance(bookBalance);
            primary.setCustomerName(cusName);
            primary.setCustomerTier(customerTier);
            primary.setDailyLimit(dailyLimit);
            primary.setDailyLimitBalance(dailyLimitBalance);
            primary.setEmailAddressValidation(emailValidation);
            primary.setMaxAccountBalance(customerMaxBal);
            primary.setPinCreatedValidation(pinCreation);
            primary.setVirtualAccount(virtAcctNo);
            primary.setVirtualAccountName(virtAcctName);
            primary.setVirtualAccountType(virtAcctType);
            primary.setWalletNo(walletNo);
            primary.setUsedDailyLimitBalance(usedDailyLimitBalance);
            primary.setSingleTransactionLimit(singleTransactionLimit);
            primary.setMerchantBookedAccountBalance(merchantBookedBalance);
            primary.setWalletId(walletId);
            primary.setCurrencyCode(utilMeth.returnSETTING_ONBOARDING_DEFAULT_CURRENCY_CODE());
            primary.setEscrowBalance(
                    escrowBalance(getDecoded.emailAddress, primary.getCurrencyCode())
            );

            String email = getCus.get(0).getEmail();
            String walletIdds = getCus.get(0).getWalletId();
            String currCode = primary.getCurrencyCode();
            ComputeInvestmentBalance rqq = new ComputeInvestmentBalance();
            rqq.setCurrencyCode(currCode);
            rqq.setEmail(email);
            rqq.setWalletId(walletId);
            primary.setInvestmentBalance(computeInvestmentBalance(rqq));
            System.out.println("primary.getInvestmentBalance()  " + "::::: " + primary.getInvestmentBalance());

            allDetails.add(primary);

            // 6) Additional accounts by email
            List<AddAccountDetails> extraAccounts = addAccountDetailsRepo.findByEmailAddress(getDecoded.emailAddress);
            if (extraAccounts != null && !extraAccounts.isEmpty()) {
                for (AddAccountDetails addAcc : extraAccounts) {

                    String addWalletNo = addAcc.getAccountNumber() != null ? addAcc.getAccountNumber() : walletNo;
                    String addWalletId = addAcc.getWalletId() != null ? addAcc.getWalletId() : walletId;
                    String addName = addAcc.getVirtualAccountName() != null ? addAcc.getVirtualAccountName() : cusName;
                    String virtAcctNumber = addAcc.getVirtualAccountNumber() == null ? "" : addAcc.getVirtualAccountNumber();
                    String currencyCode = addAcc.getCurrencyCode(); // may be null

                    BaseResponse addBalRes = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(addWalletNo);
                    BigDecimal addBal = toBigDecimalSafe(addBalRes != null && addBalRes.getData() != null
                            ? addBalRes.getData().get("accountBalance") : null);

                    GetCustomerDetails extra = new GetCustomerDetails();
                    extra.setCurrencyCode(currencyCode != null ? currencyCode : utilMeth.returnSETTING_ONBOARDING_DEFAULT_CURRENCY_CODE());
                    extra.setEscrowBalance(
                            escrowBalance(getDecoded.emailAddress, extra.getCurrencyCode())
                    );

                    ComputeInvestmentBalance rqq2 = new ComputeInvestmentBalance();
                    rqq2.setCurrencyCode(currCode);
                    rqq2.setEmail(addAcc.getEmailAddress());
                    rqq2.setWalletId(addAcc.getWalletId());

                    extra.setInvestmentBalance(computeInvestmentBalance(rqq2));

                    System.out.println("extra.getInvestmentBalance()  " + "::::: " + extra.getInvestmentBalance());

                    extra.setAccountBalance(addBal.toString());
                    extra.setBookAccountBalance(null);
                    extra.setCustomerName(addName);
                    // If tiers/limits vary per account, replace these with per-wallet lookups:
                    extra.setCustomerTier(customerTier);
                    extra.setDailyLimit(dailyLimit);
                    extra.setDailyLimitBalance(dailyLimitBalance);
                    extra.setEmailAddressValidation(emailValidation);
                    extra.setMaxAccountBalance(customerMaxBal);
                    extra.setPinCreatedValidation(pinCreation);

                    if (!virtAcctNumber.isEmpty()) {
                        extra.setVirtualAccount(virtAcctNumber);
                        // Set the *name* correctly (was previously set from number)
                        extra.setVirtualAccountName(addAcc.getVirtualAccountName());
                    } else {
                        extra.setVirtualAccount(null);
                        extra.setVirtualAccountName(null);
                    }
                    // You used currency code as virtualAccountType — keeping that behavior:
                    extra.setVirtualAccountType(currencyCode);

                    extra.setWalletNo(addWalletNo);
                    extra.setUsedDailyLimitBalance(usedDailyLimitBalance);
                    extra.setSingleTransactionLimit(singleTransactionLimit);
                    extra.setMerchantBookedAccountBalance(null);
                    extra.setWalletId(addWalletId);

                    allDetails.add(extra);
                }
            }

            responseModel.setDescription("Customer details retrieved successfully.");
            responseModel.setStatusCode(200);
            responseModel.setData(allDetails); // ← ALWAYS an array

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            responseModel.setData(java.util.Collections.emptyList()); // ← keep array shape on errors too
        }
        return responseModel;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public ApiResponseModel getCustomerDetailsWorking(String channel, String auth) {

        ApiResponseModel response = new ApiResponseModel();
        int failCode = 400;

        try {
            DecodedJWTToken decoded = DecodedJWTToken.getDecoded(auth);

            // ===================== 1) MAIN CUSTOMER ==========================
            List<RegWalletInfo> list = regWalletInfoRepo.findByPhoneNumberData(decoded.phoneNumber);
            if (list == null || list.isEmpty()) {
                recordFailedTrans(channel);
                return error(response, failCode, "Wallet to Wallet transfer, Customer is invalid!");
            }

            RegWalletInfo main = list.get(0);
            String customerEmail = main.getEmail();
            String primaryWalletNo = main.getPhoneNumber();
            String primaryWalletId = main.getWalletId();
            String emailValidation = main.isEmailVerification() ? "1" : "0";
            String pinCreated = main.isActivation() ? "1" : "0";

            // ===================== 2) VIRTUAL ACCOUNT =========================
            CreateVirtualAcctSucc virt = createVirtualAcctSuccRepo.findByWalletNo(primaryWalletNo).orElse(null);

            String virtAcctNo = virt != null ? nv(virt.getAccountNumber(), null) : null;
            String virtAcctName = virt != null ? nv(virt.getAccountName(), null) : null;
            String virtAcctType = virt != null ? nv(virt.getAccountType(), null) : null;

            String customerName
                    = !isEmpty(virtAcctName)
                    ? virtAcctName
                    : (main.getFirstName() + " " + main.getLastName()).trim();

            // ===================== 3) ACCOUNT BALANCE =========================
            BaseResponse balRes = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(primaryWalletNo);
            BigDecimal primaryBalance
                    = toBigDecimalSafe(balRes != null && balRes.getData() != null
                            ? balRes.getData().get("accountBalance")
                            : null);

            // ===================== 4) LIMITS =========================
            LimitInfo limits = getLimitInfo(primaryWalletNo);

            // ===================== 5) RESULT LIST =========================
            List<GetCustomerDetails> results = new ArrayList<>();

            // ===================== PRIMARY WALLET DTO =========================
            GetCustomerDetails primary = new GetCustomerDetails();
            String primaryCurrency = utilMeth.returnSETTING_ONBOARDING_DEFAULT_CURRENCY_CODE();

            primary.setCustomerName(customerName);
            primary.setWalletNo(primaryWalletNo);
            primary.setWalletId(primaryWalletId);
            primary.setAccountBalance(primaryBalance.toString());

            primary.setCustomerTier(limits.tier);
            primary.setDailyLimit(limits.dailyLimit);
            primary.setDailyLimitBalance(limits.dailyLimitBalance);
            primary.setUsedDailyLimitBalance(limits.usedDailyLimitBalance);
            primary.setMaxAccountBalance(limits.maxBalance);
            primary.setSingleTransactionLimit(limits.singleTxnLimit);

            primary.setEmailAddressValidation(emailValidation);
            primary.setPinCreatedValidation(pinCreated);

            primary.setVirtualAccount(virtAcctNo);
            primary.setVirtualAccountName(virtAcctName);
            primary.setVirtualAccountType(virtAcctType);

            primary.setCurrencyCode(primaryCurrency);
            primary.setEscrowBalance(escrowBalance(customerEmail, primaryCurrency));

            // ---- Correct investment balance for PRIMARY wallet ----
            ComputeInvestmentBalance rq = new ComputeInvestmentBalance();
            rq.setCurrencyCode(primaryCurrency);
            rq.setEmail(customerEmail);
            rq.setWalletId(primaryWalletId);
            System.out.println("Calling ComputeInvestmentBalance primary  " + "::::: " + new Gson().toJson(rq));
            primary.setInvestmentBalance(computeInvestmentBalance(rq));
            System.out.println("Calling ComputeTotalAccuredAmount primary  " + "::::: " + new Gson().toJson(rq));

            primary.setTotalAccruedInterest(computeTotalAccuredAmount(rq));

            results.add(primary);

            // ===================== 6) EXTRA WALLETS =========================
            List<AddAccountDetails> extras = addAccountDetailsRepo.findByEmailAddress(customerEmail);

            if (extras != null && !extras.isEmpty()) {

                for (AddAccountDetails acc : extras) {

                    String extraWalletNo = nv(acc.getAccountNumber(), primaryWalletNo);
                    String extraWalletId = nv(acc.getWalletId(), primaryWalletId);
                    String extraCurrency = nv(acc.getCurrencyCode(), primaryCurrency);
                    String extraVirtAcct = nv(acc.getVirtualAccountNumber(), null);
                    String extraName = nv(acc.getVirtualAccountName(), customerName);

                    BaseResponse balExtra = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(extraWalletNo);
                    BigDecimal extraBalance
                            = toBigDecimalSafe(balExtra != null && balExtra.getData() != null
                                    ? balExtra.getData().get("accountBalance")
                                    : null);

                    GetCustomerDetails dto = new GetCustomerDetails();
                    dto.setCustomerName(extraName);
                    dto.setWalletNo(extraWalletNo);
                    dto.setWalletId(extraWalletId);
                    dto.setAccountBalance(extraBalance.toString());

                    dto.setCustomerTier(limits.tier);
                    dto.setDailyLimit(limits.dailyLimit);
                    dto.setDailyLimitBalance(limits.dailyLimitBalance);
                    dto.setUsedDailyLimitBalance(limits.usedDailyLimitBalance);
                    dto.setMaxAccountBalance(limits.maxBalance);
                    dto.setSingleTransactionLimit(limits.singleTxnLimit);

                    dto.setEmailAddressValidation(emailValidation);
                    dto.setPinCreatedValidation(pinCreated);

                    dto.setVirtualAccount(extraVirtAcct);
                    dto.setVirtualAccountName(extraName);
                    dto.setVirtualAccountType(extraCurrency);

                    dto.setCurrencyCode(extraCurrency);
                    dto.setEscrowBalance(escrowBalance(customerEmail, extraCurrency));

                    // ---- CORRECT investment balance per wallet & currency ----
                    ComputeInvestmentBalance rq2 = new ComputeInvestmentBalance();
                    rq2.setCurrencyCode(extraCurrency);
                    rq2.setEmail(acc.getEmailAddress());
                    rq2.setWalletId(extraWalletId);
                    System.out.println("Calling ComputeInvestmentBalance rq2  " + "::::: " + new Gson().toJson(rq2));
                    dto.setInvestmentBalance(computeInvestmentBalance(rq2));
                    System.out.println("Calling ComputeTotalAccuredAmount: rq2 " + "::::: " + new Gson().toJson(rq2));

                    dto.setTotalAccruedInterest(computeTotalAccuredAmount(rq2));

                    results.add(dto);
                }
            }

            // ===================== SUCCESS =========================
            response.setDescription("Customer details retrieved successfully.");
            response.setStatusCode(200);
            response.setData(results);
            return response;

        } catch (Exception ex) {
            ex.printStackTrace();
            return error(response, failCode, "An error occurred, please try again");
        }
    }

    private LimitInfo getLimitInfo(String walletNo) {

        LimitInfo info = new LimitInfo();
        info.tier = "0";
        info.maxBalance = "0";
        info.dailyLimit = "0";
        info.singleTxnLimit = "0";
        info.dailyLimitBalance = "0";
        info.usedDailyLimitBalance = "0";

        try {
            List<UserLimitConfig> userLimit = userLimitConfigRepo.findByWalletNumber(walletNo);
            if (userLimit != null && !userLimit.isEmpty()) {

                String tierCategory = userLimit.get(0).getTierCategory();
                List<GlobalLimitConfig> gl = globalLimitConfigRepo.findByLimitCategory(tierCategory);

                if (gl != null && !gl.isEmpty()) {
                    GlobalLimitConfig g = gl.get(0);
                    info.tier = nv(g.getCategory(), "0");
                    info.maxBalance = nv(g.getMaximumBalance(), "0");
                    info.dailyLimit = nv(g.getDailyLimit(), "0");
                    info.singleTxnLimit = nv(g.getSingleTransactionLimit(), "0");
                }
            }

            List<RegWalletCheckLog> logs = regWalletCheckLogRepo.findByPhoneNumberList(walletNo);
            if (logs != null && !logs.isEmpty()) {
                String used = logs.get(0).getWalletTransferCumm();
                info.usedDailyLimitBalance = nv(used, "0");

                BigDecimal dLimit = toBigDecimalSafe(info.dailyLimit);
                BigDecimal usedLimit = toBigDecimalSafe(info.usedDailyLimitBalance);

                info.dailyLimitBalance = dLimit.subtract(usedLimit).toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static class LimitInfo {

        String tier;
        String maxBalance;
        String dailyLimit;
        String singleTxnLimit;
        String dailyLimitBalance;
        String usedDailyLimitBalance;
    }

    private ApiResponseModel error(ApiResponseModel resp, int statusCode, String description) {
        resp.setStatusCode(statusCode);
        resp.setDescription(description);
        resp.setData(Collections.emptyList());  // always return an array structure
        return resp;
    }

    private void recordFailedTrans(String channel) {

        ProcessorUserFailedTransInfo failed = new ProcessorUserFailedTransInfo(
                "resend-otp",
                "Wallet to Wallet transfer, Customer is invalid!",
                String.valueOf(GlobalMethods.generateTransactionId()),
                "",
                channel,
                "Profiling-Service"
        );

        procFailedRepo.save(failed);
    }

    private String nv(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    /**
     * Null-to-empty helper for Strings.
     */
    private static String nv(String s) {
        return (s == null) ? "" : s;
    }

    private BigDecimal nv(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Parse possibly-null/blank numeric Strings safely to BigDecimal (default
     * 0).
     */
    private static BigDecimal safeBigDecimal(String s) {
        if (s == null || s.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (Exception ignore) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Safely parse heterogeneous balance values to BigDecimal.
     */
    private static BigDecimal toBigDecimalSafe(Object amountObj) {
        if (amountObj == null) {
            return BigDecimal.ZERO;
        }
        if (amountObj instanceof BigDecimal) {
            return (BigDecimal) amountObj;
        }
        if (amountObj instanceof Number) {
            return BigDecimal.valueOf(((Number) amountObj).doubleValue());
        }
        if (amountObj instanceof String) {
            try {
                return new BigDecimal((String) amountObj);
            } catch (Exception ignore) {
            }
        }
        return BigDecimal.ZERO;
    }

}
