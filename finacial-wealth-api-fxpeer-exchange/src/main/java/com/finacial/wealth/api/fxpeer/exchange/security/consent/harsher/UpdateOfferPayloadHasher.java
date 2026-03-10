/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

import com.finacial.wealth.api.fxpeer.exchange.offer.UpdateOfferCallerReq;
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
public class UpdateOfferPayloadHasher extends BaseJsonConsentHasher<UpdateOfferCallerReq> {

    private static final Logger log = LoggerFactory.getLogger(UpdateOfferPayloadHasher.class);

    @Override
    public String appJsonPayloadString(UpdateOfferCallerReq r) {

        String json = ConsentJsonBuilder.create()
                .addString("newRate", r.getNewRate())
                .addString("correlationId", r.getCorrelationId())
                .addString("minAmount", r.getMinAmount())
                .addString("maxAmount", r.getMaxAmount())
                .build();

        log.info("[CONSENT] updateOffer appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(UpdateOfferCallerReq r) {

        return "v1|UPDATE_OFFER"
                + "|newRate=" + ConsentStringUtil.nz(r.getNewRate())
                + "|correlationId=" + ConsentStringUtil.nz(r.getCorrelationId())
                + "|minAmount=" + ConsentStringUtil.nz(r.getMinAmount())
                + "|maxAmount=" + ConsentStringUtil.nz(r.getMaxAmount());
    }
}
