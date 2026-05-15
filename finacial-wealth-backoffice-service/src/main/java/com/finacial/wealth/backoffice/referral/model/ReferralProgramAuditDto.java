package com.finacial.wealth.backoffice.referral.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralProgramAuditDto {

    private Long id;
    private Long programId;
    private String eventType;
    private String actor;
    private String note;
    private Date eventAt;
}
