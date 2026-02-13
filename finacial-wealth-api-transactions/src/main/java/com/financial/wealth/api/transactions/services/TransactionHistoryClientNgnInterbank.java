/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.financial.wealth.api.transactions.domain.TransactionHistoryEvent;
import java.time.Instant;

@Service
public class TransactionHistoryClientNgnInterbank {

    private static final String EXCHANGE = "finwealth.exchange";
    private static final String ROUTING_KEY = "finwealth.txn.history";

    private final RabbitTemplate rabbitTemplate;

    public TransactionHistoryClientNgnInterbank(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransactionHistoryEvent publishFromTxn(FinWealthPaymentTransaction req) {

        if (req.getTransactionId() == null || req.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("transactionId is required for history logging");
        }
        if (req.getWalletNo() == null || req.getWalletNo().trim().isEmpty()) {
            throw new IllegalArgumentException("walletNo is required for history logging");
        }

        TransactionHistoryEvent e = new TransactionHistoryEvent();

        e.setWalletNo(req.getWalletNo());
        e.setReceiver(req.getReceiver());
        e.setReceiverName(req.getReceiverName());
        e.setSender(req.getSender());
        e.setSenderName(req.getSenderName());

        e.setReceiverBankCode(req.getReceiverBankCode());
        e.setReceiverBankName(req.getReceiverBankName());

        e.setTransactionId(req.getTransactionId());   // ✅ FIXED
        e.setPaymentType(req.getPaymentType());       // ✅ NOT hardcoded (or set default if null)
        e.setAmmount(req.getAmmount());
        e.setFees(req.getFees());

        e.setSentAmount(req.getSentAmount());
        e.setTheNarration(req.getTheNarration());

        e.setSenderTransactionType(req.getSenderTransactionType());
        e.setReceiverTransactionType(req.getReceiverTransactionType());

        e.setCurrencyCode(req.getCurrencyCode());

       // e.setEventTime(Instant.now());                // ✅ IMPORTANT

        publish(e);

        return e;
    }

    public void publish(TransactionHistoryEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
