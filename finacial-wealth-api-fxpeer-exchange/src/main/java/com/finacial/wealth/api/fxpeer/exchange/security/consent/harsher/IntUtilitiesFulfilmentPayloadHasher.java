/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */

import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ProcessTrnsactionReq;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IntUtilitiesFulfilmentPayloadHasher extends BaseJsonConsentHasher<ProcessTrnsactionReq> {

    private static final Logger log = LoggerFactory.getLogger(IntUtilitiesFulfilmentPayloadHasher.class);

    @Override
    public String appJsonPayloadString(ProcessTrnsactionReq r) {

        String json = ConsentJsonBuilder.create()
                .addString("operator", r.getOperator())
                .addString("product", r.getProduct())
                .addString("recipient", r.getRecipient())
                .addString("amount", r.getAmount())
                .addString("currencyCode", r.getCurrencyCode())
                .build();

        log.info("[CONSENT] intUtilitiesFulfilment appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(ProcessTrnsactionReq r) {

        return "v1|INT_UTILITIES_FULFILMENT"
                + "|operator=" + ConsentStringUtil.nz(r.getOperator())
                + "|product=" + ConsentStringUtil.nz(r.getProduct())
                + "|recipient=" + ConsentStringUtil.nz(r.getRecipient())
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|currencyCode=" + ConsentStringUtil.nz(r.getCurrencyCode());
    }
}
