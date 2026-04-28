/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import com.financial.wealth.api.transactions.utils.DecodedToken;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
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

    @Value("${transfaar.hmac-signing-strategy:BODY_PIPE_TIMESTAMP_HEX}")
    private String hmacSigningStrategy;

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
    private final ObjectMapper vendorMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private Map<String, String> signRequest(String bodyJson) {
        return HmacSigner.makeSignatureForStrategy(hmacSecret, bodyJson, hmacSigningStrategy);
    }

    private String resolveOnBehalfOf(String auth, List<RegWalletInfo> walletdetails) {
        String customerId = null;
        if (walletdetails != null && !walletdetails.isEmpty()) {
            customerId = trimToNull(walletdetails.get(0).getCustomerId());
        }
        if (customerId != null) {
            return customerId;
        }
        try {
            DecodedToken decodedToken = DecodedToken.getDecoded(auth);
            customerId = trimToNull(decodedToken.customerId);
            if (customerId != null) {
                return customerId;
            }
        } catch (Exception ex) {
            // Fall through to business owner context.
        }
        return "self";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String defaultDestinationAmount(String destinationAmount, String sourceAmount) {
        String resolved = firstNonBlank(destinationAmount, sourceAmount);
        return resolved == null ? sourceAmount : resolved;
    }

    private String normalizeAmount(String amount) {
        String trimmed = trimToNull(amount);
        if (trimmed == null) {
            return null;
        }
        return new BigDecimal(trimmed).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String writeVendorBody(Object payload) throws Exception {
        return vendorMapper.writeValueAsString(payload);
    }

    private String defaultOriginReference(String originReference) {
        String resolved = trimToNull(originReference);
        if (resolved == null) {
            return String.valueOf(GlobalMethods.generateTransactionId());
        }
        if (resolved.length() > 40) {
            return resolved.substring(0, 40);
        }
        return resolved;
    }

    private String defaultOnBehalfOf(String requestedOnBehalfOf, String resolvedOnBehalfOf) {
        String requested = trimToNull(requestedOnBehalfOf);
        return requested != null ? requested : resolvedOnBehalfOf;
    }

    private void validateExpectedSourceInteracEmail(String email, BaseResponse responseModel, String errorMessage)
            throws IllegalArgumentException {
        String trimmedEmail = trimToNull(email);
        if (trimmedEmail == null) {
            return;
        }
        if (!uttilityMethods.isValidEmailAddress(trimmedEmail)) {
            settlementFailureLogRepo.save(new SettlementFailureLog("", "", errorMessage));
            responseModel.setDescription(errorMessage);
            responseModel.setStatusCode(400);
            throw new IllegalArgumentException("INVALID_EXPECTED_SOURCE_EMAIL");
        }
    }

    private void applyTransactionProcessingFailure(BaseResponse responseModel, Exception ex) {
        responseModel.setDescription("Transaction processing failed");
        responseModel.setStatusCode(500);
        ex.printStackTrace();
    }

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
            /*String encyrptedPin = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getRegUsr.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {

                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "he pin is not valid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("he pin is not valid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }*/

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

            try {
                validateExpectedSourceInteracEmail(
                        request.getExpectedSourceInteracEmail(),
                        responseModel,
                        "ExpectedSourceEmail is invalid!");
            } catch (IllegalArgumentException ex) {
                if ("INVALID_EXPECTED_SOURCE_EMAIL".equals(ex.getMessage())) {
                    return responseModel;
                }
                throw ex;
            }

            List<RegWalletInfo> walletdetails
                    = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            if (walletdetails == null || walletdetails.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "", "Wallet not found!");
                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("Wallet not found!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String onBehalfOf = resolveOnBehalfOf(auth, walletdetails);
            // build quote request
            CreateQuoteWithdrawal cQuote = new CreateQuoteWithdrawal();
            cQuote.setDestinationAmount(normalizeAmount(defaultDestinationAmount(request.getDestinationAmount(), request.getSourceAmount())));
            cQuote.setExpectedSourceInteracEmail(trimToNull(request.getExpectedSourceInteracEmail()));
            cQuote.setFeeConfigId("1");
            cQuote.setNarration(request.getNarration());
            cQuote.setQuoteType(firstNonBlank(request.getQuoteType(), quoteTypeWithdrawal));
            cQuote.setSourceAmount(normalizeAmount(request.getSourceAmount()));
            cQuote.setSourceCurrency(firstNonBlank(request.getSourceCurrency(), sourceCurrency));
            cQuote.setTargetCurrency(firstNonBlank(request.getTargetCurrency(), targetCurrency));
            cQuote.setTz(firstNonBlank(request.getTz(), timeZone));
            cQuote.setOriginReference(defaultOriginReference(request.getOriginReference()));
            cQuote.setOnBehalfOf(defaultOnBehalfOf(request.getOnBehalfOf(), onBehalfOf));
            cQuote.setBeneficiaryId(trimToNull(request.getBeneficiaryId()));
            cQuote.setUserTag(trimToNull(request.getUserTag()));
            cQuote.setPaymentNetwork(trimToNull(request.getPaymentNetwork()));
            cQuote.setPaymentAddress(trimToNull(request.getPaymentAddress()));
            cQuote.setRail(trimToNull(request.getRail()));

            String bodyJson = writeVendorBody(cQuote);

            String endpoint = url + "/client/quotes";
            System.out.println("createQuoteWithdrawal url ::::: " + endpoint);
            System.out.println("createQuoteWithdrawal req ::::: " + bodyJson);

            Map<String, String> sig = signRequest(bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("X-API-Key", apiKey);
            headers.add("X-Timestamp", sig.get("timestamp"));
            headers.add("X-Signature", sig.get("signature"));

            if ("1".equals(sendToThirdParty)) {

                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

                ResponseEntity<String> resp
                        = restTemplate.postForEntity(endpoint, entity, String.class);

                System.out.println("createQuoteWithdrawal raw resp status ::::: " + resp.getStatusCode());
            // System.out.println("createQuoteWithdrawal raw resp body ::::: " + resp.getBody());

                CreateQuoteWithResponse body = resp.getBody() == null
                        ? null
                        : vendorMapper.readValue(resp.getBody(), CreateQuoteWithResponse.class);

                System.out.println("createQuoteWithdrawal resp ::::: "
                        + new Gson().toJson(body));
                if (body == null) {
                    responseModel.setDescription("No response from quote provider");
                    responseModel.setStatusCode(500);
                    return responseModel;
                }

                // persist log
                CreateQuoteResLog logg = new CreateQuoteResLog();

                if (body.beneficiary != null) {
                    logg.setBankName(body.beneficiary.bankName);
                    logg.setCountryCode(body.beneficiary.countryCode);
                    logg.setCurrencyCode(body.beneficiary.currencyCode);
                    logg.setEmail(body.beneficiary.email);
                    logg.setFeeCurrency(body.fees != null ? body.fees.feeCurrency : null);
                    logg.setFirstName(body.beneficiary.firstName);
                    logg.setLastName(body.beneficiary.lastName);
                }

                if (body.timeline != null) {
                    logg.setCreatedAt(body.timeline.createdAt);
                    logg.setValidUntil(body.timeline.validUntil);
                }

                logg.setQuoteId(body.quoteId);
                logg.setStatus(body.status);

                if (body.fees != null) {
                    logg.setTotalFees(body.fees.totalFees);
                }

                logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                logg.setCreateQuoteResponse(new Gson().toJson(body));
                logg.setWalletNumber(getDecoded.phoneNumber);

                if (body.source != null) {
                    logg.setAmount(body.source.amount);
                }

                logg.setPaymentType(body.type);

                createQuoteResLogRepo.save(logg);

                // FE response map
                Map mp = new HashMap();

                mp.put("bankName", logg.getBankName());
                mp.put("expectedSourceInteracEmail", logg.getEmail());
                mp.put("firstName", logg.getFirstName());
                mp.put("lastName", logg.getLastName());
                mp.put("quoteId", logg.getQuoteId());
                mp.put("walletNumber", logg.getWalletNumber());
                mp.put("currencyCode", logg.getCurrencyCode());
                mp.put("paymentType", body.type);

                BigDecimal amount = BigDecimal.ZERO;
                BigDecimal fees = BigDecimal.ZERO;

                if (logg.getAmount() != null && !logg.getAmount().trim().isEmpty()) {
                    amount = new BigDecimal(logg.getAmount().trim());
                }
                if (logg.getTotalFees() != null && !logg.getTotalFees().trim().isEmpty()) {
                    fees = new BigDecimal(logg.getTotalFees().trim());
                }

                mp.put("fees", fees);
                // FIX: total = amount + fees (not amount + amount)
                mp.put("totalAmount", amount.add(fees));

                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);

                System.out.println("createQuoteWithdrawal responseModel to FE ::::: "
                        + new Gson().toJson(responseModel));

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
                mp.put("quoteId", quoteId);
                mp.put("fees", new BigDecimal("7"));
                mp.put("walletNumber", walletdetails.get(0).getWalletId());
                mp.put("totalAmount", new BigDecimal(logg.getTotalFees()).add(new BigDecimal(request.getSourceAmount())));
                mp.put("currencyCode", "CAD");

                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);

                System.out.println("It is NOT THIRDPARTY createQuoteWithdrawal responseModel to FE ::::: "
                        + new Gson().toJson(responseModel));
            }

        } catch (Exception ex) {
            applyTransactionProcessingFailure(responseModel, ex);
        }

        return responseModel;
    }

    public BaseResponse acceptQuoteWithdrawal(AcceptQuoteFE rq, String auth) throws Exception {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            statusCode = 400;

            DecodedJWTToken decoded = DecodedJWTToken.getDecoded(auth);

            List<RegWalletInfo> senderWalletdetails
                    = regWalletInfoRepository.findByPhoneNumberData(decoded.phoneNumber);

            if (senderWalletdetails == null || senderWalletdetails.isEmpty()) {

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "Wallet not found!")
                );

                responseModel.setDescription("Wallet not found!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            List<CreateQuoteResLog> logs
                    = createQuoteResLogRepo.findByQuoteId(rq.getQuoteId());

            if (logs == null || logs.isEmpty()) {

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "QuoteId is invalid!")
                );

                responseModel.setDescription("QuoteId is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            CreateQuoteResLog log = logs.get(0);

            if (!"WITHDRAWAL".equals(log.getPaymentType())) {

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "PaymentType is mismatched!")
                );

                responseModel.setDescription("PaymentType is mismatched!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String isDebited = log.getIsDebited() == null ? "0" : log.getIsDebited();

            if ("1".equals(isDebited)) {

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "Withdrawal is already processed!")
                );

                responseModel.setDescription("Withdrawal already processed!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            // Validate PIN
            /*String encryptedPin
                    = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);

            String storedPin = senderWalletdetails.get(0).getPersonId();

            if (!encryptedPin.equals(storedPin)) {

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "Invalid PIN!")
                );

                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/
            CreateQuoteResLog logToUpdate
                    = createQuoteResLogRepo.findByQuoteIdUpdate(rq.getQuoteId());

            logToUpdate.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

            if (!rq.isAccepted()) {

                logToUpdate.setIsAccepted("0");
                logToUpdate.setAcceptQuoteResponse("FAILED");

                createQuoteResLogRepo.save(logToUpdate);

                settlementFailureLogRepo.save(
                        new SettlementFailureLog("", "", "Quote was not accepted!")
                );

                responseModel.setDescription("Quote not accepted!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            // Third party call
            String endpoint = url + "/client/quotes/" + rq.getQuoteId() + "/accept";

            String bodyJson = "";

            System.out.println("acceptQuoteWithdrawal req ::::: " + bodyJson);

            Map<String, String> sig
                    = signRequest(bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("X-API-Key", apiKey);
            headers.add("X-Timestamp", sig.get("timestamp"));
            headers.add("X-Signature", sig.get("signature"));

            if ("1".equals(sendToThirdParty)) {

                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

                ResponseEntity<String> resp
                        = restTemplate.postForEntity(endpoint, entity, String.class);

                System.out.println("acceptQuoteWithdrawal raw resp status ::::: " + resp.getStatusCode());
            // System.out.println("acceptQuoteWithdrawal raw resp body ::::: " + resp.getBody());

                AcceptQuoteResponse body = resp.getBody() == null
                        ? null
                        : vendorMapper.readValue(resp.getBody(), AcceptQuoteResponse.class);

                System.out.println("acceptQuoteWithdrawal resp ::::: "
                        + new Gson().toJson(body));

                if (body == null) {

                    responseModel.setDescription("No response from provider");
                    responseModel.setStatusCode(500);
                    return responseModel;
                }

                BigDecimal providerAmount
                        = body.getPaymentInstructions().getAmount();

                BigDecimal localAmount
                        = new BigDecimal(log.getAmount().trim());

                providerAmount = providerAmount.setScale(2, RoundingMode.UNNECESSARY);
                localAmount = localAmount.setScale(2, RoundingMode.UNNECESSARY);

                if (providerAmount.compareTo(localAmount) != 0) {

                    settlementFailureLogRepo.save(
                            new SettlementFailureLog("", "", "Amount mismatch!")
                    );

                    responseModel.setDescription("Quote rejected: amount mismatch");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!rq.getQuoteId().equals(body.getQuoteId())) {

                    settlementFailureLogRepo.save(
                            new SettlementFailureLog("", "", "QuoteId mismatch!")
                    );

                    responseModel.setDescription("Quote rejected: invalid quoteId");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!log.getEmail().equals(body.getPaymentInstructions().getEmail())) {

                    settlementFailureLogRepo.save(
                            new SettlementFailureLog("", "", "Email mismatch!")
                    );

                    responseModel.setDescription("Quote rejected: invalid email");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                if (!"ACCEPTED".equals(body.getStatus())) {

                    settlementFailureLogRepo.save(
                            new SettlementFailureLog("", "", "Quote not accepted by provider")
                    );

                    responseModel.setDescription("Quote rejected by provider");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            }

            // update local log
            logToUpdate.setIsAccepted("1");
            logToUpdate.setAcceptQuoteResponse("SUCCESS");
            logToUpdate.setStatus("PENDING");

            createQuoteResLogRepo.save(logToUpdate);

            // trigger withdrawal
            WithdrawalOutflow withdrawal = new WithdrawalOutflow();
            withdrawal.setAmount(log.getAmount());
            withdrawal.setQuoteId(log.getQuoteId());

            BaseResponse withdrawalResponse
                    = webhookKeyService.processPaymentWithdrawal(withdrawal, auth);

            System.out.println("Withdrawal processing response ::::: "
                    + new Gson().toJson(withdrawalResponse));

            Map<String, Object> mp = new HashMap<>();
            mp.put("quoteId", rq.getQuoteId());
            mp.put("accepted", true);

            responseModel.setData(mp);
            responseModel.setDescription("Quote accepted.");
            responseModel.setStatusCode(200);

            System.out.println("acceptQuoteWithdrawal response ::::: "
                    + new Gson().toJson(responseModel));

        } catch (Exception ex) {

            applyTransactionProcessingFailure(responseModel, ex);
        }

        return responseModel;
    }

    public BaseResponse createQuoteOld(CreateQuoteFE request, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {

            // System.out.println("apiKey ::::::::::::::::  %S  " + apiKey);

            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            try {
                validateExpectedSourceInteracEmail(
                        request.getExpectedSourceInteracEmail(),
                        responseModel,
                        "ExpectedSourceEmail is invalid!");
            } catch (IllegalArgumentException ex) {
                if ("INVALID_EXPECTED_SOURCE_EMAIL".equals(ex.getMessage())) {
                    return responseModel;
                }
                throw ex;
            }

            List<RegWalletInfo> walletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (walletdetails == null || walletdetails.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "", "Wallet not found!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Wallet not found!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String onBehalfOf = resolveOnBehalfOf(auth, walletdetails);

            CreateQuote cQuote = new CreateQuote();
            cQuote.setDestinationAmount(normalizeAmount(defaultDestinationAmount(request.getDestinationAmount(), request.getSourceAmount())));
            cQuote.setExpectedSourceInteracEmail(trimToNull(request.getExpectedSourceInteracEmail()));
            cQuote.setFeeConfigId("1");
            cQuote.setNarration(request.getNarration());
            cQuote.setQuoteType(firstNonBlank(request.getQuoteType(), quoteType));
            cQuote.setSourceAmount(normalizeAmount(request.getSourceAmount()));
            cQuote.setSourceCurrency(firstNonBlank(request.getSourceCurrency(), sourceCurrency));
            cQuote.setTargetCurrency(firstNonBlank(request.getTargetCurrency(), targetCurrency));
            cQuote.setTz(firstNonBlank(request.getTz(), timeZone));
            cQuote.setOriginReference(defaultOriginReference(request.getOriginReference()));
            cQuote.setOnBehalfOf(defaultOnBehalfOf(request.getOnBehalfOf(), onBehalfOf));
            cQuote.setBeneficiaryId(trimToNull(request.getBeneficiaryId()));
            cQuote.setUserTag(trimToNull(request.getUserTag()));
            cQuote.setPaymentNetwork(trimToNull(request.getPaymentNetwork()));
            cQuote.setPaymentAddress(trimToNull(request.getPaymentAddress()));
            cQuote.setRail(trimToNull(request.getRail()));
            //origin_reference
            //on_behalf_of - Customer ID (must belong to your business)
            String bodyJson = writeVendorBody(cQuote);

            System.out.println("createQuote req to thirdparty ::::::::::::::::  %S  " + new Gson().toJson(bodyJson));

            Map<String, String> sig = signRequest(bodyJson);

            url = url + "/client/quotes";
            System.out.println("createQuote url ::::::::::::::::  %S  " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-API-Key", apiKey);
            headers.add("X-Timestamp", sig.get("timestamp"));
            headers.add("X-Signature", sig.get("signature"));
            if (sendToThirdParty.endsWith("1")) {
                HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
                ResponseEntity<CreateQuoteResponse> resp = restTemplate.postForEntity(url, entity, CreateQuoteResponse.class);
                System.out.println("createQuote resp from thirdparty ::::::::::::::::  %S  " + new Gson().toJson(bodyJson));

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
                System.out.println("createQuote responseModel to FE ::::::::::::::::  %S  " + new Gson().toJson(responseModel));

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

    public BaseResponse createQuote(CreateQuoteFE request, String auth) throws Exception {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {
            System.out.println("apiKey ::::::::::::::::  %S  " + apiKey);

            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            List<RegWalletInfo> walletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (walletdetails == null || walletdetails.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "", "Wallet not found!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Wallet not found!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            String onBehalfOf = resolveOnBehalfOf(auth, walletdetails);

            // validate email only when supplied by the caller
            try {
                validateExpectedSourceInteracEmail(
                        request.getExpectedSourceInteracEmail(),
                        responseModel,
                        "ExpectedSourceEmail is invalid!");
            } catch (IllegalArgumentException ex) {
                if ("INVALID_EXPECTED_SOURCE_EMAIL".equals(ex.getMessage())) {
                    return responseModel;
                }
                throw ex;
            }

            // build quote request
            CreateQuote cQuote = new CreateQuote();
            cQuote.setDestinationAmount(normalizeAmount(defaultDestinationAmount(request.getDestinationAmount(), request.getSourceAmount())));
            cQuote.setExpectedSourceInteracEmail(trimToNull(request.getExpectedSourceInteracEmail()));
            cQuote.setFeeConfigId("1");
            cQuote.setNarration(request.getNarration());
            cQuote.setQuoteType(firstNonBlank(request.getQuoteType(), quoteType));
            cQuote.setSourceAmount(normalizeAmount(request.getSourceAmount()));
            cQuote.setSourceCurrency(firstNonBlank(request.getSourceCurrency(), sourceCurrency));
            cQuote.setTargetCurrency(firstNonBlank(request.getTargetCurrency(), targetCurrency));
            cQuote.setTz(firstNonBlank(request.getTz(), timeZone));
            cQuote.setOriginReference(defaultOriginReference(request.getOriginReference()));
            cQuote.setOnBehalfOf(defaultOnBehalfOf(request.getOnBehalfOf(), onBehalfOf));
            cQuote.setBeneficiaryId(trimToNull(request.getBeneficiaryId()));
            cQuote.setUserTag(trimToNull(request.getUserTag()));
            cQuote.setPaymentNetwork(trimToNull(request.getPaymentNetwork()));
            cQuote.setPaymentAddress(trimToNull(request.getPaymentAddress()));
            cQuote.setRail(trimToNull(request.getRail()));

            String bodyJson = writeVendorBody(cQuote);

            System.out.println("createQuote req ::::: " + bodyJson);

            String endpoint = url + "/client/quotes";
            System.out.println("createQuote url ::::: " + endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, String> sig = signRequest(bodyJson);

            headers.add("X-API-Key", apiKey);
            headers.add("X-Timestamp", sig.get("timestamp"));
            headers.add("X-Signature", sig.get("signature"));

            System.out.println("BODY ::::: " + bodyJson);
            // System.out.println("X-API-Key ::::: " + apiKey);
            // System.out.println("X-Timestamp ::::: " + sig.get("timestamp"));
            // System.out.println("X-Signature ::::: " + sig.get("signature"));
            // if not sending to third party, return mock/pending response (existing behavior)
            if (!"1".equals(sendToThirdParty)) {

                if (walletdetails == null || walletdetails.isEmpty()) {
                    responseModel.setDescription("Wallet details not found");
                    responseModel.setStatusCode(400);
                    return responseModel;
                }

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
                mp.put("quoteId", quoteId);
                mp.put("fees", new BigDecimal("7"));
                mp.put("walletNumber", walletdetails.get(0).getWalletId());
                mp.put("totalAmount", new BigDecimal(logg.getTotalFees()).add(new BigDecimal(request.getSourceAmount())));
                mp.put("currencyCode", "CAD");

                responseModel.setData(mp);
                responseModel.setDescription("Please accept quote");
                responseModel.setStatusCode(200);

                return responseModel;
            }

            // send to third party
            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

            ResponseEntity<String> resp
                    = restTemplate.postForEntity(endpoint, entity, String.class);

            System.out.println("createQuote raw resp status ::::: " + resp.getStatusCode());
            System.out.println("createQuote raw resp body ::::: " + resp.getBody());

            CreateQuoteResponse body = resp.getBody() == null
                    ? null
                    : vendorMapper.readValue(resp.getBody(), CreateQuoteResponse.class);

            System.out.println("createQuote resp status ::::: " + resp.getStatusCode());
            System.out.println("createQuote resp body ::::: " + new Gson().toJson(body));

            if (body == null) {
                responseModel.setDescription("No response from quote provider");
                responseModel.setStatusCode(500);
                return responseModel;
            }

            // map to FE response (your DTO uses public fields)
            CreateQuoteResponseFE resFE = new CreateQuoteResponseFE();
            if (body.beneficiary != null) {
                resFE.setBankName(body.beneficiary.bankName);
                resFE.setCountryCode(body.beneficiary.countryCode);
                resFE.setCurrencyCode(body.beneficiary.currencyCode);
                resFE.setEmail(body.beneficiary.email);
                resFE.setFirstName(body.beneficiary.firstName);
                resFE.setLastName(body.beneficiary.lastName);
            }
            if (body.timeline != null) {
                resFE.setCreatedAt(body.timeline.createdAt);
                resFE.setValidUntil(body.timeline.validUntil);
            }
            if (body.fees != null) {
                resFE.setFeeCurrency(body.fees.feeCurrency);
                resFE.setTotalFees(body.fees.totalFees);
            }
            resFE.setQuoteId(body.quoteId);
            resFE.setStatus(body.status);

            // persist log
            CreateQuoteResLog logg = new CreateQuoteResLog();

            if (body.beneficiary != null) {
                logg.setBankName(body.beneficiary.bankName);
                logg.setCountryCode(body.beneficiary.countryCode);
                logg.setCurrencyCode(body.beneficiary.currencyCode);
                logg.setEmail(body.beneficiary.email);
                logg.setFirstName(body.beneficiary.firstName);
                logg.setLastName(body.beneficiary.lastName);
            }
            if (body.timeline != null) {
                logg.setCreatedAt(body.timeline.createdAt);
                logg.setValidUntil(body.timeline.validUntil);
            }
            if (body.fees != null) {
                logg.setFeeCurrency(body.fees.feeCurrency);
                logg.setTotalFees(body.fees.totalFees);
            }

            logg.setQuoteId(body.quoteId);
            logg.setStatus(body.status);
            logg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            logg.setCreateQuoteResponse(new Gson().toJson(body));
            logg.setWalletNumber(getDecoded.phoneNumber);

            if (body.source != null) {
                logg.setAmount(body.source.amount);
            }
            logg.setPaymentType(body.type);

            createQuoteResLogRepo.save(logg);

            // build response map for FE
            Map<String, Object> mp = new HashMap<>();

            if (body.beneficiary != null) {
                mp.put("bankName", body.beneficiary.bankName);
                mp.put("expectedSourceInteracEmail", body.beneficiary.email);
                mp.put("firstName", body.beneficiary.firstName);
                mp.put("lastName", body.beneficiary.lastName);
                mp.put("currencyCode", body.beneficiary.currencyCode);
            } else {
                mp.put("bankName", null);
                mp.put("expectedSourceInteracEmail", request.getExpectedSourceInteracEmail());
                mp.put("firstName", null);
                mp.put("lastName", null);
                mp.put("currencyCode", null);
            }

            mp.put("quoteId", body.quoteId);
            mp.put("walletNumber", getDecoded.phoneNumber);
            mp.put("paymentType", body.type);

            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal fees = BigDecimal.ZERO;

            if (body.source != null && body.source.amount != null && !body.source.amount.trim().isEmpty()) {
                amount = new BigDecimal(body.source.amount.trim());
            }
            if (body.fees != null && body.fees.totalFees != null && !body.fees.totalFees.trim().isEmpty()) {
                fees = new BigDecimal(body.fees.totalFees.trim());
            }

            mp.put("fees", fees);
            mp.put("totalAmount", amount.add(fees));

            responseModel.setData(mp);
            responseModel.setDescription("Please accept quote");
            responseModel.setStatusCode(200);

            System.out.println("createQuote responseModel to FE ::::::::::::::::  " + new Gson().toJson(responseModel));

        } catch (Exception ex) {
            applyTransactionProcessingFailure(responseModel, ex);
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

            // validate wallet
            List<RegWalletInfo> senderWalletdetails
                    = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);

            if (senderWalletdetails == null || senderWalletdetails.isEmpty()) {

                SettlementFailureLog conWall
                        = new SettlementFailureLog("", "", "Wallet not found!");

                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("Wallet not found!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            // validate quote exists
            List<CreateQuoteResLog> getDee
                    = createQuoteResLogRepo.findByQuoteId(rq.getQuoteId());

            if (getDee == null || getDee.isEmpty()) {

                SettlementFailureLog conWall
                        = new SettlementFailureLog("", "", "QuoteId is invalid!");

                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("QuoteId is invalid!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            CreateQuoteResLog quoteLog = getDee.get(0);

            // validate payment type
            if (!"DEPOSIT".equals(quoteLog.getPaymentType())) {

                SettlementFailureLog conWall
                        = new SettlementFailureLog("", "", "PaymentType is mismatched!");

                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("PaymentType is mismatched!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            // validate pin
            /* String encryptedPin
                    = uttilityMethods.encyrpt(String.valueOf(rq.getPin()), encryptionKey);

            String storedPin = senderWalletdetails.get(0).getPersonId();

            if (!encryptedPin.equals(storedPin)) {

                SettlementFailureLog conWall
                        = new SettlementFailureLog("", "", "The pin is invalid!");

                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("The pin is invalid!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }*/
            // get updateable log
            CreateQuoteResLog getDeeUp
                    = createQuoteResLogRepo.findByQuoteIdUpdate(rq.getQuoteId());

            getDeeUp.setLastModifiedDate(
                    new Timestamp(System.currentTimeMillis()));

            // if user rejected quote
            if (!rq.isAccepted()) {

                getDeeUp.setIsAccepted("0");
                getDeeUp.setAcceptQuoteResponse("FAILED");

                createQuoteResLogRepo.save(getDeeUp);

                SettlementFailureLog conWall
                        = new SettlementFailureLog("", "", "Quote was not accepted!");

                settlementFailureLogRepo.save(conWall);

                responseModel.setDescription("Quote was not accepted!");
                responseModel.setStatusCode(statusCode);

                return responseModel;
            }

            String bodyJson = "";

            System.out.println("acceptQuote req ::::: " + bodyJson);

            String endpoint = url + "/client/quotes/" + rq.getQuoteId() + "/accept";

            System.out.println("acceptQuote url ::::: " + endpoint);

            Map<String, String> sig
                    = signRequest(bodyJson);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            headers.add("X-API-Key", apiKey);
            headers.add("X-Timestamp", sig.get("timestamp"));
            headers.add("X-Signature", sig.get("signature"));

            if ("1".equals(sendToThirdParty)) {

                HttpEntity<String> entity
                        = new HttpEntity<>(bodyJson, headers);

                ResponseEntity<String> resp
                        = restTemplate.postForEntity(endpoint,
                                entity,
                                String.class);

                System.out.println("acceptQuote raw resp status ::::: " + resp.getStatusCode());
            // System.out.println("acceptQuote raw resp body ::::: " + resp.getBody());

                AcceptQuoteResponse body = resp.getBody() == null
                        ? null
                        : vendorMapper.readValue(resp.getBody(), AcceptQuoteResponse.class);

                System.out.println("acceptQuote resp ::::: "
                        + new Gson().toJson(body));

                if (body == null) {

                    responseModel.setDescription("Invalid response from provider");
                    responseModel.setStatusCode(500);

                    return responseModel;
                }

                // validate amount
                BigDecimal providerAmount
                        = body.getPaymentInstructions().getAmount()
                                .setScale(2, RoundingMode.UNNECESSARY);

                BigDecimal expectedAmount
                        = new BigDecimal(quoteLog.getAmount().trim())
                                .setScale(2, RoundingMode.UNNECESSARY);

                if (providerAmount.compareTo(expectedAmount) != 0) {

                    SettlementFailureLog conWall
                            = new SettlementFailureLog("",
                                    "",
                                    "Quote acceptance rejected, amount mismatch!");

                    settlementFailureLogRepo.save(conWall);

                    responseModel.setDescription("Quote acceptance rejected!");
                    responseModel.setStatusCode(statusCode);

                    return responseModel;
                }

                // validate quote id
                if (!rq.getQuoteId().equals(body.getQuoteId())) {

                    SettlementFailureLog conWall
                            = new SettlementFailureLog("",
                                    "",
                                    "Quote acceptance rejected, invalid quoteId!");

                    settlementFailureLogRepo.save(conWall);

                    responseModel.setDescription("Quote acceptance rejected!");
                    responseModel.setStatusCode(statusCode);

                    return responseModel;
                }

                // validate email
                if (!quoteLog.getEmail()
                        .equals(body.getPaymentInstructions().getEmail())) {

                    SettlementFailureLog conWall
                            = new SettlementFailureLog("",
                                    "",
                                    "Quote acceptance rejected, email mismatch!");

                    settlementFailureLogRepo.save(conWall);

                    responseModel.setDescription("Quote acceptance rejected!");
                    responseModel.setStatusCode(statusCode);

                    return responseModel;
                }

                // validate accepted
                if (!"ACCEPTED".equals(body.getStatus())) {

                    SettlementFailureLog conWall
                            = new SettlementFailureLog("",
                                    "",
                                    "Quote acceptance rejected, status invalid!");

                    settlementFailureLogRepo.save(conWall);

                    responseModel.setDescription("Quote acceptance rejected!");
                    responseModel.setStatusCode(statusCode);

                    return responseModel;
                }
            }

            // success response
            Map mp = new HashMap();

            mp.put("quoteId", rq.getQuoteId());
            mp.put("accepted", true);

            responseModel.setData(mp);
            responseModel.setDescription("Quote accepted.");
            responseModel.setStatusCode(200);

            System.out.println("acceptQuote response ::::: "
                    + new Gson().toJson(responseModel));

            // update DB
            getDeeUp.setIsAccepted("1");
            getDeeUp.setAcceptQuoteResponse("SUCCESS");

            createQuoteResLogRepo.save(getDeeUp);

        } catch (Exception ex) {

            applyTransactionProcessingFailure(responseModel, ex);
        }

        return responseModel;
    }

}
