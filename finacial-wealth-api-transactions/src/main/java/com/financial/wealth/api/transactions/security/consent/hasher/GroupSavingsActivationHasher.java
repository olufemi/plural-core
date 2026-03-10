/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

import com.financial.wealth.api.transactions.models.GroupSavingActivation;
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
public class GroupSavingsActivationHasher extends BaseJsonConsentHasher<GroupSavingActivation> {

    private static final Logger log = LoggerFactory.getLogger(GroupSavingsActivationHasher.class);

    @Override
    public String appJsonPayloadString(GroupSavingActivation r) {

        String json = "{"
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"emailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getEmailAddress())) + "\","
                + "\"activationDate\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getActivationDate())) + "\""
                + "}";

        log.info("[CONSENT] groupSavingsActivation appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(GroupSavingActivation r) {

        return "v1|GROUPSAVINGS_ACTIVATE"
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|emailAddress=" + ConsentStringUtil.nz(r.getEmailAddress())
                + "|activationDate=" + ConsentStringUtil.nz(r.getActivationDate());
    }
}
