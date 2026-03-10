/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

import com.financial.wealth.api.transactions.models.OtherBankTransferRequest;
import com.financial.wealth.api.transactions.security.consent.ConsentHashUtil;
import com.financial.wealth.api.transactions.security.consent.ConsentPayloadHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author olufemioshin
 */
@Component
public class BreezePayInterbankPaymentPayloadHasher implements ConsentPayloadHasher<OtherBankTransferRequest> {

    private static final Logger log = LoggerFactory.getLogger(BreezePayInterbankPaymentPayloadHasher.class);

    public String appJsonPayloadString(OtherBankTransferRequest r) {
        String json = "{"
                + "\"receiverBankAccount\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getReceiverBankAccount())) + "\","
                + "\"receiverAccountName\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getReceiverAccountName())) + "\","
                + "\"sender\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getSender())) + "\","
                + "\"amount\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getAmount())) + "\","
                + "\"processId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getProcessId())) + "\","
                + "\"transactionType\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getTransactionType())) + "\","
                + "\"fees\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getFees())) + "\","
                + "\"bankCode\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getBankCode())) + "\","
                + "\"bankName\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getBankName())) + "\""
                + "}";

        log.info("[CONSENT] interbank appJsonPayloadString={}", json);
        return json;
    }

    public String diagnosticCanonicalPayload(OtherBankTransferRequest r) {
        return "v1|INTERBANK"
                + "|processId=" + ConsentStringUtil.nz(r.getProcessId())
                + "|transactionType=" + ConsentStringUtil.nz(r.getTransactionType())
                + "|sender=" + ConsentStringUtil.nz(r.getSender())
                + "|receiverBankAccount=" + ConsentStringUtil.nz(r.getReceiverBankAccount())
                + "|receiverAccountName=" + ConsentStringUtil.nz(r.getReceiverAccountName())
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|fees=" + ConsentStringUtil.nz(r.getFees())
                + "|bankCode=" + ConsentStringUtil.nz(r.getBankCode())
                + "|bankName=" + ConsentStringUtil.nz(r.getBankName());
    }

    public String payloadHashB64(OtherBankTransferRequest r) {
        return ConsentHashUtil.sha256Base64(appJsonPayloadString(r));
    }
}
