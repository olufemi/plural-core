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
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingService;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.SellerStats;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MarketFacade {

    private final OfferSearchService search;
    private final RatingService ratings;

    public MarketFacade(OfferSearchService search, RatingService ratings) {
        this.search = search;
        this.ratings = ratings;
    }

    public Page<OfferView> browse(Optional<CurrencyCode> ccySell,
            Optional<CurrencyCode> ccyRecv,
            Optional<BigDecimal> rateMin,
            Optional<BigDecimal> rateMax,
            Optional<BigDecimal> amountMin,
            int page, int size, String sort) {
        Page<Offer> p = search.search(ccySell, ccyRecv, rateMin, rateMax, amountMin, page, size, sort);
// Pre-compute stats per seller for the page (avoids N calls per item when seller repeats)
        Map<Long, SellerStats> statsBySeller = p.getContent().stream()
                .map(Offer::getSellerUserId)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), ratings::sellerStats));

        return p.map(o -> new OfferView(
                o.getId(), o.getSellerUserId(), o.getCurrencySell(), o.getCurrencyReceive(),
                o.getRate(), o.getQtyAvailable(), o.getQtyTotal(),
                statsBySeller.get(o.getSellerUserId())
        ));
    }
}
