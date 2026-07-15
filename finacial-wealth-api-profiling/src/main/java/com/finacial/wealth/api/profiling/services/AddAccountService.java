/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.breezpay.virt.acct.details.CreatNigeriaAccount;
import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumberReq;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnLookup;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnLookupRepository;
import com.finacial.wealth.api.profiling.client.model.WalletSystemResponse;
import com.finacial.wealth.api.profiling.market.service.impl.MarketProfileSyncService;
import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.AddFailedTransLog;
import com.finacial.wealth.api.profiling.domain.Countries;
import com.finacial.wealth.api.profiling.domain.GenerateVirtAcctNumb;
import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.VerifyReqIdDetailsAuth;
import com.finacial.wealth.api.profiling.identity.IdentityFaceCompareRequest;
import com.finacial.wealth.api.profiling.identity.IdentityFaceCompareResponse;
import com.finacial.wealth.api.profiling.identity.IdentityFaceProxy;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessApiResponse;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyRequest;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyResponse;
import com.finacial.wealth.api.profiling.models.AddNewUserToLimit;
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
import com.finacial.wealth.api.profiling.utils.GlobalMethods;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final IdentityFaceProxy identityFaceProxy;
    @Autowired
    private MarketProfileSyncService marketProfileSyncService;

    private static final int STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE = 58;
    private static final String STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION = "Please validate BVN with OTP";
    private static final String BVN_FACE_PURPOSE = "NG_BVN_ACCOUNT_OPENING";
    private final BvnLookupRepository repo;

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

    @Value("${spring.profiles.active}")
    private String environment;
    @Value("${fin.wealth.goto.breeze}")
    private String gotoBreezeapay;
    @Value("${fin.wealth.identity.bvn.verification-mode:OTP_OR_FACE}")
    private String bvnVerificationMode;
    @Value("${fin.wealth.identity.face.minimum-score:0.85}")
    private BigDecimal faceMinimumScore;

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
            UtilitiesProxy utilitiesProxy, BvnLookupRepository repo,
            IdentityFaceProxy identityFaceProxy) {
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
        this.repo = repo;
        this.identityFaceProxy = identityFaceProxy;
    }

    public BaseResponse addNigeriaAccountCallThirdPartyApi(CreatNigeriaAccount rq) {
        HttpStatus http = null;
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            //create virtual account 
            System.out.println(" authKey :::::::::::::::: %S " + authKey);
            System.out.println(" subKey :::::::::::::::: %S " + subKey);
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
            System.out.println(" GenerateVirtualAccountNumberReq :::::::::::::::: %S " + new Gson().toJson(rqq));

            GenerateVirtualAccountNumResponse genRess = breezePayVirtAcctProxy.generateVirtualAccount(rqq, authKey, subKey);

            System.out.println(" GenerateVirtualAccountNumResponse :::::::::::::::: %S " + new Gson().toJson(rqq));

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
            rq.setCountryCode("NG");
            rq.setCountry("Nigeria");

            BvnLookup getBvnDe = repo.findByBvn(rq.getBvn()).orElse(null);

            if (getBvnDe == null) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Please validate BVN!", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Please validate BVN!");
                responseModel.setStatusCode(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE);

                return responseModel;
            }

            if (!uttilityMethods.isNumeric(getBvnDe.getPhoneNumber1())) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Phonenumber is not numeric", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Phonenumber is not numeric");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            if (!uttilityMethods.isValid11Num(getBvnDe.getPhoneNumber1())) {
                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                        "Phonenumber is not a valid phone number", "", "", emailAddress);
                addFailedTransLoggRepo.save(pinActTransFailed);
                responseModel.setDescription("Phonenumber is not a valid phone number!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(emailAddress);

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
                try {
                    if (getRec.isPresent()) {
                        marketProfileSyncService.syncNigeriaAccountProvisioned(getRec.get(), getAcct.get(0));
                    }
                } catch (Exception ex) {
                    logger.warn("Unable to sync NG_RETAIL market profile for existing account", ex);
                }
                return responseModel;

            }

            String processId = hasText(rq.getRequestId()) ? rq.getRequestId() : "0";
            logger.info("addAccount processId={} verificationMode={}", processId, bvnVerificationMode);

            BvnVerificationMode verificationMode = resolveVerificationMode();
            VerificationRequirements requirements = resolveVerificationRequirements(verificationMode, rq);
            if (!requirements.otpRequired && !requirements.faceRequired) {
                return failAddAccount(responseModel, "Please validate BVN with OTP", STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE, emailAddress);
            }

            VerifyReqIdDetailsAuth updateVeri = null;
            BaseResponse otpFailureResponse = null;
            if (requirements.otpRequired || requirements.allowEither) {
                List<VerifyReqIdDetailsAuth> getInitAcPin = verifyReqIdDetailsAuthRepo.findByProcIdList(processId);

                if (getInitAcPin.size() <= 0) {
                    logger.info("Invalid process Id");
                    if (!requirements.allowEither) {
                        return failAddAccount(responseModel, STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION,
                                STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE, emailAddress);
                    }
                    otpFailureResponse = new BaseResponse(STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE,
                            STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION);
                } else if ("1".equals(getInitAcPin.get(0).getProcessIdUsed())) {
                    if (!requirements.allowEither) {
                        return failAddAccount(responseModel, "Transaction is already completed!", statusCode, emailAddress);
                    }
                    otpFailureResponse = new BaseResponse(statusCode, "Transaction is already completed!");
                } else {
                    OtpValidateRequest request1 = new OtpValidateRequest();
                    request1.setOtp(rq.getOtp());
                    request1.setRequestId(rq.getRequestId());

                    BaseResponse bRes = utilitiesProxy.validateOtp(request1);

                    if (bRes.getStatusCode() != 200) {
                        if (!requirements.allowEither) {
                            responseModel.setDescription(bRes.getDescription());
                            responseModel.setStatusCode(bRes.getStatusCode());
                            return responseModel;
                        }
                        otpFailureResponse = bRes;
                    } else {
                        updateVeri = getInitAcPin.get(0);
                    }
                }
            }

            Map<String, Object> faceVerificationData = null;
            boolean shouldTryFace = requirements.faceRequired || (requirements.allowEither && updateVeri == null);
            if (shouldTryFace) {
                BaseResponse faceResponse = verifyBvnFace(rq, getBvnDe, processId);
                if (faceResponse.getStatusCode() != 200) {
                    if (requirements.allowEither && otpFailureResponse != null) {
                        faceResponse.setDescription("BVN ownership verification failed");
                    }
                    return faceResponse;
                }
                faceVerificationData = faceResponse.getData();
            }

            if (updateVeri != null) {
                updateVeri.setProcessIdUsed("1");
                updateVeri.setLastModifiedDate(Instant.now());
                updateVeri.setProcessId(processId);
                //updateVeri.setRequestId(otpReqId);
                verifyReqIdDetailsAuthRepo.save(updateVeri);
            }

            CreatNigeriaAccount cAcc = new CreatNigeriaAccount();
            cAcc.setBvn(rq.getBvn());
            cAcc.setCountryCode("NGN");
            cAcc.setEmailAddress(emailAddress);
            cAcc.setFullName(getRec.get().getFirstName());
            cAcc.setPhoneNumber(getBvnDe.getPhoneNumber1());
            cAcc.setWalletId(getRec.get().getWalletId());
            String virtAccNo = null;
            String virtName = null;
            // if (!environment.equals("dev")) {
            if (gotoBreezeapay.equals("1")) {
                BaseResponse calThirdParty = this.addNigeriaAccountCallThirdPartyApi(cAcc);

                if (calThirdParty.getStatusCode() != 200) {
                    AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account",
                            calThirdParty.getDescription(), "", "", emailAddress);
                    addFailedTransLoggRepo.save(pinActTransFailed);
                    responseModel.setDescription("Account creation failed, please try again!");
                    responseModel.setStatusCode(statusCode);

                    return responseModel;
                }
                virtAccNo = (String) calThirdParty.getData().get("virtAccNo");
                virtName = (String) calThirdParty.getData().get("virtAccName");

            } else {
                virtAccNo = uniqueIds.nextUniqueWalletId();
                virtName = getBvnDe.getFirstName() + " " + getBvnDe.getLastName();
            }

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
            addDe.setPhoneNumber(getBvnDe.getPhoneNumber1());
            addDe.setVirtualAccountName(virtName);
            addDe.setCreatedBy("System");
            addDe.setLastModifiedBy("System");

            //call add and account to wallet
            WalletSystemResponse addUserToWalletSystem = walletServices.addUserToWalletSystem(addDe.getAccountNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                return responseModel;

            }

            AddNewUserToLimit addLimit = new AddNewUserToLimit();
            addLimit.setCategory(uttilityMethods.getTier2());
            addLimit.setWalletNumber(addDe.getWalletId());
            BaseResponse bAddLimitRes = walletServices.addTierToWallet(addLimit);
            //BaseResponse bAddLimitRes = null;

            //ALSO CREATE WALLET HERE
            if (bAddLimitRes.getStatusCode() != 200) {

                responseModel.setDescription(bAddLimitRes.getDescription());
                responseModel.setStatusCode(bAddLimitRes.getStatusCode());
                return responseModel;
            }

            addAccountDetailsRepo.save(addDe);
            try {
                marketProfileSyncService.syncNigeriaAccountProvisioned(getRec.get(), addDe);
            } catch (Exception ex) {
                logger.warn("Unable to sync NG_RETAIL market profile after account provisioning", ex);
            }

            Map added = new HashMap();
            added.put("accountNumber", addDe.getAccountNumber());
            added.put("countryCode", addDe.getCountryCode());
            added.put("countryName", addDe.getCountryName());
            if (faceVerificationData != null) {
                added.put("faceVerification", faceVerificationData);
            }

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

    private BaseResponse verifyBvnFace(AddAccountObj rq, BvnLookup bvnLookup, String processId) {
        BaseResponse responseModel = new BaseResponse();
        if (!hasText(rq.getLiveFaceBase64())) {
            responseModel.setDescription("Please complete face verification");
            responseModel.setStatusCode(400);
            return responseModel;
        }
        if (!hasText(bvnLookup.getBase64Image())) {
            responseModel.setDescription("BVN image not available for face verification");
            responseModel.setStatusCode(400);
            return responseModel;
        }

        try {
            Map<String, Object> livenessMetadata = null;
            if (hasText(rq.getLivenessSessionReference())) {
                BaseResponse livenessResponse = verifyLivenessForFace(rq, processId);
                if (livenessResponse.getStatusCode() != 200) {
                    return livenessResponse;
                }
                livenessMetadata = livenessResponse.getData();
            }

            IdentityFaceCompareRequest request = new IdentityFaceCompareRequest();
            request.setRequestReference("NG-BVN-FACE-" + faceReference(processId));
            request.setImageABase64(rq.getLiveFaceBase64());
            request.setImageBBase64(bvnLookup.getBase64Image());
            request.setPurpose(BVN_FACE_PURPOSE);

            IdentityFaceCompareResponse response = identityFaceProxy.compare(request);
            Map<String, Object> metadata = faceMetadata(response);
            if (isApprovedFaceMatch(response)) {
                responseModel.setDescription("Face verification successful");
                responseModel.setStatusCode(200);
                if (livenessMetadata != null) {
                    metadata.put("livenessVerification", livenessMetadata);
                }
                responseModel.setData(metadata);
                return responseModel;
            }

            responseModel.setDescription("Face verification failed");
            responseModel.setStatusCode(400);
            if (livenessMetadata != null) {
                metadata.put("livenessVerification", livenessMetadata);
            }
            responseModel.setData(metadata);
            return responseModel;
        } catch (Exception ex) {
            logger.warn("Face verification failed for BVN account request {}", faceReference(processId), ex);
            responseModel.setDescription("Face verification failed");
            responseModel.setStatusCode(400);
            return responseModel;
        }
    }

    private BaseResponse verifyLivenessForFace(AddAccountObj rq, String processId) {
        BaseResponse responseModel = new BaseResponse();
        try {
            IdentityLivenessVerifyRequest request = new IdentityLivenessVerifyRequest();
            request.setSessionReference(rq.getLivenessSessionReference());
            request.setRequestReference("NG-BVN-LIVE-" + faceReference(processId));
            request.setBase64Image(rq.getLiveFaceBase64());

            IdentityLivenessApiResponse<IdentityLivenessVerifyResponse> response = identityFaceProxy.verifyLiveness(request);
            Map<String, Object> metadata = livenessMetadata(response);
            if (isApprovedLiveness(response)) {
                responseModel.setDescription("Liveness verification successful");
                responseModel.setStatusCode(200);
                responseModel.setData(metadata);
                return responseModel;
            }

            responseModel.setDescription("Liveness verification failed");
            responseModel.setStatusCode(400);
            responseModel.setData(metadata);
            return responseModel;
        } catch (Exception ex) {
            logger.warn("Liveness verification failed for BVN account request {}", faceReference(processId), ex);
            responseModel.setDescription("Liveness verification failed");
            responseModel.setStatusCode(400);
            return responseModel;
        }
    }

    private boolean isApprovedLiveness(IdentityLivenessApiResponse<IdentityLivenessVerifyResponse> response) {
        return response != null
                && response.isSuccess()
                && response.getData() != null
                && "APPROVED".equalsIgnoreCase(response.getData().getDecision());
    }

    private Map<String, Object> livenessMetadata(IdentityLivenessApiResponse<IdentityLivenessVerifyResponse> response) {
        Map<String, Object> metadata = new HashMap<>();
        if (response == null) {
            return metadata;
        }
        metadata.put("code", response.getCode());
        metadata.put("message", response.getMessage());
        metadata.put("correlationId", response.getCorrelationId());
        if (response.getData() != null) {
            IdentityLivenessVerifyResponse data = response.getData();
            metadata.put("livenessSessionId", data.getLivenessSessionId());
            metadata.put("sessionReference", data.getSessionReference());
            metadata.put("livenessScore", data.getLivenessScore());
            metadata.put("spoofScore", data.getSpoofScore());
            metadata.put("confidenceScore", data.getConfidenceScore());
            metadata.put("attackType", data.getAttackType());
            metadata.put("decision", data.getDecision());
            metadata.put("reasons", data.getReasons());
            metadata.put("provider", data.getProvider());
            metadata.put("providerReference", data.getProviderReference());
        }
        return metadata;
    }

    private boolean isApprovedFaceMatch(IdentityFaceCompareResponse response) {
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return false;
        }
        IdentityFaceCompareResponse.IdentityFaceCompareData data = response.getData();
        boolean approved = "APPROVED".equalsIgnoreCase(data.getDecision());
        boolean matched = "MATCH".equalsIgnoreCase(data.getMatchOutcome());
        BigDecimal similarityScore = data.getSimilarityScore();
        boolean scorePassed = similarityScore != null && similarityScore.compareTo(faceMinimumScore) >= 0;
        return approved && matched && scorePassed;
    }

    private Map<String, Object> faceMetadata(IdentityFaceCompareResponse response) {
        Map<String, Object> metadata = new HashMap<>();
        if (response == null) {
            return metadata;
        }
        metadata.put("code", response.getCode());
        metadata.put("message", response.getMessage());
        metadata.put("correlationId", response.getCorrelationId());
        if (response.getData() != null) {
            IdentityFaceCompareResponse.IdentityFaceCompareData data = response.getData();
            metadata.put("verificationId", data.getVerificationId());
            metadata.put("similarityScore", data.getSimilarityScore());
            metadata.put("confidenceScore", data.getConfidenceScore());
            metadata.put("decision", data.getDecision());
            metadata.put("matchOutcome", data.getMatchOutcome());
            metadata.put("provider", data.getProvider());
            metadata.put("providerReference", data.getProviderReference());
        }
        return metadata;
    }

    private BaseResponse failAddAccount(BaseResponse responseModel, String message, int statusCode, String emailAddress) {
        AddFailedTransLog pinActTransFailed = new AddFailedTransLog("add-account", message, "", "", emailAddress);
        addFailedTransLoggRepo.save(pinActTransFailed);
        responseModel.setDescription(message);
        responseModel.setStatusCode(statusCode);
        return responseModel;
    }

    private VerificationRequirements resolveVerificationRequirements(BvnVerificationMode mode, AddAccountObj rq) {
        VerificationRequirements requirements = new VerificationRequirements();
        switch (mode) {
            case OTP_ONLY:
                requirements.otpRequired = true;
                break;
            case FACE_ONLY:
                requirements.faceRequired = true;
                break;
            case OTP_AND_FACE:
                requirements.otpRequired = true;
                requirements.faceRequired = true;
                break;
            case OTP_OR_FACE:
            default:
                if ("FACE".equalsIgnoreCase(rq.getVerificationMethod())) {
                    requirements.faceRequired = true;
                } else if ("OTP".equalsIgnoreCase(rq.getVerificationMethod())) {
                    requirements.otpRequired = true;
                } else if (hasOtpDetails(rq) && hasText(rq.getLiveFaceBase64())) {
                    requirements.allowEither = true;
                } else if (hasText(rq.getLiveFaceBase64())) {
                    requirements.faceRequired = true;
                } else {
                    requirements.otpRequired = true;
                }
                break;
        }
        return requirements;
    }

    private BvnVerificationMode resolveVerificationMode() {
        try {
            return BvnVerificationMode.valueOf(bvnVerificationMode.trim().toUpperCase());
        } catch (Exception ex) {
            logger.warn("Invalid BVN verification mode '{}', falling back to OTP_OR_FACE", bvnVerificationMode);
            return BvnVerificationMode.OTP_OR_FACE;
        }
    }

    private String faceReference(String processId) {
        return hasText(processId) && !"0".equals(processId) ? processId : String.valueOf(GlobalMethods.generateTransactionId());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasOtpDetails(AddAccountObj rq) {
        return hasText(rq.getRequestId()) && rq.getOtp() > 0;
    }

    private enum BvnVerificationMode {
        OTP_ONLY,
        FACE_ONLY,
        OTP_AND_FACE,
        OTP_OR_FACE
    }

    private static class VerificationRequirements {
        private boolean otpRequired;
        private boolean faceRequired;
        private boolean allowEither;
    }

}
