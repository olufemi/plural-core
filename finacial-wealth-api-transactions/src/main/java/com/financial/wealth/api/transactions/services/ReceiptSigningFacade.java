/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.FinWalletPaymentTransModel;
import com.financial.wealth.api.transactions.models.ReceiptSignRequest;
import com.financial.wealth.api.transactions.models.ReceiptSignResponse;
import com.financial.wealth.api.transactions.proxies.ReceiptCryptoClient;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReceiptSigningFacade {

    private final ReceiptCryptoClient cryptoClient;

    public void attachReceipt(FinWalletPaymentTransModel tx) {

        if (tx.getCreatedDate() == null) {
            throw new IllegalStateException("createdDate is null for txId=" + tx.getTransactionId()
                    + ". Must populate createdDate from DB.");
        }
        if (tx.getStatus() == null || tx.getStatus().trim().isEmpty()) {
            throw new IllegalStateException("status is missing for txId=" + tx.getTransactionId());
        }
        if (tx.getCurrencyCode() == null || tx.getCurrencyCode().trim().isEmpty()) {
            throw new IllegalStateException("currencyCode is missing for txId=" + tx.getTransactionId());
        }

        ReceiptSignRequest req = new ReceiptSignRequest();
        req.setTxId(tx.getTransactionId());
        req.setAmountMinor(MoneyMinorUnits.toMinorUnits(tx.getAmmount(), tx.getCurrencyCode()));
        req.setCurrency(tx.getCurrencyCode().trim().toUpperCase());
        req.setSenderId(tx.getSender());     // agreed: senderId=sender
        req.setReceiverId(tx.getReceiver()); // agreed: receiverId=receiver
        req.setTimestampUtcIso(DateTimeFormatter.ISO_INSTANT.format(tx.getCreatedDate()));
        req.setStatus(tx.getStatus().trim().toUpperCase());

       // System.out.println("ReceiptSignRequest ::::::::::::::::::::: " + new Gson().toJson(req));

        ReceiptSignResponse sig = cryptoClient.sign(req);

        tx.setReceiptKid(sig.getKid());
        tx.setReceiptSignature(sig.getSignature());
    }
}
