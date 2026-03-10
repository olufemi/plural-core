/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent.hasher;

/**
 *
 * @author olufemioshin
 */

import com.financial.wealth.api.transactions.models.GroupSavingConf;
import com.financial.wealth.api.transactions.security.consent.BaseJsonConsentHasher;
import com.financial.wealth.api.transactions.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GroupSavingsConfirmCreateHasher extends BaseJsonConsentHasher<GroupSavingConf> {

    private static final Logger log = LoggerFactory.getLogger(GroupSavingsConfirmCreateHasher.class);

    @Override
    public String appJsonPayloadString(GroupSavingConf r) {

        String json = "{"
                + "\"invitationCodeReqId\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getInvitationCodeReqId())) + "\","
                + "\"emailAddress\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(r.getEmailAddress())) + "\""
                + "}";

        log.info("[CONSENT] groupSavingsConfirmCreate appJsonPayloadString={}", json);

        return json;
    }

    @Override
    public String diagnosticCanonicalPayload(GroupSavingConf r) {

        return "v1|GROUPSAVINGS_CONFIRM_CREATE"
                + "|invitationCodeReqId=" + ConsentStringUtil.nz(r.getInvitationCodeReqId())
                + "|emailAddress=" + ConsentStringUtil.nz(r.getEmailAddress());
    }
}
