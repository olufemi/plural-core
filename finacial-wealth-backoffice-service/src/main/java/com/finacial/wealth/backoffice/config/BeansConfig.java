package com.finacial.wealth.backoffice.config;

import com.finacial.wealth.backoffice.util.CryptoBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class BeansConfig {

  @Bean
  public CryptoBox cryptoBox(@Value("${bo.crypto.master-key-base64}") String masterKeyB64) {
    byte[] key = Base64.getDecoder().decode(masterKeyB64);
    return new CryptoBox(key);
  }
}
