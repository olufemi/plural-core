package com.finacial.wealth.backoffice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequest {
  @NotBlank
  private String mfaToken;

  @NotBlank
  private String totpCode;
}
