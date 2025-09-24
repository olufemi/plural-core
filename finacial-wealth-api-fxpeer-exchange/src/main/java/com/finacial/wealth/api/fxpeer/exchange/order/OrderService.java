/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

/**
 *
 * @author olufemioshin
 */

import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import com.finacial.wealth.api.fxpeer.exchange.offer.OfferRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OfferRepository offers;

    public OrderService(OrderRepository orders, OfferRepository offers) {
        this.orders = orders;
        this.offers = offers;
    }

    @Transactional
    public Order buyNow(long offerId, BigDecimal amount, long buyerId, long lockTtlSeconds) {
        Offer off = offers.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (off.getStatus() != OfferStatus.LIVE) {
            throw new BusinessException("Offer not LIVE");
        }
        if (off.getQtyAvailable().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient available amount");
        }

// Reserve available
        off.setQtyAvailable(off.getQtyAvailable().subtract(amount));
        offers.save(off);

// Compute receive = amount * rate
        BigDecimal receive = amount.multiply(off.getRate()).setScale(2, RoundingMode.HALF_UP);

        Order ord = new Order();
        ord.setOfferId(off.getId());
        ord.setSellerUserId(off.getSellerUserId());
        ord.setBuyerUserId(buyerId);
        ord.setCurrencySell(off.getCurrencySell());
        ord.setCurrencyReceive(off.getCurrencyReceive());
        ord.setSellAmount(amount);
        ord.setReceiveAmount(receive);
        ord.setRate(off.getRate());
        ord.setLockExpiresAt(Instant.now().plusSeconds(lockTtlSeconds));
        ord.setStatus(com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus.PENDING_ESCROW);
        return orders.save(ord);
    }
}
