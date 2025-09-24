/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.client.model.WalletSystemResponse;
import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.AddFailedTransLog;
import com.finacial.wealth.api.profiling.domain.Countries;
import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import com.finacial.wealth.api.profiling.models.accounts.AddAccountObj;
import com.finacial.wealth.api.profiling.models.accounts.ValidationResponse;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.AddFailedTransLoggRepo;
import com.finacial.wealth.api.profiling.repo.CountriesRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 *
 * @author olufemioshin
 */
public class AddAccountService {

    private final AddFailedTransLoggRepo addFailedTransLoggRepo;
    private final CountryService countryService;
    private final CountriesRepository countriesRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final UniqueIdService uniqueIds;
    private final WalletServices walletServices;

    public AddAccountService(AddFailedTransLoggRepo addFailedTransLoggRepo,
            CountryService countryService,
            CountriesRepository countriesRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            UniqueIdService uniqueIds,
            WalletServices walletServices) {
        this.addFailedTransLoggRepo = addFailedTransLoggRepo;
        this.countryService = countryService;
        this.countriesRepository = countriesRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.uniqueIds = uniqueIds;
        this.walletServices =walletServices;
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

            //call add and account to wallet
            WalletSystemResponse addUserToWalletSystem = walletServices.addUserToWalletSystem(addDe.getAccountNumber());
            if (addUserToWalletSystem.getStatusCode() != 200) {
                responseModel.setDescription(addUserToWalletSystem.getDescription());
                responseModel.setStatusCode(addUserToWalletSystem.getStatusCode());
                return responseModel;
            }
            
            addAccountDetailsRepo.save(addDe);

            //
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                    http.INTERNAL_SERVER_ERROR.toString(), "", "", rq.getAccount());
            addFailedTransLoggRepo.save(pinActTransFailed);
            ex.printStackTrace();
        }

        return responseModel;
    }

}
