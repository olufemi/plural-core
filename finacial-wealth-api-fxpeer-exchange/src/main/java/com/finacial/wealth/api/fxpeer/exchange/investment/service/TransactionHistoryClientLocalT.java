/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthPaymentTransaction;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.TransactionHistoryEvent;
import static jakarta.persistence.GenerationType.UUID;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionHistoryClientLocalT {

    private static final Logger log = LoggerFactory.getLogger(TransactionHistoryClientLocalT.class);

    private static final String EXCHANGE = "finwealth.exchange";
    private static final String ROUTING_KEY = "finwealth.txn.history";
    private static final SecureRandom secureRandom = new SecureRandom();

    private final RabbitTemplate rabbitTemplate;

    public TransactionHistoryClientLocalT(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransactionHistoryEvent publishFromTxn(FinWealthPaymentTransaction req) {
        TransactionHistoryEvent e = new TransactionHistoryEvent();
        try {
            if (req == null) {
                throw new IllegalArgumentException("FinWealthPaymentTransaction is null");
            }

            String txId = req.getTransactionId();
            String walletNo = req.getWalletNo();

            if (txId == null || txId.trim().isEmpty()) {
                throw new IllegalArgumentException("transactionId is required for history logging");
            }
            if (walletNo == null || walletNo.trim().isEmpty()) {
                throw new IllegalArgumentException("walletNo is required for history logging");
            }
            if (req.getTransactionId() == null || req.getTransactionId().trim().isEmpty()) {
                throw new IllegalArgumentException("transactionId is required for history logging");
            }
            if (req.getWalletNo() == null || req.getWalletNo().trim().isEmpty()) {
                throw new IllegalArgumentException("walletNo is required for history logging");
            }

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

            // important for auditing
            e.setEventTime(java.time.Instant.now());

            publish(e);
            log.info("HISTORY PUBLISH OK txId={} walletNo={} paymentType={}", txId, walletNo, req.getPaymentType());

         

        } catch (Exception ex) {
            log.error("HISTORY PUBLISH FAILED txId={} walletNo={} paymentType={}",
                    req != null ? req.getTransactionId() : null,
                    req != null ? req.getWalletNo() : null,
                    req != null ? req.getPaymentType() : null,
                    ex);

        }
        return e;
    }

    public void publish(TransactionHistoryEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    ROUTING_KEY,
                    event,
                    msg -> {
                        // Traceability
                        msg.getMessageProperties().setMessageId(event.getTransactionId());
                        msg.getMessageProperties().setCorrelationId(event.getTransactionId());

                        // OPTIONAL: force a stable type id that your consumer can map
                        msg.getMessageProperties().getHeaders().put(
                                "__TypeId__",
                                "com.finacial.wealth.api.fxpeer.exchange.investment.record.TransactionHistoryEvent"
                        );
                        return msg;
                    }
            );
            log.info("Rabbit publish done txId={} exchange={} routingKey={}", event.getTransactionId(), EXCHANGE, ROUTING_KEY);
        } catch (Exception ex) {
            log.error("Rabbit publish failed txId={} exchange={} routingKey={}", event != null ? event.getTransactionId() : null, EXCHANGE, ROUTING_KEY, ex);
        }
    }

    public static String generateCorrelationId() {
        return new BigInteger(130, secureRandom).toString(32);
    }
}
