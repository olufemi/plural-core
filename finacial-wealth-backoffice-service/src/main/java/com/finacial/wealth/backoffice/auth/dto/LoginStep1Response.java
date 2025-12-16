package com.finacial.wealth.backoffice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginStep1Response {
  private String status; // OK or MFA_REQUIRED
  private String mfaToken; // present when MFA_REQUIRED
}
