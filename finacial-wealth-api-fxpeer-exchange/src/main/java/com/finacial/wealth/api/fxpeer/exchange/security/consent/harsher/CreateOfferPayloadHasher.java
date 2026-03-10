/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

import com.finacial.wealth.api.fxpeer.exchange.offer.CreateOfferCaller;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author olufemioshin
 */


@Component
public class CreateOfferPayloadHasher extends BaseJsonConsentHasher<CreateOfferCaller> {

    private static final Logger log = LoggerFactory.getLogger(CreateOfferPayloadHasher.class);

    @Override
    public String appJsonPayloadString(CreateOfferCaller r) {

        String json = ConsentJsonBuilder.create()
                .addString("currencySell", r.getCurrencySell())
                .addString("currencyReceive", r.getCurrencyReceive())
                .addString("rate", r.getRate())
                .addString("qtyTotal", r.getQtyTotal())
                .addString("expiredAt", r.getExpiredAt())
                .addString("minAmount", r.getMinAmount())
                .addBooleanAsString("showInTopDeals", r.isShowInTopDeals())
                .build();

        log.info("[CONSENT] createOffer appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(CreateOfferCaller r) {

        return "v1|CREATE_OFFER"
                + "|currencySell=" + ConsentStringUtil.nz(r.getCurrencySell())
                + "|currencyReceive=" + ConsentStringUtil.nz(r.getCurrencyReceive())
                + "|rate=" + ConsentStringUtil.nz(r.getRate())
                + "|qtyTotal=" + ConsentStringUtil.nz(r.getQtyTotal())
                + "|expiredAt=" + ConsentStringUtil.nz(r.getExpiredAt())
                + "|minAmount=" + ConsentStringUtil.nz(r.getMinAmount())
                + "|showInTopDeals=" + r.isShowInTopDeals();
    }
}
