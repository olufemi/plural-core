/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.AcceptQuoteResponseFailed;
import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.domain.LocalTransFailedTransInfo;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.AcceptQuote;
import com.financial.wealth.api.transactions.models.AcceptQuoteFE;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.WalletNo;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.AcceptQuoteResponse;

import com.financial.wealth.api.transactions.models.tranfaar.inflow.CreateQuote;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.CreateQuoteFE;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.CreateQuoteResponse;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.CreateQuoteResponseFE;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.CreateQuoteWithResponse;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.CreateQuoteWithResponseFE;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.CreateQuoteWithdrawal;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.CreateQuoteWithdrawalFE;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.WithdrawalOutflow;
import com.financial.wealth.api.transactions.repo.AcceptQuoteResponseFailedRepo;
import com.financial.wealth.api.transactions.repo.CreateQuoteResLogRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SettlementFailureLogRepo;
import com.financial.wealth.api.transactions.tranfaar.util.HmacSigner;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang.StringUtils.left;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author olufemioshin
 */
@Service
public class CreateQuoteClient {

    @Value("${transfaar.url}")
    private String url;

    @Value("${transfaar.api-key}")
    private String apiKey;

    @Value("${transfaar.hmac-secret}")
    private String hmacSecret;

    @Value("${transfaar.create.quote.source.currency}")
    private String sourceCurrency;

    @Value("${transfaar.create.quote.target.currency}")
    private String targetCurrency;

    @Value("${transfaar.create.quote.type}")
    private String quoteType;

    @Value("${transfaar.create.quote.type.withdrawal}")
    private String quoteTypeWithdrawal;

    @Value("${transfaar.create.quote.time.zone}")
    private String timeZone;

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${create.quote.send.to.third.party}")
    private String sendToThirdParty;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    private final CreateQuoteResLogRepo createQuoteResLogRepo;
    private final SettlementFailureLogRepo settlementFailureLogRepo;
    private final AcceptQuoteResponseFailedRepo acceptQuoteResponseFailedRepo;
    private final UttilityMethods uttilityMethods;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final WebhookKeyService webhookKeyService;

    public CreateQuoteClient(CreateQuoteResLogRepo createQuoteResLogRepo,
            SettlementFailureLogRepo settlementFailureLogRepo,
            AcceptQuoteResponseFailedRepo acceptQuoteResponseFailedRepo,
            UttilityMethods uttilityMethods, RegWalletInfoRepository regWalletInfoRepository,
            WebhookKeyService webhookKeyService) {
        this.createQuoteResLogRepo = createQuoteResLogRepo;
        this.settlementFailureLogRepo = settlementFailureLogRepo;
        this.acceptQuoteResponseFailedRepo = acceptQuoteResponseFailedRepo;
        this.uttilityMethods = uttilityMethods;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.webhookKeyService = webhookKeyService;
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public BaseResponse validatePin(WalletNo rq, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            List<RegWalletInfo> getRegUsr = regWalletInfoRepository.findByEmailsList(emailAddress);
            if (!rq.getWalletId().equals(getRegUsr.get(0).getWalletId())) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }
            String encyrptedPin = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getRegUsr.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "he pin is not valid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("he pin is not valid!");
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

    public BaseResponse createQuoteWithdrawal(CreateQuoteWithdrawalFE request, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            if (!uttilityMethods.isValidEmailAddress(request.getExpectedSourceInteracEmail())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "ExpectedSourceEmail is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("ExpectedSourceEmail is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> walletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            CreateQuoteWithdrawal cQuote = new CreateQuoteWithdrawal();
            cQuote.setDestinationAmount(apiKey);
            cQuote.setExpectedSourceInteracEmail(request.getExpectedSourceInteracEmail());
            cQuote.setFeeConfigId("1");
            cQuote.setNarration(request.getNarration());
            cQuote.setQuoteType(quoteTypeWithdrawal);
            cQuote.setSourceAmount(request.getSourceAmount());
            cQuote.setSourceCurrency(sourceCurrency);
            cQuote.setTargetCurrency(targetCurrency);
            cQuote.setTz(timeZone);
            String bodyJson = mapper.writeValueAsString(cQuote);

            Map<String, String> sig = HmacSigner.makeSignature(hmacSecret, bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-API-Key", apiKey);
            headers.add("timestamp", sig.get("timestamp"));
            headers.add("signature", sig.get("signature"));
            if (sendToThirdParty.endsWith("1")) {
                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
                ResponseEntity<CreateQuoteWithResponse> resp = restTemplate.postForEntity(url, entity, CreateQuoteWithResponse.class);

                //return some details to the customer to confirm payment
                CreateQuoteWithResponseFE resFE = new CreateQuoteWithResponseFE();
                resFE.setBankName(resp.getBody().beneficiary.bankName);
                resFE.setCountryCode(resp.getBody().beneficiary.countryCode);
                resFE.setCreatedAt(resp.getBody().timeline.createdAt);
                resFE.setCurrencyCode(resp.getBody().beneficiary.currencyCode);
                resFE.setEmail(resp.getBody().beneficiary.email);
                resFE.setFeeCurrency(resp.getBody().fees.feeCurrency);
                resFE.setFirstName(resp.getBody().beneficiary.firstName);
                resFE.setLastName(resp.getBody().beneficiary.lastName);
                resFE.setQuoteId(resp.getBody().quoteId);
                resFE.setStatus(resp.getBody().status);
                resFE.setTotalFees(resp.getBody().fees.totalFees);
                resFE.setValidUntil(resp.getBody().timeline.validUntil);

                CreateQuoteResLog logg = new CreateQuoteResLog();
                logg.setBankName(resp.getBody().beneficiary.bankName);
                logg.setCountryCode(resp.getBody().beneficiary.countryCode);
                logg.setCreatedAt(resp.getBody().timeline.createdAt);
                logg.setCurrencyCode(resp.getBody().beneficiary.currencyCode);
                logg.setEmail(resp.getBody().beneficiary.email);
                logg.setFeeCurrency(resp.getBody().fees.feeCurrency);
                logg.setFirstName(resp.getBody().beneficiary.firstName);
                logg.setLastName(resp.getBody().beneficiary.lastName);
                logg.setQuoteId(resp.getBody().quoteId);
                logg.setStatus(resp.getBody().status);
                logg.setTotalFees(resp.getBody().fees.totalFees);
                logg.setValidUntil(resp.getBody().timeline.validUntil);
                logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                logg.setCreateQuoteResponse(resp.toString());
                logg.setWalletNumber(getDecoded.phoneNumber);
                logg.setAmount(resp.getBody().source.amount);
                logg.setPaymentType(resp.getBody().type);
                createQuoteResLogRepo.save(logg);
                // return resp.getBody();
                Map mp = new HashMap();
                mp.put("bankName", logg.getBankName());
                mp.put("expectedSourceInteracEmail", logg.getEmail());
                mp.put("firstName", logg.getFirstName());
                mp.put("lastName", logg.getLastName());
                mp.put("quoteId", logg.getQuoteId());
                mp.put("fees", new BigDecimal(logg.getTotalFees()));
                mp.put("walletNumber", logg.getWalletNumber());
                mp.put("totalAmount", new BigDecimal(logg.getAmount()).add(new BigDecimal(logg.getAmount())));
                mp.put("currencyCode", logg.getCurrencyCode());
                mp.put("paymentType", resp.getBody().type);
                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);
            } else {

                String quoteId = String.valueOf(GlobalMethods.generateTransactionId());

                CreateQuoteResLog logg = new CreateQuoteResLog();
                logg.setBankName("Bank of CAD");
                logg.setCountryCode("CAD");
                logg.setCreatedAt(new Date().toString());
                logg.setCurrencyCode("CAD");
                logg.setEmail(getDecoded.emailAddress);
                logg.setFeeCurrency("CAD");
                logg.setFirstName(walletdetails.get(0).getFirstName());
                logg.setLastName(walletdetails.get(0).getLastName());
                logg.setQuoteId(quoteId);
                logg.setStatus("PENDING");
                logg.setTotalFees("7");
                logg.setValidUntil(new Date().toString());
                logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                logg.setCreateQuoteResponse("PENDING");
                logg.setWalletNumber(walletdetails.get(0).getWalletId());
                logg.setAmount(request.getSourceAmount());
                logg.setPaymentType("WITHDRAWAL");
                createQuoteResLogRepo.save(logg);
                Map mp = new HashMap();
                mp.put("bankName", "Bank of CAD");
                mp.put("paymentType", "WITHDRAWAL");
                mp.put("expectedSourceInteracEmail", getDecoded.emailAddress);
                mp.put("firstName", walletdetails.get(0).getFirstName());
                mp.put("lastName", walletdetails.get(0).getLastName());
                mp.put("quoteId", quoteId.toString());
                mp.put("fees", new BigDecimal("7"));
                mp.put("walletNumber", walletdetails.get(0).getWalletId());
                mp.put("totalAmount", new BigDecimal(logg.getTotalFees()).add(new BigDecimal(request.getSourceAmount())));
                mp.put("currencyCode", "CAD");
                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);

            }

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse acceptQuoteWithdrawal(AcceptQuoteFE rq, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //validate pin (has user created pin?)
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            List<CreateQuoteResLog> getDee = createQuoteResLogRepo.findByQuoteId(rq.getQuoteId());

            if (getDee.size() <= 0) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "QuoteId is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("QuoteId is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (!getDee.get(0).getPaymentType().equals("WITHDRAWAL")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "PaymentTuype is mismatched!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("PaymentTuype is mismatched!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String encyrptedPin = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = senderWalletdetails.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The pin is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The pin is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            CreateQuoteResLog getDeeUp = createQuoteResLogRepo.findByQuoteIdUpdate(rq.getQuoteId());

            getDeeUp.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

            if (rq.isAccepted() != true) {
                getDeeUp.setIsAccepted("0");
                getDeeUp.setAcceptQuoteResponse("FAILED");
                createQuoteResLogRepo.save(getDeeUp);

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Quote was not accepted!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Quote was not accepted!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            AcceptQuote aQuote = new AcceptQuote();

            aQuote.setAccepted(rq.isAccepted());
            String bodyJson = mapper.writeValueAsString(aQuote);

            Map<String, String> sig = HmacSigner.makeSignature(hmacSecret, bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-API-Key", apiKey);
            headers.add("timestamp", sig.get("timestamp"));
            headers.add("signature", sig.get("signature"));
            if (sendToThirdParty.endsWith("1")) {
                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
                ResponseEntity<AcceptQuoteResponse> resp = restTemplate.postForEntity(url, entity, AcceptQuoteResponse.class);

                BigDecimal left = resp.getBody().getPaymentInstructions().getAmount();   // BigDecimal
                String rightStr = getDee.get(0).getAmount();                             // String

                //BigDecimal right = new BigDecimal(rightStr.trim()); // avoid Double.valueOf
                BigDecimal left2 = left.setScale(2, RoundingMode.UNNECESSARY);
                BigDecimal right2 = new BigDecimal(rightStr.trim()).setScale(2, RoundingMode.UNNECESSARY);

                AcceptQuoteResponseFailed acF = new AcceptQuoteResponseFailed();
                acF.setAmount(resp.getBody().getPaymentInstructions().getAmount());
                acF.setCurrency(resp.getBody().getPaymentInstructions().getCurrency());
                acF.setEmail(resp.getBody().getPaymentInstructions().getEmail());
                acF.setMessage(statusMessage);
                acF.setQuoteId(rq.getQuoteId());
                acF.setStatus(resp.getBody().getStatus());
                acF.setType(quoteTypeWithdrawal);

                acF.setTransactionId(hmacSecret);
                acF.setType(auth);

                if (left2.compareTo(right2) != 0) {
                    acF.setSuccess(false);
                    /* different */
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, the amount is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getQuoteId().equals(rq.getQuoteId())) {
                    acF.setSuccess(false);
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, quoteid is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getPaymentInstructions().getEmail().equals(getDee.get(0).getEmail())) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, email is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getStatus().equals("ACCEPTED")) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, beneficiary email is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            }

            getDeeUp.setIsAccepted("1");
            getDeeUp.setAcceptQuoteResponse("SUCCESS");
            createQuoteResLogRepo.save(getDeeUp);

            WithdrawalOutflow wSend = new WithdrawalOutflow();
            wSend.setAmount(getDee.get(0).getAmount());
            wSend.setQuoteId(getDee.get(0).getQuoteId());

            BaseResponse sendWith = webhookKeyService.processPaymentWithdrawal(wSend, auth);
            System.out.println("webhookKeyService.processPaymentWithdrawal response ::::::::::::::::  %S  " + new Gson().toJson(sendWith));

            Map mp = new HashMap();
            mp.put("quoteId", rq.getQuoteId());
            mp.put("accepted", true);
            responseModel.setData(mp);
            responseModel.setDescription("Quote accepted.");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse createQuote(CreateQuoteFE request, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            if (!uttilityMethods.isValidEmailAddress(request.getExpectedSourceInteracEmail())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "ExpectedSourceEmail is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("ExpectedSourceEmail is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<RegWalletInfo> walletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            CreateQuote cQuote = new CreateQuote();
            cQuote.setDestinationAmount(apiKey);
            cQuote.setExpectedSourceInteracEmail(request.getExpectedSourceInteracEmail());
            cQuote.setFeeConfigId("1");
            cQuote.setNarration(request.getNarration());
            cQuote.setQuoteType(quoteType);
            cQuote.setSourceAmount(request.getSourceAmount());
            cQuote.setSourceCurrency(sourceCurrency);
            cQuote.setTargetCurrency(targetCurrency);
            cQuote.setTz(timeZone);
            String bodyJson = mapper.writeValueAsString(cQuote);

            Map<String, String> sig = HmacSigner.makeSignature(hmacSecret, bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-API-Key", apiKey);
            headers.add("timestamp", sig.get("timestamp"));
            headers.add("signature", sig.get("signature"));
            if (sendToThirdParty.endsWith("1")) {
                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
                ResponseEntity<CreateQuoteResponse> resp = restTemplate.postForEntity(url, entity, CreateQuoteResponse.class);

                //return some details to the customer to confirm payment
                CreateQuoteResponseFE resFE = new CreateQuoteResponseFE();
                resFE.setBankName(resp.getBody().beneficiary.bankName);
                resFE.setCountryCode(resp.getBody().beneficiary.countryCode);
                resFE.setCreatedAt(resp.getBody().timeline.createdAt);
                resFE.setCurrencyCode(resp.getBody().beneficiary.currencyCode);
                resFE.setEmail(resp.getBody().beneficiary.email);
                resFE.setFeeCurrency(resp.getBody().fees.feeCurrency);
                resFE.setFirstName(resp.getBody().beneficiary.firstName);
                resFE.setLastName(resp.getBody().beneficiary.lastName);
                resFE.setQuoteId(resp.getBody().quoteId);
                resFE.setStatus(resp.getBody().status);
                resFE.setTotalFees(resp.getBody().fees.totalFees);
                resFE.setValidUntil(resp.getBody().timeline.validUntil);

                CreateQuoteResLog logg = new CreateQuoteResLog();
                logg.setBankName(resp.getBody().beneficiary.bankName);
                logg.setCountryCode(resp.getBody().beneficiary.countryCode);
                logg.setCreatedAt(resp.getBody().timeline.createdAt);
                logg.setCurrencyCode(resp.getBody().beneficiary.currencyCode);
                logg.setEmail(resp.getBody().beneficiary.email);
                logg.setFeeCurrency(resp.getBody().fees.feeCurrency);
                logg.setFirstName(resp.getBody().beneficiary.firstName);
                logg.setLastName(resp.getBody().beneficiary.lastName);
                logg.setQuoteId(resp.getBody().quoteId);
                logg.setStatus(resp.getBody().status);
                logg.setTotalFees(resp.getBody().fees.totalFees);
                logg.setValidUntil(resp.getBody().timeline.validUntil);
                logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                logg.setCreateQuoteResponse(resp.toString());
                logg.setWalletNumber(getDecoded.phoneNumber);
                logg.setAmount(resp.getBody().source.amount);
                logg.setPaymentType(resp.getBody().type);
                createQuoteResLogRepo.save(logg);
                // return resp.getBody();
                Map mp = new HashMap();
                mp.put("bankName", logg.getBankName());
                mp.put("expectedSourceInteracEmail", logg.getEmail());
                mp.put("firstName", logg.getFirstName());
                mp.put("lastName", logg.getLastName());
                mp.put("quoteId", logg.getQuoteId());
                mp.put("fees", new BigDecimal(logg.getTotalFees()));
                mp.put("walletNumber", logg.getWalletNumber());
                mp.put("totalAmount", new BigDecimal(logg.getAmount()).add(new BigDecimal(logg.getAmount())));
                mp.put("currencyCode", logg.getCurrencyCode());
                mp.put("paymentType", resp.getBody().type);
                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);
            } else {

                String quoteId = String.valueOf(GlobalMethods.generateTransactionId());

                CreateQuoteResLog logg = new CreateQuoteResLog();
                logg.setBankName("Bank of CAD");
                logg.setCountryCode("CAD");
                logg.setCreatedAt(new Date().toString());
                logg.setCurrencyCode("CAD");
                logg.setEmail(getDecoded.emailAddress);
                logg.setFeeCurrency("CAD");
                logg.setFirstName(walletdetails.get(0).getFirstName());
                logg.setLastName(walletdetails.get(0).getLastName());
                logg.setQuoteId(quoteId);
                logg.setStatus("PENDING");
                logg.setTotalFees("7");
                logg.setValidUntil(new Date().toString());
                logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                logg.setCreateQuoteResponse("PENDING");
                logg.setWalletNumber(walletdetails.get(0).getWalletId());
                logg.setAmount(request.getSourceAmount());
                logg.setPaymentType("DEPOSIT");
                createQuoteResLogRepo.save(logg);
                Map mp = new HashMap();
                mp.put("bankName", "Bank of CAD");
                mp.put("paymentType", "DEPOSIT");
                mp.put("expectedSourceInteracEmail", getDecoded.emailAddress);
                mp.put("firstName", walletdetails.get(0).getFirstName());
                mp.put("lastName", walletdetails.get(0).getLastName());
                mp.put("quoteId", quoteId.toString());
                mp.put("fees", new BigDecimal("7"));
                mp.put("walletNumber", walletdetails.get(0).getWalletId());
                mp.put("totalAmount", new BigDecimal(logg.getTotalFees()).add(new BigDecimal(request.getSourceAmount())));
                mp.put("currencyCode", "CAD");
                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);

            }

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    public BaseResponse acceptQuote(AcceptQuoteFE rq, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //validate pin (has user created pin?)
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            List<CreateQuoteResLog> getDee = createQuoteResLogRepo.findByQuoteId(rq.getQuoteId());

            if (getDee.size() <= 0) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "QuoteId is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("QuoteId is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            if (!getDee.get(0).getPaymentType().equals("WITHDRAWAL")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "PaymentTuype is mismatched!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("PaymentTuype is mismatched!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String encyrptedPin = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = senderWalletdetails.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The pin is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The pin is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            CreateQuoteResLog getDeeUp = createQuoteResLogRepo.findByQuoteIdUpdate(rq.getQuoteId());

            getDeeUp.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

            if (rq.isAccepted() != true) {
                getDeeUp.setIsAccepted("0");
                getDeeUp.setAcceptQuoteResponse("FAILED");
                createQuoteResLogRepo.save(getDeeUp);

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Quote was not accepted!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Quote was not accepted!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            AcceptQuote aQuote = new AcceptQuote();

            aQuote.setAccepted(rq.isAccepted());
            String bodyJson = mapper.writeValueAsString(aQuote);

            Map<String, String> sig = HmacSigner.makeSignature(hmacSecret, bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-API-Key", apiKey);
            headers.add("timestamp", sig.get("timestamp"));
            headers.add("signature", sig.get("signature"));
            if (sendToThirdParty.endsWith("1")) {
                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
                ResponseEntity<AcceptQuoteResponse> resp = restTemplate.postForEntity(url, entity, AcceptQuoteResponse.class);

                BigDecimal left = resp.getBody().getPaymentInstructions().getAmount();   // BigDecimal
                String rightStr = getDee.get(0).getAmount();                             // String

                //BigDecimal right = new BigDecimal(rightStr.trim()); // avoid Double.valueOf
                BigDecimal left2 = left.setScale(2, RoundingMode.UNNECESSARY);
                BigDecimal right2 = new BigDecimal(rightStr.trim()).setScale(2, RoundingMode.UNNECESSARY);

                AcceptQuoteResponseFailed acF = new AcceptQuoteResponseFailed();
                acF.setAmount(resp.getBody().getPaymentInstructions().getAmount());
                acF.setCurrency(resp.getBody().getPaymentInstructions().getCurrency());
                acF.setEmail(resp.getBody().getPaymentInstructions().getEmail());
                acF.setMessage(statusMessage);
                acF.setQuoteId(rq.getQuoteId());
                acF.setStatus(resp.getBody().getStatus());
                acF.setType(quoteType);

                acF.setTransactionId(hmacSecret);
                acF.setType(auth);

                if (left2.compareTo(right2) != 0) {
                    acF.setSuccess(false);
                    /* different */
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, the amount is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getQuoteId().equals(rq.getQuoteId())) {
                    acF.setSuccess(false);
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, quoteid is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getPaymentInstructions().getEmail().equals(getDee.get(0).getEmail())) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, email is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!resp.getBody().getStatus().equals("ACCEPTED")) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Quote acceptance was rejected, beneficiary email is invalid!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Quote acceptance was rejected!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            }

            Map mp = new HashMap();
            mp.put("quoteId", rq.getQuoteId());
            mp.put("accepted", true);
            responseModel.setData(mp);
            responseModel.setDescription("Quote accepted.");
            responseModel.setStatusCode(200);

            getDeeUp.setIsAccepted("1");
            getDeeUp.setAcceptQuoteResponse("SUCCESS");
            createQuoteResLogRepo.save(getDeeUp);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

}
