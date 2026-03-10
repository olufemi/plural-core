/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.LeaveGroupRequest;
import com.financial.wealth.api.transactions.security.consent.BaseJsonConsentHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LeaveGroupPayloadHasher extends BaseJsonConsentHasher<LeaveGroupRequest> {

    private static final Logger log = LoggerFactory.getLogger(LeaveGroupPayloadHasher.class);

    @Override
    public String appJsonPayloadString(LeaveGroupRequest r) {

        String json = "{"
                + "\"memberEmailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberEmailAddress())) + "\","
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"memberId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getMemberId())) + "\""
                + "}";

        log.info("[CONSENT] leaveGroup appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(LeaveGroupRequest r) {

        return "v1|LEAVEGROUP"
                + "|memberEmailAddress=" + ConsentStringUtil.nz(r.getMemberEmailAddress())
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|memberId=" + ConsentStringUtil.nz(r.getMemberId());
    }
}
