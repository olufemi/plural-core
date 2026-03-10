/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.security.hasher;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.models.ChangePasswordInApp;
import com.finacial.wealth.api.profiling.security.consent.BaseJsonConsentHasher;
import com.finacial.wealth.api.profiling.security.consent.ConsentJsonBuilder;
import com.finacial.wealth.api.profiling.security.consent.ConsentStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordPayloadHasher extends BaseJsonConsentHasher<ChangePasswordInApp> {

    private static final Logger log = LoggerFactory.getLogger(ChangePasswordPayloadHasher.class);

    @Override
    public String appJsonPayloadString(ChangePasswordInApp r) {
        String json = ConsentJsonBuilder.create()
                .addString("emailAddress", r.getEmailAddress())
                .addString("oldPassword", r.getOldPassword())
                .addString("newPassword", r.getNewPassword())
                .addString("uuid", r.getUuid())
                .build();

        String maskedJson = ConsentJsonBuilder.create()
                .addString("emailAddress", r.getEmailAddress())
                .addString("oldPassword", "***")
                .addString("newPassword", "***")
                .addString("uuid", r.getUuid())
                .build();

        log.info("[CONSENT] changePassword appJsonPayloadString={}", maskedJson);
        return json;
    }
}
