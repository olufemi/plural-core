/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.ledger.LedgerClient;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.WalletInfo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OfferService {

    private final OfferRepository repo;
    private final LedgerClient ledger;

    public OfferService(OfferRepository repo, LedgerClient ledger) {
        this.repo = repo;
        this.ledger = ledger;
    }

    @Transactional
    public Offer createOffer(CreateOfferRq rq, long sellerId) {
        ledger.ensureWallet(sellerId, rq.currencySell().name());
        ledger.ensureWallet(sellerId, rq.currencyReceive().name());
        WalletInfo w = ledger.getWallet(sellerId, rq.currencySell().name());
        if (w.available().compareTo(rq.qtyTotal()) < 0) {
            throw new BusinessException("Insufficient balance to list qtyTotal");
        }
        Offer o = new Offer();
        o.setSellerUserId(sellerId);
        o.setCurrencySell(rq.currencySell());
        o.setCurrencyReceive(rq.currencyReceive());
        o.setRate(rq.rate());
        o.setQtyTotal(rq.qtyTotal());
        o.setQtyAvailable(rq.qtyTotal());
        o.setStatus(OfferStatus.LIVE);
        return repo.save(o);
    }

    @Transactional
    public Offer updateRate(long offerId, BigDecimal newRate, long sellerId) {
        Offer o = repo.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (!o.getSellerUserId().equals(sellerId)) {
            throw new BusinessException("Forbidden");
        }
        if (o.getStatus() != OfferStatus.LIVE) {
            throw new BusinessException("Only LIVE offers can be updated");
        }
        o.setRate(newRate);
        return repo.save(o);
    }

    @Transactional
    public void cancel(long offerId, long sellerId) {
        Offer o = repo.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (!o.getSellerUserId().equals(sellerId)) {
            throw new BusinessException("Forbidden");
        }
        o.setStatus(OfferStatus.CANCELLED);
        repo.save(o);
    }
}
