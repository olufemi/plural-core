/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.investment.record.CreateSubscriptionReq;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateSubscriptionPayloadHasher extends BaseJsonConsentHasher<CreateSubscriptionReq> {

    private static final Logger log = LoggerFactory.getLogger(CreateSubscriptionPayloadHasher.class);

    @Override
    public String appJsonPayloadString(CreateSubscriptionReq r) {

        String json = ConsentJsonBuilder.create()
                .addString("productId", r.getProductId())
                .addString("amount", r.getAmount())
                .addString("currencyCode", r.getCurrencyCode())
                .build();

        log.info("[CONSENT] createSubscription appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(CreateSubscriptionReq r) {

        return "v1|CREATE_SUBSCRIPTION"
                + "|productId=" + ConsentStringUtil.nz(r.getProductId())
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|currencyCode=" + ConsentStringUtil.nz(r.getCurrencyCode());
    }
}
