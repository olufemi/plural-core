/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.AcceptQuoteFE;

import com.financial.wealth.api.transactions.security.consent.ConsentHashUtil;
import com.financial.wealth.api.transactions.security.consent.ConsentPayloadHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AcceptQuotePayloadHasher implements ConsentPayloadHasher<AcceptQuoteFE> {

    private static final Logger log = LoggerFactory.getLogger(AcceptQuotePayloadHasher.class);

    @Override
    public String appJsonPayloadString(AcceptQuoteFE r) {
        String json = "{"
                + "\"accepted\":\"" + r.isAccepted() + "\","
                + "\"quoteId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getQuoteId())) + "\""
                + "}";

        log.info("[CONSENT] acceptQuote appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(AcceptQuoteFE r) {
        return "v1|ACCEPTQUOTE"
                + "|quoteId=" + ConsentStringUtil.nz(r.getQuoteId())
                + "|accepted=" + r.isAccepted();
    }

    @Override
    public String payloadHashB64(AcceptQuoteFE r) {
        return ConsentHashUtil.sha256Base64(appJsonPayloadString(r));
    }
}
