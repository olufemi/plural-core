/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.config.RabbitConfig;
import com.finacial.wealth.api.utility.domains.FinWealthPaymentTransaction;
import com.finacial.wealth.api.utility.domains.TransactionHistoryEvent;
import com.finacial.wealth.api.utility.repository.FinWealthPaymentTransactionRepo;
import java.time.Instant;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionHistoryListener {

    private final FinWealthPaymentTransactionRepo repo;

    public TransactionHistoryListener(FinWealthPaymentTransactionRepo repo) {
        this.repo = repo;
    }

    @RabbitListener(
            queues = RabbitConfig.QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(TransactionHistoryEvent e) {

        System.out.println("Consumed txnId=" + e.getTransactionId()
                + " eventTime=" + e.getEventTime());

        System.out.println("Received TXN Event: " + e.getTransactionId());

        if (repo.existsByTransactionId(e.getTransactionId())) {
            System.out.println("Already exists, skipping");
            return;
        }

        if (repo.existsByTransactionId(e.getTransactionId())) {
            return;
        }

        FinWealthPaymentTransaction t = new FinWealthPaymentTransaction();

        t.setWalletNo(e.getWalletNo());
        t.setReceiver(e.getReceiver());
        t.setReceiverName(e.getReceiverName());
        t.setSenderName(e.getSenderName());
        t.setSender(e.getSender());
        t.setReceiverBankName(e.getReceiverBankName());
        t.setReceiverBankCode(e.getReceiverBankCode());

        t.setTransactionType(e.getTransactionType());
        t.setPaymentType(e.getPaymentType());
        t.setPaymentTypeFeeCum(e.getPaymentTypeFeeCum());
        t.setCusPaymentTypeFeeCum(e.getCusPaymentTypeFeeCum());

        t.setAmmount(e.getAmmount());
        t.setAmmountCum(e.getAmmountCum());
        t.setCusAmountCum(e.getCusAmountCum());

        t.setFees(e.getFees());
        t.setTransactionId(e.getTransactionId());
        t.setSentAmount(e.getSentAmount());
        t.setTheNarration(e.getTheNarration());

        t.setSourceAccount(e.getSourceAccount());
        t.setSenderTransactionType(e.getSenderTransactionType());
        t.setReceiverTransactionType(e.getReceiverTransactionType());

        t.setReversals(e.getReversals());
        t.setCreatedBy(e.getCreatedBy());
        t.setEmailAddress(e.getEmailAddress());
        t.setRequestType(e.getRequestType());

        t.setAirtimeReceiver(e.getAirtimeReceiver());
        t.setDataReceiver(e.getDataReceiver());
        t.setBillsToken(e.getBillsToken());
        t.setCurrencyCode(e.getCurrencyCode());

        // timestamp
       // t.setCreatedDate(e.getEventTime() != null ? e.getEventTime() : Instant.now());
       t.setCreatedDate(Instant.now());

        repo.save(t);

        System.out.println("Transaction saved successfully");
    }

}
