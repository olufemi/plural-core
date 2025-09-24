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
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;

import com.financial.wealth.api.transactions.repo.AcceptQuoteResponseFailedRepo;
import com.financial.wealth.api.transactions.repo.CreateQuoteResLogRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SettlementFailureLogRepo;
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
import org.springframework.stereotype.Service;

@Service
public class WebhookKeyService {

    private final CreateQuoteResLogRepo createQuoteResLogRepo;
    private final SettlementFailureLogRepo settlementFailureLogRepo;
    private final AcceptQuoteResponseFailedRepo acceptQuoteResponseFailedRepo;
    private final UttilityMethods utilMeth;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;

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
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo) {
        // Example: pre-provision one key
        keys.put(transfaarClient, generateBase64Secret());
        this.createQuoteResLogRepo = createQuoteResLogRepo;
        this.settlementFailureLogRepo = settlementFailureLogRepo;
        this.acceptQuoteResponseFailedRepo = acceptQuoteResponseFailedRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.utilMeth = utilMeth;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
    }

    public String findSecret(String keyId) {
        return keys.get(keyId);
    }

    public static String generateBase64Secret() {
        byte[] buf = new byte[32]; // 256-bit
        new SecureRandom().nextBytes(buf);
        return Base64.getEncoder().encodeToString(buf);
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
                        "Amount is invalid!");
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
            CreditWalletCaller rqC = new CreditWalletCaller();
            rqC.setAuth("Receiver");
            rqC.setFees("0.00");
            rqC.setFinalCHarges(amount);
            rqC.setNarration("Deposit");
            rqC.setPhoneNumber(getDee.get(0).getWalletNumber());
            rqC.setTransAmount(amount);
            rqC.setTransactionId(quoteId);
            BaseResponse creditAcct = utilMeth.creditCustomerWithType(rqC, "CUSTOMER");

            System.out.println("Credit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct));

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
            CreditWalletCaller globalGLCredit = new CreditWalletCaller();
            globalGLCredit.setAuth("Receiver");
            globalGLCredit.setFees("0.00");
            globalGLCredit.setFinalCHarges(amount);
            globalGLCredit.setNarration("GLOBAL_GL_Deposit");
            globalGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_GLOBAL()));
            globalGLCredit.setTransAmount(amount);
            globalGLCredit.setTransactionId(quoteId);

            utilMeth.creditCustomerWithType(globalGLCredit, "GLOBAL_GL");

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON");
        }

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
