/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

import com.financial.wealth.api.transactions.models.SwapSlotReq;
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
public class GroupSavingsSwapSlotRequestHasher extends BaseJsonConsentHasher<SwapSlotReq> {

    private static final Logger log = LoggerFactory.getLogger(GroupSavingsSwapSlotRequestHasher.class);

    @Override
    public String appJsonPayloadString(SwapSlotReq r) {

        String json = "{"
                + "\"memberId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberId())) + "\","
                + "\"senderSlot\":\"" + r.getSenderSlot() + "\","
                + "\"receiverSlot\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getReceiverSlot())) + "\","
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"senderEmailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getSenderEmailAddress())) + "\""
                + "}";

        log.info("[CONSENT] swapSlotRequest appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(SwapSlotReq r) {

        return "v1|GROUPSAVINGS_SWAP_SLOT"
                + "|memberId=" + ConsentStringUtil.nz(r.getMemberId())
                + "|senderSlot=" + r.getSenderSlot()
                + "|receiverSlot=" + ConsentStringUtil.nz(r.getReceiverSlot())
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|senderEmailAddress=" + ConsentStringUtil.nz(r.getSenderEmailAddress());
    }
}
