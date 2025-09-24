/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class OfferSpecs {

    private OfferSpecs() {
    }

    public static Specification<Offer> statusLive() {
        return (root, q, cb) -> cb.equal(root.get("status"), OfferStatus.LIVE);
    }

    public static Specification<Offer> currencySell(CurrencyCode code) {
        return (root, q, cb) -> cb.equal(root.get("currencySell"), code);
    }

    public static Specification<Offer> currencyReceive(CurrencyCode code) {
        return (root, q, cb) -> cb.equal(root.get("currencyReceive"), code);
    }

    public static Specification<Offer> rateBetween(BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("rate"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("rate"), min);
            }
            if (max != null) {
                return cb.lessThanOrEqualTo(root.get("rate"), max);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Offer> qtyAtLeast(BigDecimal amount) {
        return (root, q, cb) -> amount == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("qtyAvailable"), amount);
    }
}
