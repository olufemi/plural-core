package com.finacial.wealth.backoffice.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoBox {

  private static final String AES = "AES";
  private static final String AES_GCM = "AES/GCM/NoPadding";

  private final byte[] masterKey; // 32 bytes

  public CryptoBox(byte[] masterKey) {
    if (masterKey == null || masterKey.length != 32) {
      throw new IllegalArgumentException("masterKey must be 32 bytes (AES-256)");
    }
    this.masterKey = masterKey;
  }

  public record EncResult(String cipherTextBase64, String ivBase64) {}

  public EncResult encrypt(String plain) {
    try {
      byte[] iv = new byte[12];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(AES_GCM);
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKey, AES), new GCMParameterSpec(128, iv));
      byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

      return new EncResult(Base64.getEncoder().encodeToString(ct), Base64.getEncoder().encodeToString(iv));
    } catch (Exception e) {
      throw new IllegalStateException("encrypt failed", e);
    }
  }

  public String decrypt(String cipherTextBase64, String ivBase64) {
    try {
      byte[] iv = Base64.getDecoder().decode(ivBase64);
      byte[] ct = Base64.getDecoder().decode(cipherTextBase64);

      Cipher cipher = Cipher.getInstance(AES_GCM);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, AES), new GCMParameterSpec(128, iv));
      byte[] pt = cipher.doFinal(ct);

      return new String(pt, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("decrypt failed", e);
    }
  }
}
