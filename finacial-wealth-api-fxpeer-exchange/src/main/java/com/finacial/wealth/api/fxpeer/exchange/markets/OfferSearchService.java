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
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import com.finacial.wealth.api.fxpeer.exchange.offer.OfferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OfferSearchService {

    private final OfferRepository offers;
    private final OfferSpecs offerSpecs;

    public OfferSearchService(OfferRepository offers,
            OfferSpecs offerSpecs) {
        this.offers = offers;
        this.offerSpecs = offerSpecs;
    }

    public Page<Offer> search(Optional<CurrencyCode> ccySell,
            Optional<CurrencyCode> ccyRecv,
            Optional<BigDecimal> rateMin,
            Optional<BigDecimal> rateMax,
            Optional<BigDecimal> amountMin,
            int page, int size, String sort) {
        Specification<Offer> spec = offerSpecs.statusLive();
        if (ccySell.isPresent()) {
            spec = spec.and(offerSpecs.currencySell(ccySell.get()));
        }
        if (ccyRecv.isPresent()) {
            spec = spec.and(offerSpecs.currencyReceive(ccyRecv.get()));
        }
        spec = spec.and(offerSpecs.rateBetween(rateMin.orElse(null), rateMax.orElse(null)));
        spec = spec.and(offerSpecs.qtyAtLeast(amountMin.orElse(null)));

        Sort s = switch (sort == null ? "" : sort) {
            case "bestRate" ->
                Sort.by(Sort.Direction.ASC, "rate");
            case "newest" ->
                Sort.by(Sort.Direction.DESC, "createdAt");
            default ->
                Sort.by(Sort.Direction.ASC, "rate");
        };
        Pageable p = PageRequest.of(page, size, s);
        return offers.findAll(spec, p);
    }
}
