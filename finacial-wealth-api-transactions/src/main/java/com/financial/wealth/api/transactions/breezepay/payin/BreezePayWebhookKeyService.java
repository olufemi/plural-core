/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payin;

import com.fasterxml.jackson.databind.JsonNode;
import com.financial.wealth.api.transactions.breezepay.payout.AddAccountDetails;
import com.financial.wealth.api.transactions.breezepay.payout.AddAccountDetailsRepo;
import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.PushNotificationFireBase;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.services.notify.MessageCenterService;
import com.financial.wealth.api.transactions.tranfaar.services.PaymentNotificationResponse;
import static com.financial.wealth.api.transactions.tranfaar.services.WebhookKeyService.pushNotifyCreditWalletForWalletTransfer;
import com.financial.wealth.api.transactions.utils.StrongAES;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
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

/**
 *
 * @author olufemioshin
 */
@Service
@RequiredArgsConstructor
public class BreezePayWebhookKeyService {

    private final UttilityMethods utilMeth;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final MessageCenterService messageCenterService;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private static final String CCY = "NGN";
    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    public BaseResponse processPayment(WebHookRequest rq,String auth) {

        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            // TODO: validate amounts, ids, etc., then persist or enqueue
            // 6) Build response
            List<AddAccountDetails> getAcct = addAccountDetailsRepo.findByVirtualAccountNumberList(rq.getVirtualAccount());

            List<RegWalletInfo> regWalletInfo = regWalletInfoRepository.findByEmailsList(getAcct.get(0).getEmailAddress());

            CreditWalletCaller rqC = new CreditWalletCaller();
            rqC.setAuth("Receiver");
            rqC.setFees("0.00");
            rqC.setFinalCHarges(rq.getAmount());
            rqC.setNarration("Deposit");
            rqC.setPhoneNumber(getAcct.get(0).getAccountNumber());
            rqC.setTransAmount(rq.getAmount());
            rqC.setTransactionId(rq.getProcessId());
            System.out.println("Credit Request TO core rqC ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse creditAcct = utilMeth.creditCustomerWithType(rqC, "CUSTOMER");

            System.out.println("Credit Response from core creditAcct ::::::::::::::::  %S  " + new Gson().toJson(creditAcct));

            //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
            if (creditAcct.getStatusCode() == 200) {

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(rq.getAmount()));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqC.getFees()));
                kTrans2b.setPaymentType("Deposit to Account");
                kTrans2b.setReceiver(getAcct.get(0).getAccountNumber());
                kTrans2b.setSender(rq.getVirtualAccount());
                kTrans2b.setTransactionId(rq.getProcessId());
                kTrans2b.setSenderTransactionType("");
                kTrans2b.setReceiverTransactionType("Deposit");
                kTrans2b.setCurrencyCode(CCY);

                // List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rqC.getPhoneNumber());
                kTrans2b.setWalletNo(rqC.getPhoneNumber());
                kTrans2b.setReceiverName(regWalletInfo.get(0).getFullName());
                kTrans2b.setSenderName(getAcct.get(0).getVirtualAccountName());
                kTrans2b.setSentAmount(rq.getAmount());
                kTrans2b.setTheNarration("Deposit");

                finWealthPaymentTransactionRepo.save(kTrans2b);

                PushNotificationFireBase puFireSender = new PushNotificationFireBase();
                puFireSender.setBody(pushNotifyCreditWalletForWalletTransfer(new BigDecimal(rq.getAmount()),
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

                        messageCenterService.createAndPushToUser(regWalletInfo.get(0).getWalletId(), puFireSender.getTitle(),
                                puFireSender.getBody(),
                                data, null, "");

                    }
                }

            }

            // Credit BAAS NGN_GL
            CreditWalletCaller ngnGLCredit = new CreditWalletCaller();
            ngnGLCredit.setAuth("Receiver");
            ngnGLCredit.setFees("0.00");
            ngnGLCredit.setFinalCHarges(rq.getAmount());
            ngnGLCredit.setNarration("NGN_Deposit");
            ngnGLCredit.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG()));
            ngnGLCredit.setTransAmount(rq.getAmount());
            ngnGLCredit.setTransactionId(rq.getProcessId());

            utilMeth.creditCustomerWithType(ngnGLCredit, "NGN_GL");

            responseModel.setDescription("Success");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            ex.printStackTrace();
        }

        return responseModel;
    }

    private String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);

        // log.info("decryptData ::::: {} ", decryptData);
        return decryptData;

    }

}
