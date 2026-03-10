/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.offer.CancelOfferCallerReq;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CancelOfferPayloadHasher extends BaseJsonConsentHasher<CancelOfferCallerReq> {

    private static final Logger log = LoggerFactory.getLogger(CancelOfferPayloadHasher.class);

    @Override
    public String appJsonPayloadString(CancelOfferCallerReq r) {

        String json = ConsentJsonBuilder.create()
                .addString("correlationId", r.getCorrelationId())
                .build();

        log.info("[CONSENT] cancelOffer appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(CancelOfferCallerReq r) {

        return "v1|CANCEL_OFFER"
                + "|correlationId=" + ConsentStringUtil.nz(r.getCorrelationId());
    }
}
