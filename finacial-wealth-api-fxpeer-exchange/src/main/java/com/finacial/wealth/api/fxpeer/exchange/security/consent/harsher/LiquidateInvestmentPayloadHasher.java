/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.harsher;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.investment.record.LiquidateInvestmentRequest;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentStringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LiquidateInvestmentPayloadHasher extends BaseJsonConsentHasher<LiquidateInvestmentRequest> {

    private static final Logger log = LoggerFactory.getLogger(LiquidateInvestmentPayloadHasher.class);

    @Override
    public String appJsonPayloadString(LiquidateInvestmentRequest r) {

        String json = ConsentJsonBuilder.create()
                .addString("liquidationAmount",
                        r.liquidationAmount() == null ? "" : r.liquidationAmount().toPlainString())
                .addString("orderId", r.orderId())
                .addBooleanAsString("fullLiquidation", r.fullLiquidation())
                .build();

        log.info("[CONSENT] requestLiquidation appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(LiquidateInvestmentRequest r) {

        return "v1|REQUEST_LIQUIDATION"
                + "|liquidationAmount=" + (r.liquidationAmount() == null ? "" : r.liquidationAmount())
                + "|orderId=" + ConsentStringUtil.nz(r.orderId())
                + "|fullLiquidation=" + r.fullLiquidation();
    }
}
