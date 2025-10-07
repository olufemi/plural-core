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
import com.finacial.wealth.api.profiling.models.accounts.AddAccountObj;
import com.finacial.wealth.api.profiling.models.accounts.ValidationResponse;
import com.finacial.wealth.api.profiling.proxies.BreezePayVirtAcctProxy;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.AddFailedTransLoggRepo;
import com.finacial.wealth.api.profiling.repo.CountriesRepository;
import com.finacial.wealth.api.profiling.repo.GenerateVirtAcctNumbRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class AddAccountService {

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

    /*
    fin.wealth.breeze.pay.mer.id=${FIN_WEALTH_BREZ_VIRT_MERCHANT_ID:PLURAL}
fin.wealth.breeze.pay.mer.code.id=${FIN_WEALTH_BREZ_VIRT_MERCHANT_CODE:PLURAL}
fin.wealth.breeze.pay.mer.auth=${FIN_WEALTH_BREZ_VIRT_MERCHANT_AUTH:PLRo4fA4qFFCTFswX7OrA==}
fin.wealth.breeze.pay.mer.sub.key=${FIN_WEALTH_BREZ_VIRT_MERCHANT_SUB_KEY:PLURAL}
     */
    public AddAccountService(AddFailedTransLoggRepo addFailedTransLoggRepo,
            CountryService countryService,
            CountriesRepository countriesRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            UniqueIdService uniqueIds,
            WalletServices walletServices,
            BreezePayVirtAcctProxy breezePayVirtAcctProxy,
            RegWalletInfoRepository regWalletInfoRepository,
            GenerateVirtAcctNumbRepo generateVirtAcctNumbRepo, UttilityMethods uttilityMethods) {
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

            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(rq.getWalletId());

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
            addDe.setVirtualAccountNumber(reqAuthorizer);
            addDe.setPhoneNumber(rq.getPhoneNumber());

            //call add and account to wallet
            WalletSystemResponse addUserToWalletSystem = walletServices.addUserToWalletSystem(addDe.getAccountNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                return responseModel;
            }

            Map added = new HashMap();
            added.put("accountNumber", addDe.getAccountNumber());
            added.put("countryCode", addDe.getCountryCode());
            added.put("countryName", addDe.getCountryName());
            addAccountDetailsRepo.save(addDe);
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
