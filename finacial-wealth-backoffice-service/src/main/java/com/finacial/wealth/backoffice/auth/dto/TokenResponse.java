package com.finacial.wealth.backoffice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
}
