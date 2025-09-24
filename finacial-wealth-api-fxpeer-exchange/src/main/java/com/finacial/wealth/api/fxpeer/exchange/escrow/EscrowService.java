/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.escrow;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.EscrowStatus;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.ledger.LedgerClient;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.CreditRq;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.DebitRq;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.TxnResult;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.receipt.ReceiptService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EscrowService {

    private final EscrowRepository repo;
    private final LedgerClient ledger;
    private final OrderRepository orders;
    private final ReceiptService receipts;

    public EscrowService(EscrowRepository repo, LedgerClient ledger, OrderRepository orders, ReceiptService receipts) {
        this.repo = repo;
        this.ledger = ledger;
        this.orders = orders;
        this.receipts = receipts;
    }

    @Transactional
    public Escrow get(long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Escrow not found"));
    }

    @Transactional
    public Escrow initEscrow(long orderId, long escrowTtlSeconds) {
        Order o = orders.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        Escrow e = new Escrow();
        e.setOrderId(o.getId());
        e.setExpiresAt(Instant.now().plusSeconds(escrowTtlSeconds));
        EscrowLeg buyer = new EscrowLeg();
        buyer.setUserId(o.getBuyerUserId());
        buyer.setCurrency(o.getCurrencyReceive());
        buyer.setRequiredAmount(o.getReceiveAmount());
        EscrowLeg seller = new EscrowLeg();
        seller.setUserId(o.getSellerUserId());
        seller.setCurrency(o.getCurrencySell());
        seller.setRequiredAmount(o.getSellAmount());
        e.setBuyerLeg(buyer);
        e.setSellerLeg(seller);
        e.setStatus(EscrowStatus.PENDING_BUYER);
        return repo.save(e);
    }

    @Transactional
    public void fundBuyer(long escrowId, String idemKey) {
        Escrow e = repo.findById(escrowId).orElseThrow(() -> new NotFoundException("Escrow not found"));
        if (e.getBuyerLeg().getFundedAmount() != null) {
            return; // idempotent
        }
        var leg = e.getBuyerLeg();
        TxnResult res = ledger.debit(idemKey, new DebitRq(
                leg.getUserId(), leg.getCurrency().name(), leg.getRequiredAmount(),
                "ESCROW-" + e.getId() + "-BUYER"));
        leg.setFundedAmount(leg.getRequiredAmount());
        leg.setFundedAt(Instant.now());
        leg.setLedgerTxnId(res.txnId());
        if (e.getSellerLeg().getFundedAmount() != null) {
            e.setStatus(EscrowStatus.ESCROWED);
        } else {
            e.setStatus(EscrowStatus.PENDING_SELLER);
        }
        repo.save(e);
    }

    @Transactional
    public void fundSeller(long escrowId, String idemKey) {
        Escrow e = repo.findById(escrowId).orElseThrow(() -> new NotFoundException("Escrow not found"));
        if (e.getSellerLeg().getFundedAmount() != null) {
            return; // idempotent
        }
        var leg = e.getSellerLeg();
        TxnResult res = ledger.debit(idemKey, new DebitRq(
                leg.getUserId(), leg.getCurrency().name(), leg.getRequiredAmount(),
                "ESCROW-" + e.getId() + "-SELLER"));
    }

    @Transactional
    public void confirmRelease(long escrowId, boolean buyer) {
        Escrow e = repo.findById(escrowId).orElseThrow(() -> new NotFoundException("Escrow not found"));
        if (e.getStatus() != EscrowStatus.ESCROWED) {
            throw new BusinessException("Not escrowed");
        }
        if (buyer) {
            e.setBuyerReleased(true);
        } else {
            e.setSellerReleased(true);
        }
        if (e.isBuyerReleased() && e.isSellerReleased()) {
// Perform swap credits
            Order o = orders.findById(e.getOrderId()).orElseThrow(() -> new NotFoundException("Order not found"));
            ledger.credit(UUID.randomUUID().toString(), new CreditRq(
                    o.getBuyerUserId(), o.getCurrencySell().name(), o.getSellAmount().subtract(o.getFeesBuyer()),
                    "ORDER-" + o.getId() + "-BUYER-CREDIT"));
            ledger.credit(UUID.randomUUID().toString(), new CreditRq(
                    o.getSellerUserId(), o.getCurrencyReceive().name(), o.getReceiveAmount().subtract(o.getFeesSeller()),
                    "ORDER-" + o.getId() + "-SELLER-CREDIT"));
            e.setStatus(EscrowStatus.RELEASED);
            o.setStatus(OrderStatus.RELEASED);
            repo.save(e);
            orders.save(o);
            receipts.generateAndStore(o.getId());
        }
    }
}
