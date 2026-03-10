/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

import com.financial.wealth.api.transactions.models.JoinGroupRequest;
import com.financial.wealth.api.transactions.security.consent.BaseJsonConsentHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author olufemioshin
 */


@Component
public class JoinGroupPayloadHasher extends BaseJsonConsentHasher<JoinGroupRequest> {

    private static final Logger log = LoggerFactory.getLogger(JoinGroupPayloadHasher.class);

    @Override
    public String appJsonPayloadString(JoinGroupRequest r) {
        String json = "{"
                + "\"memberId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberId())) + "\","
                + "\"memberType\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberType())) + "\","
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"memberEmailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberEmailAddress())) + "\","
                + "\"selectedSlot\":\"" + r.getSelectedSlot() + "\""
                + "}";

        log.info("[CONSENT] joinGroup appJsonPayloadString={}", json);
        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(JoinGroupRequest r) {
        return "v1|JOINGROUP"
                + "|memberId=" + ConsentStringUtil.nz(r.getMemberId())
                + "|memberType=" + ConsentStringUtil.nz(r.getMemberType())
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|memberEmailAddress=" + ConsentStringUtil.nz(r.getMemberEmailAddress())
                + "|selectedSlot=" + r.getSelectedSlot();
    }
}
