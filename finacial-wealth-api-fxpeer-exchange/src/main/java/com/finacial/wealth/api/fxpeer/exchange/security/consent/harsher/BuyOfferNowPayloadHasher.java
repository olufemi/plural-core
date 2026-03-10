/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.order.BuyOfferNow;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BuyOfferNowPayloadHasher extends BaseJsonConsentHasher<BuyOfferNow> {

    private static final Logger log = LoggerFactory.getLogger(BuyOfferNowPayloadHasher.class);

    @Override
    public String appJsonPayloadString(BuyOfferNow r) {

        String json = ConsentJsonBuilder.create()
                .addString("amount", r.getAmount())
                .addString("referralCode", r.getReferralCode())
                .addString("offerCorrelationId", r.getOfferCorrelationId())
                .build();

        log.info("[CONSENT] buyOfferNow appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(BuyOfferNow r) {

        return "v1|BUY_OFFER_NOW"
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|referralCode=" + ConsentStringUtil.nz(r.getReferralCode())
                + "|offerCorrelationId=" + ConsentStringUtil.nz(r.getOfferCorrelationId());
    }
}
