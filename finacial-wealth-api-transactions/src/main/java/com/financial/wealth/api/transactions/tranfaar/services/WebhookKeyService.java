/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.models.PushNotificationFireBase;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.DepositWebhook;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.WithdrawalOutflow;

import com.financial.wealth.api.transactions.repo.AcceptQuoteResponseFailedRepo;
import com.financial.wealth.api.transactions.repo.CreateQuoteResLogRepo;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SettlementFailureLogRepo;
import com.financial.wealth.api.transactions.services.notify.FcmService;
import static com.financial.wealth.api.transactions.services.LocalTransferService.pushNotifyDebitWalletForWalletTransferSender;
import com.financial.wealth.api.transactions.services.notify.MessageCenterService;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import com.financial.wealth.api.transactions.utils.StrongAES;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WebhookKeyService {

    private final CreateQuoteResLogRepo createQuoteResLogRepo;
    private final SettlementFailureLogRepo settlementFailureLogRepo;
    private final AcceptQuoteResponseFailedRepo acceptQuoteResponseFailedRepo;
    private final UttilityMethods utilMeth;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final FcmService fcmService;
    private final MessageCenterService messageCenterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fell.conf.transfaar.client.name}")
    private String transfaarClient;
    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;
    // keyId -> secret (store hashed at rest in real systems)
    private final Map<String, String> keys = Collections.synchronizedMap(new HashMap<>());

    public WebhookKeyService(CreateQuoteResLogRepo createQuoteResLogRepo,
            SettlementFailureLogRepo settlementFailureLogRepo,
            AcceptQuoteResponseFailedRepo acceptQuoteResponseFailedRepo,
            UttilityMethods utilMeth, RegWalletInfoRepository regWalletInfoRepository,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            DeviceDetailsRepo deviceDetailsRepo,
            FcmService fcmService,
            MessageCenterService messageCenterService) {
        // Example: pre-provision one key
        keys.put(transfaarClient, generateBase64Secret());
        this.createQuoteResLogRepo = createQuoteResLogRepo;
        this.settlementFailureLogRepo = settlementFailureLogRepo;
        this.acceptQuoteResponseFailedRepo = acceptQuoteResponseFailedRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.utilMeth = utilMeth;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.deviceDetailsRepo = deviceDetailsRepo;
        this.fcmService = fcmService;
        this.messageCenterService = messageCenterService;
    }

    public String findSecret(String keyId) {
        return keys.get(keyId);
    }

    public static String generateBase64Secret() {
        byte[] buf = new byte[32]; // 256-bit
        new SecureRandom().nextBytes(buf);
        return Base64.getEncoder().encodeToString(buf);
    }

    @Scheduled(fixedRateString = "${pool.process.webhook.withdrawal.ms:360000}")
    public void processWebHookWithdrawal() {

        List<CreateQuoteResLog> getData = createQuoteResLogRepo.findDebitPendingAndAccepted();
        if (getData != null) {
            for (CreateQuoteResLog proc : getData) {
                WithdrawalOutflow witPoc = new WithdrawalOutflow();
                witPoc.setAmount(proc.getAmount());
                witPoc.setQuoteId(proc.getQuoteId());
                BaseResponse bRes = processPaymentWithdrawal(witPoc, "");
                System.out.println("schedule processPaymentWithdrawal::::::::::::::::  %S  " + new Gson().toJson(bRes));

            }
        }

    }

    @Scheduled(fixedRateString = "${pool.process.webhook.deposit.ms:360000}")
    public void processWebHookDeposit() {

        List<CreateQuoteResLog> getData = createQuoteResLogRepo.findAcceptedPendingDepositWithResponsePending();
        if (getData != null) {
            for (CreateQuoteResLog proc : getData) {

                DepositWebhook witPoc = new DepositWebhook();
                witPoc.setAmount(proc.getAmount());
                witPoc.setQuote_id(proc.getQuoteId());
                witPoc.setCurrency(proc.getCurrencyCode());
                witPoc.setEmail(proc.getEmail());
                witPoc.setPaymentType(proc.getPaymentType());
                witPoc.setStatus(proc.getStatus());
                ResponseEntity<?> xx = processPayment(witPoc.toString());
                System.out.println("schedule processWebHookDeposit::::::::::::::::  %S  " + new Gson().toJson(xx));

            }
        }

    }

    public BaseResponse processPaymentWithdrawal(WithdrawalOutflow rqq, String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "Something went wrong";

        try {
            statusCode = 400;
            // DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            List<CreateQuoteResLog> getDee = createQuoteResLogRepo.findByQuoteId(rqq.getQuoteId());

            if (getDee.size() <= 0) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "QuoteId is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("QuoteId is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!getDee.get(0).getIsAccepted().equals("1")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction was not accepted!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            String IsDebitedDescription = getDee.get(0).getIsDebitedDescription() == null ? "IN-PROGRESS" : getDee.get(0).getIsDebitedDescription();
            String IsDebited = getDee.get(0).getIsDebited() == null ? "0" : getDee.get(0).getIsDebited();

            if (IsDebitedDescription.equals("PROCESSED") || IsDebited.equals("1")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction is already processed!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            if (!getDee.get(0).getAmount().equals(rqq.getAmount())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Amount is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setStatusCode(statusCode);
                return responseModel;

            }

            //update success
            CreateQuoteResLog getDeeUp = createQuoteResLogRepo.findByQuoteIdUpdate(rqq.getQuoteId());
            getDeeUp.setIsDebited("1");
            getDeeUp.setIsDebitedDescription("PROCESSED");
            getDeeUp.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

            createQuoteResLogRepo.save(getDeeUp);

            // TODO: validate amounts, ids, etc., then persist or enqueue
            // 6) Build response
            List<RegWalletInfo> regWalletInfo = regWalletInfoRepository.findByWalletIdList(getDee.get(0).getWalletNumber());
            DebitWalletCaller rqC = new DebitWalletCaller();
            rqC.setAuth("Receiver");
            rqC.setFees("0.00");
            rqC.setFinalCHarges(rqq.getAmount());
            rqC.setNarration("Withdrawal");
            rqC.setPhoneNumber(regWalletInfo.get(0).getPhoneNumber());
            rqC.setTransAmount(rqq.getAmount());
            rqC.setTransactionId(rqq.getQuoteId());
            System.out.println("Credit Request TO core rqC ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse debitAcct = utilMeth.debitCustomerWithType(rqC, "CUSTOMER");

            System.out.println("Debit Response from core debitAcct ::::::::::::::::  %S  " + new Gson().toJson(debitAcct));

            //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
            if (debitAcct.getStatusCode() == 200) {

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(rqq.getAmount()));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqC.getFees()));
                kTrans2b.setPaymentType("Withdrawal from Account");
                kTrans2b.setReceiver(rqC.getPhoneNumber());
                kTrans2b.setSender(rqC.getPhoneNumber());
                kTrans2b.setTransactionId(rqq.getQuoteId());
                kTrans2b.setSenderTransactionType("");
                kTrans2b.setReceiverTransactionType("Withdrawal");

                List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rqC.getPhoneNumber());

                kTrans2b.setWalletNo(rqC.getPhoneNumber());
                kTrans2b.setReceiverName(getReceiverName.get(0).getFullName());
                kTrans2b.setSenderName(getReceiverName.get(0).getFullName());
                kTrans2b.setSentAmount(rqq.getAmount());
                kTrans2b.setTheNarration("Withdrawal");

                finWealthPaymentTransactionRepo.save(kTrans2b);

                PushNotificationFireBase puFireSender = new PushNotificationFireBase();
                puFireSender.setBody(pushNotifyCreditWalletForWalletTransfer(new BigDecimal(rqq.getAmount()),
                        "", "" + " " + ""
                ));
                List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(regWalletInfo.get(0).getWalletId());

                puFireSender.setTitle("Withdrawal-From-wallet");
                if (getDepuFireSender.size() > 0) {
                    String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                    if (getToken != "") {
                        puFireSender.setDeviceToken(getDepuFireSender.get(0).getToken());
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("type", "ALERT");            // sample custom data
                        if (puFireSender.getData() != null) {
                            data.putAll(puFireSender.getData());
                        }

                        /*fcmService.sendToToken(
                                puFireSender.getDeviceToken(),
                                puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data
                        );*/
                        messageCenterService.createAndPushToUser(getReceiverName.get(0).getWalletId(), puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data, null, "");

                    }
                }

            }

            // Credit BAAS CAD_GL
            DebitWalletCaller cadGLCredit = new DebitWalletCaller();
            cadGLCredit.setAuth("Receiver");
            cadGLCredit.setFees("0.00");
            cadGLCredit.setFinalCHarges(rqq.getAmount());
            cadGLCredit.setNarration("CAD_Withdrawal");
            cadGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
            cadGLCredit.setTransAmount(rqq.getAmount());
            cadGLCredit.setTransactionId(rqq.getQuoteId());

            utilMeth.debitCustomerWithType(cadGLCredit, "CAD_GL");
            responseModel.setDescription("Successful");
            responseModel.setStatusCode(200);
            return responseModel;

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;

    }

    public ResponseEntity<?> processPayment(String rawBody) {

        try {
            JsonNode root = objectMapper.readTree(rawBody);

            // Extract minimal fields you need (tolerant to types)
            String quoteId = asText(root, "quote_id");
            String paymentType = asText(root, "paymentType");
            String currency = asText(root, "currency");
            String email = asText(root, "email");
            String status = asText(root, "status"); // if boolean/duplicate, last valid wins
            String amount = asText(root, "amount");

            List<CreateQuoteResLog> getDee = createQuoteResLogRepo.findByQuoteId(quoteId);

            if (getDee.size() <= 0) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "QuoteId is invalid!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .success(false)
                        .description("QuoteId is invalid!")
                        .build();

                return ResponseEntity.ok(resp);
            }

            if (!getDee.get(0).getIsAccepted().equals("1")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction was not accepted!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .description("Transaction was not accepted!")
                        .success(false)
                        .build();

                return ResponseEntity.ok(resp);
            }

            if (getDee.get(0).getStatus().equals("RECEIVED")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction is already processed!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .description("Transaction is already processed!")
                        .success(false)
                        .build();

                return ResponseEntity.ok(resp);
            }

            if (!getDee.get(0).getAmount().equals(amount)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Amount is invalid!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .description("Amount is invalid!")
                        .success(false)
                        .build();

                return ResponseEntity.ok(resp);
            }

            if (!getDee.get(0).getPaymentType().equals(paymentType)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "PaymentType is invalid!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .description("PaymentType is invalid!")
                        .success(false)
                        .build();

                return ResponseEntity.ok(resp);
            }

            if (!getDee.get(0).getEmail().equals(email)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Email is invalid!");
                settlementFailureLogRepo.save(conWall);

                PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                        .quoteId(quoteId)
                        .paymentType(paymentType)
                        .currency(currency)
                        .email(email)
                        .status("REJECTED")
                        .success(false)
                        .description("Email is invalid!")
                        .build();

                return ResponseEntity.ok(resp);
            }

            PaymentNotificationResponse resp = PaymentNotificationResponse.builder()
                    .quoteId(quoteId)
                    .paymentType(paymentType)
                    .currency(currency)
                    .email(email)
                    // .status(status != null ? status : "RECEIVED")
                    .status("RECEIVED")
                    .description("Transaction was successful")
                    .success(true)
                    .build();

            //update success
            CreateQuoteResLog getDeeUp = createQuoteResLogRepo.findByQuoteIdUpdate(quoteId);
            getDeeUp.setStatus("RECEIVED");
            getDeeUp.setCreateQuoteResponse("PROCESSED");
            getDeeUp.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
            getDeeUp.setWebHookSuccResponse(rawBody);
            createQuoteResLogRepo.save(getDeeUp);

            // TODO: validate amounts, ids, etc., then persist or enqueue
            // 6) Build response
            List<RegWalletInfo> regWalletInfo = regWalletInfoRepository.findByWalletIdList(getDee.get(0).getWalletNumber());
            CreditWalletCaller rqC = new CreditWalletCaller();
            rqC.setAuth("Receiver");
            rqC.setFees("0.00");
            rqC.setFinalCHarges(amount);
            rqC.setNarration("Deposit");
            rqC.setPhoneNumber(regWalletInfo.get(0).getPhoneNumber());
            rqC.setTransAmount(amount);
            rqC.setTransactionId(quoteId);
            System.out.println("Credit Request TO core rqC ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse creditAcct = utilMeth.creditCustomerWithType(rqC, "CUSTOMER");

            System.out.println("Credit Response from core creditAcct ::::::::::::::::  %S  " + new Gson().toJson(creditAcct));

            //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
            if (creditAcct.getStatusCode() == 200) {

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(amount));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqC.getFees()));
                kTrans2b.setPaymentType("Deposit to Account");
                kTrans2b.setReceiver(rqC.getPhoneNumber());
                kTrans2b.setSender(rqC.getPhoneNumber());
                kTrans2b.setTransactionId(quoteId);
                kTrans2b.setSenderTransactionType("");
                kTrans2b.setReceiverTransactionType("Deposit");

                List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rqC.getPhoneNumber());

                kTrans2b.setWalletNo(rqC.getPhoneNumber());
                kTrans2b.setReceiverName(getReceiverName.get(0).getFullName());
                kTrans2b.setSenderName(getReceiverName.get(0).getFullName());
                kTrans2b.setSentAmount(amount);
                kTrans2b.setTheNarration("Deposit");

                finWealthPaymentTransactionRepo.save(kTrans2b);

                PushNotificationFireBase puFireSender = new PushNotificationFireBase();
                puFireSender.setBody(pushNotifyCreditWalletForWalletTransfer(new BigDecimal(amount),
                        "", "" + " " + ""
                ));
                List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(regWalletInfo.get(0).getWalletId());

                puFireSender.setTitle("Deposit-To-wallet");
                if (getDepuFireSender.size() > 0) {
                    String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                    if (getToken != "") {
                        puFireSender.setDeviceToken(getDepuFireSender.get(0).getToken());
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("type", "ALERT");            // sample custom data
                        if (puFireSender.getData() != null) {
                            data.putAll(puFireSender.getData());
                        }

                        messageCenterService.createAndPushToUser(getReceiverName.get(0).getWalletId(), puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data, null, "");

                        /* fcmService.sendToToken(
                                puFireSender.getDeviceToken(),
                                puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data
                        );*/
                    }
                }

            }

            // Credit BAAS CAD_GL
            CreditWalletCaller cadGLCredit = new CreditWalletCaller();
            cadGLCredit.setAuth("Receiver");
            cadGLCredit.setFees("0.00");
            cadGLCredit.setFinalCHarges(amount);
            cadGLCredit.setNarration("CAD_Deposit");
            cadGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
            cadGLCredit.setTransAmount(amount);
            cadGLCredit.setTransactionId(quoteId);

            utilMeth.creditCustomerWithType(cadGLCredit, "CAD_GL");

            // Credit GLOBAL GL
            /*CreditWalletCaller globalGLCredit = new CreditWalletCaller();
            globalGLCredit.setAuth("Receiver");
            globalGLCredit.setFees("0.00");
            globalGLCredit.setFinalCHarges(amount);
            globalGLCredit.setNarration("GLOBAL_GL_Deposit");
            globalGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL()));
            globalGLCredit.setTransAmount(amount);
            globalGLCredit.setTransactionId(quoteId);

            utilMeth.creditCustomerWithType(globalGLCredit, "GLOBAL_GL");*/
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON");
        }

    }

    public static String pushNotifyCreditWalletForWalletTransfer(BigDecimal amount, String recName, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been credited with " + "N" + amount + ", "
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    private String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);

        // log.info("decryptData ::::: {} ", decryptData);
        return decryptData;

    }

    private static String asText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isTextual()) {
            return v.asText();
        }
        if (v.isBoolean()) {
            return String.valueOf(v.asBoolean());
        }
        if (v.isNumber()) {
            return v.numberValue().toString();
        }
        return v.toString();
    }
}
