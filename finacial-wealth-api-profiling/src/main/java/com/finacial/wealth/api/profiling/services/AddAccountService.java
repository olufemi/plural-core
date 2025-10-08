/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.breezpay.virt.acct.details.CreatNigeriaAccount;
import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumberReq;
import com.finacial.wealth.api.profiling.client.model.WalletSystemResponse;
import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.AddFailedTransLog;
import com.finacial.wealth.api.profiling.domain.Countries;
import com.finacial.wealth.api.profiling.domain.GenerateVirtAcctNumb;
import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.VerifyReqIdDetailsAuth;
import com.finacial.wealth.api.profiling.models.accounts.AddAccountObj;
import com.finacial.wealth.api.profiling.models.accounts.ValidationResponse;
import com.finacial.wealth.api.profiling.proxies.BreezePayVirtAcctProxy;
import com.finacial.wealth.api.profiling.proxies.UtilitiesProxy;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.AddFailedTransLoggRepo;
import com.finacial.wealth.api.profiling.repo.CountriesRepository;
import com.finacial.wealth.api.profiling.repo.GenerateVirtAcctNumbRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.repo.VerifyReqIdDetailsAuthRepo;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utilities.models.OtpValidateRequest;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class AddAccountService {

    private final Logger logger = LoggerFactory.getLogger(AddAccountService.class);

    private final AddFailedTransLoggRepo addFailedTransLoggRepo;
    private final CountryService countryService;
    private final CountriesRepository countriesRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final UniqueIdService uniqueIds;
    private final WalletServices walletServices;
    private final BreezePayVirtAcctProxy breezePayVirtAcctProxy;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final GenerateVirtAcctNumbRepo generateVirtAcctNumbRepo;
    private final UttilityMethods uttilityMethods;
    private final VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo;
    private final UtilitiesProxy utilitiesProxy;

    private static final int STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE = 58;
    private static final String STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION = "Please validate bvn";

    //${fin.wealth.breeze.pay.base.url}
    @Value("${fin.wealth.breeze.pay.mer.id}")
    private String merchantId;
    @Value("${fin.wealth.breeze.pay.mer.code.id}")
    private String channelCode;
    @Value("${fin.wealth.breeze.pay.mer.auth}")
    private String authKey;

    @Value("${fin.wealth.breeze.pay.mer.sub.key}")
    private String subKey;
    private static final String SUCCESSFUL = "00";
    //
    @Value("${fin.wealth.breeze.pay.mer.req.authorizer}")
    private String reqAuthorizer;

    public AddAccountService(AddFailedTransLoggRepo addFailedTransLoggRepo,
            CountryService countryService,
            CountriesRepository countriesRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            UniqueIdService uniqueIds,
            WalletServices walletServices,
            BreezePayVirtAcctProxy breezePayVirtAcctProxy,
            RegWalletInfoRepository regWalletInfoRepository,
            GenerateVirtAcctNumbRepo generateVirtAcctNumbRepo, UttilityMethods uttilityMethods,
            VerifyReqIdDetailsAuthRepo verifyReqIdDetailsAuthRepo,
            UtilitiesProxy utilitiesProxy) {
        this.addFailedTransLoggRepo = addFailedTransLoggRepo;
        this.countryService = countryService;
        this.countriesRepository = countriesRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.uniqueIds = uniqueIds;
        this.walletServices = walletServices;
        this.breezePayVirtAcctProxy = breezePayVirtAcctProxy;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.generateVirtAcctNumbRepo = generateVirtAcctNumbRepo;
        this.uttilityMethods = uttilityMethods;
        this.verifyReqIdDetailsAuthRepo = verifyReqIdDetailsAuthRepo;
        this.utilitiesProxy = utilitiesProxy;
    }

    public BaseResponse addNigeriaAccountCallThirdPartyApi(CreatNigeriaAccount rq) {
        HttpStatus http = null;
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            //create virtual account 
            GenerateVirtualAccountNumberReq rqq = new GenerateVirtualAccountNumberReq();
            rqq.setBvn(rq.getBvn());
            rqq.setChannelCode(channelCode);
            rqq.setCurrency("NGN");
            rqq.setCustomerEmail(rq.getEmailAddress());
            rqq.setCustomerId(merchantId);
            rqq.setCustomerName(rq.getFullName());
            rqq.setCustomerPhone(rq.getPhoneNumber());
            rqq.setForceDebit("Y");
            rqq.setMerchantId(merchantId);
            rqq.setRequestAuthorizer(reqAuthorizer);
            GenerateVirtualAccountNumResponse genRess = breezePayVirtAcctProxy.generateVirtualAccount(rqq, authKey, subKey);

            if (!genRess.equals(SUCCESSFUL)) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        genRess.getResponseMessage(), "", "", rq.getEmailAddress());
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription(genRess.getResponseMessage());
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            GenerateVirtAcctNumb genLog = new GenerateVirtAcctNumb();
            genLog.setCreatedDate(Instant.now());
            genLog.setEmailAddress(rq.getEmailAddress());
            genLog.setExpiryDatetime(genRess.getResponseData().getExpiryDatetime());
            genLog.setResponseCode(genRess.getResponseCode());
            genLog.setResponseMessage(genRess.getResponseMessage());
            genLog.setVirtualAcctName(genRess.getResponseData().getVirtualAcctName());
            genLog.setVirtualAcctNo(genRess.getResponseData().getVirtualAcctNo());
            genLog.setWalletId(rq.getWalletId());
            generateVirtAcctNumbRepo.save(genLog);
            responseModel.setDescription(genRess.getResponseMessage());
            responseModel.setStatusCode(200);
            Map added = new HashMap();
            added.put("virtAccNo", genRess.getResponseData().getVirtualAcctNo());
            added.put("virtAccNoame", genRess.getResponseData().getVirtualAcctName());
            responseModel.setData(added);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                    http.INTERNAL_SERVER_ERROR.toString(), "", "", "");
            addFailedTransLoggRepo.save(pinActTransFailed);
            ex.printStackTrace();
        }

        return responseModel;

    }

    public BaseResponse addAccount(AddAccountObj rq, String auth) {
        HttpStatus http = null;
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {

            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            if (!uttilityMethods.isNumeric(rq.getPhoneNumber())) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Phonenumber is not numeric", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Phonenumber is not numeric");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (!uttilityMethods.isValid11Num(rq.getPhoneNumber())) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Phonenumber is not a valid phone number", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Phonenumber is not a valid phone number!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            //validate country/code
            ValidationResponse resp = countryService.validateCountryPair(rq.getCountryCode(), rq.getCountry());
            if (resp.getStatusCode() != 200) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        resp.getStatusDescription(), "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription(resp.getStatusDescription());
                responseModel.setStatusCode(resp.getStatusCode());

                return responseModel;
            }
            List<AddAccountDetails> getAcct = addAccountDetailsRepo.findByCountryCodeByEmailAddress(rq.getCountryCode(), emailAddress);
            if (getAcct.size() > 0) {

                Map addExit = new HashMap();
                addExit.put("accountNumber", getAcct.get(0).getAccountNumber());
                addExit.put("countryCode", getAcct.get(0).getCountryCode());
                addExit.put("countryName", getAcct.get(0).getCountryName());
                addExit.put("virtualAccountNumber", getAcct.get(0).getVirtualAccountNumber());
                addExit.put("virtualAccountName", getAcct.get(0).getVirtualAccountName());
                responseModel.setDescription("The account exists.");
                responseModel.setData(addExit);
                responseModel.setStatusCode(200);
                return responseModel;

            }

            String processId = rq.getRequestId() == null ? "0" : rq.getRequestId();
            logger.info("addAccount  {}  ::::::::::::::::::::: ", processId);

            List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByProcIdList(processId);

            if (getInitAcPin.size() <= 0) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Invalid process Id!", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);

                logger.info("  {}  ::::::::::::::::::::: ", "Invalid process Id");

                responseModel.setDescription(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION);
                responseModel.setStatusCode(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE);
                return responseModel;
            }

            if (getInitAcPin.get(0).getProcessIdUsed().equals("1")) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Transaction is already completed!", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);

                responseModel.setDescription("Transaction is already completed!");
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

            VerifyReqIdDetailsAuth updateVeri = getInitAcPin.get(0);
            updateVeri.setProcessIdUsed("0");
            updateVeri.setLastModifiedDate(Instant.now());
            updateVeri.setProcessId(processId);
            //updateVeri.setRequestId(otpReqId);
            verifyReqIdDetailsAuthRepo.save(updateVeri);

            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(rq.getWalletId());

            CreatNigeriaAccount cAcc = new CreatNigeriaAccount();
            cAcc.setBvn(rq.getBvn());
            cAcc.setCountryCode("NGN");
            cAcc.setEmailAddress(emailAddress);
            cAcc.setFullName(getRec.get().getFirstName());
            cAcc.setPhoneNumber(rq.getPhoneNumber());
            cAcc.setWalletId(rq.getWalletId());

            BaseResponse calThirdParty = this.addNigeriaAccountCallThirdPartyApi(cAcc);

            if (calThirdParty.getStatusCode() != 200) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        calThirdParty.getDescription(), "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Account creation failed, please try again!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            String virtAccNo = (String) calThirdParty.getData().get("virtAccNo");
            String virtName = (String) calThirdParty.getData().get("virtAccNoame");

            Optional<Countries> getCounByCode = countriesRepository.findByCountryCodeIgnoreCase(rq.getCountryCode());

            //generate unique phonenUmber;
            //generate unique walletId;
            //  throw new IllegalStateException("Failed to persist unique phoneNumber after retries");
            AddAccountDetails addDe = new AddAccountDetails();

            addDe.setAccountNumber(uniqueIds.nextUniquePhoneNumber());

            addDe.setCountryCode(getCounByCode.get().getCountryCode());
            addDe.setCountryName(getCounByCode.get().getCountry());
            addDe.setCreatedDate(Instant.now());
            addDe.setCurrencyName(getCounByCode.get().getCurrencyCode());
            addDe.setCurrencyCode(getCounByCode.get().getCurrencySymbol());
            addDe.setEmailAddress(emailAddress);
            addDe.setWalletId(uniqueIds.nextUniqueWalletId());
            addDe.setVirtualAccountNumber(virtAccNo);
            addDe.setPhoneNumber(rq.getPhoneNumber());
            addDe.setVirtualAccountName(virtName);

            //call add and account to wallet
            WalletSystemResponse addUserToWalletSystem = walletServices.addUserToWalletSystem(addDe.getAccountNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                return responseModel;

            }

            addAccountDetailsRepo.save(addDe);

            Map added = new HashMap();
            added.put("accountNumber", addDe.getAccountNumber());
            added.put("countryCode", addDe.getCountryCode());
            added.put("countryName", addDe.getCountryName());

            responseModel.setDescription("Account added successfully.");
            responseModel.setData(added);
            responseModel.setStatusCode(200);

            //
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                    http.INTERNAL_SERVER_ERROR.toString(), "", "", "");
            addFailedTransLoggRepo.save(pinActTransFailed);
            ex.printStackTrace();
        }

        return responseModel;
    }

}
