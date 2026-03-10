/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.LocalTransferRequest;
import com.financial.wealth.api.transactions.security.consent.ConsentHashUtil;
import com.financial.wealth.api.transactions.security.consent.ConsentPayloadHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalTransferPayloadHasher implements ConsentPayloadHasher<LocalTransferRequest> {

    private static final Logger log = LoggerFactory.getLogger(LocalTransferPayloadHasher.class);

    @Override
    public String appJsonPayloadString(LocalTransferRequest r) {
        String json = "{"
                + "\"receiver\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getReceiver())) + "\","
                + "\"receiverName\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getReceiverName())) + "\","
                + "\"sender\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getSender())) + "\","
                + "\"amount\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getAmount())) + "\","
                + "\"fees\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getFees())) + "\","
                + "\"theNarration\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getTheNarration())) + "\","
                + "\"processId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getProcessId())) + "\","
                + "\"transactionType\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getTransactionType())) + "\""
                + "}";

        log.info("[CONSENT] localTransfer appJsonPayloadString={}", json);
        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(LocalTransferRequest r) {
        return "v1|LOCALTRANSFER"
                + "|refId=" + ConsentStringUtil.nz(r.getProcessId())
                + "|transactionType=" + ConsentStringUtil.nz(r.getTransactionType())
                + "|sender=" + ConsentStringUtil.nz(r.getSender())
                + "|receiver=" + ConsentStringUtil.nz(r.getReceiver())
                + "|receiverName=" + ConsentStringUtil.nz(r.getReceiverName())
                + "|amount=" + ConsentStringUtil.nz(r.getAmount())
                + "|fees=" + ConsentStringUtil.nz(r.getFees())
                + "|theNarration=" + ConsentStringUtil.nz(r.getTheNarration());
    }

    @Override
    public String payloadHashB64(LocalTransferRequest r) {
        return ConsentHashUtil.sha256Base64(appJsonPayloadString(r));
    }
}
