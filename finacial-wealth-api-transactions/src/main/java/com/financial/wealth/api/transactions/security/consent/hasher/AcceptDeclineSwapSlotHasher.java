/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

/**
 *
 * @author olufemioshin
 */

import com.financial.wealth.api.transactions.models.AcceptDeclineSwapSlotReq;
import com.financial.wealth.api.transactions.security.consent.BaseJsonConsentHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AcceptDeclineSwapSlotHasher extends BaseJsonConsentHasher<AcceptDeclineSwapSlotReq> {

    private static final Logger log = LoggerFactory.getLogger(AcceptDeclineSwapSlotHasher.class);

    @Override
    public String appJsonPayloadString(AcceptDeclineSwapSlotReq r) {

        String json = "{"
                + "\"memberId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberId())) + "\","
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"senderEmailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getSenderEmailAddress())) + "\","
                + "\"acceptOrDecline\":\"" + r.isAcceptOrDecline() + "\""
                + "}";

        log.info("[CONSENT] acceptDeclineSwapSlot appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(AcceptDeclineSwapSlotReq r) {

        return "v1|GROUPSAVINGS_ACCEPT_DECLINE_SWAP"
                + "|memberId=" + ConsentStringUtil.nz(r.getMemberId())
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|senderEmailAddress=" + ConsentStringUtil.nz(r.getSenderEmailAddress())
                + "|acceptOrDecline=" + r.isAcceptOrDecline();
    }
}
