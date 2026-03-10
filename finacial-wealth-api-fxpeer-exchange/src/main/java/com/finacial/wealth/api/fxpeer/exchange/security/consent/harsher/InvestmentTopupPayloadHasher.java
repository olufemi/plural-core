/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentTopupRequestCaller;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvestmentTopupPayloadHasher extends BaseJsonConsentHasher<InvestmentTopupRequestCaller> {

    private static final Logger log = LoggerFactory.getLogger(InvestmentTopupPayloadHasher.class);

    @Override
    public String appJsonPayloadString(InvestmentTopupRequestCaller r) {

        String json = ConsentJsonBuilder.create()
                .addString("orderRef", r.getOrderRef())
                .addString("amount", r.getAmount())
                .addString("currencyCode", r.getCurrencyCode())
                .addString("productId", r.getProductId())
                .build();

        log.info("[CONSENT] investmentTopup appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(InvestmentTopupRequestCaller r) {

        return "v1|INVESTMENT_TOPUP"
                + "|orderRef=" + ConsentStringUtil.nz(r.getOrderRef())
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|currencyCode=" + ConsentStringUtil.nz(r.getCurrencyCode())
                + "|productId=" + ConsentStringUtil.nz(r.getProductId());
    }
}
