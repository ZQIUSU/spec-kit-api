package com.creamlogin.app.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordRecoveryCipher {

  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private final SecretKey secretKey;

  public PasswordRecoveryCipher(
      @Value("${app.password-recovery.secret:}") String configuredSecret) {
    byte[] keyMaterial = resolveKeyBytes(configuredSecret);
    this.secretKey = new SecretKeySpec(keyMaterial, "AES");
  }

  private static byte[] resolveKeyBytes(String configuredSecret) {
    String effective =
        configuredSecret == null || configuredSecret.isBlank()
            ? "cream-login-dev-only-change-me"
            : configuredSecret;
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      return sha256.digest(effective.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to derive recovery cipher key", e);
    }
  }

  public String encrypt(String plainText) {
    if (plainText == null) {
      throw new IllegalArgumentException("plainText");
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      new SecureRandom().nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
      buf.put(iv);
      buf.put(cipherText);
      return Base64.getEncoder().encodeToString(buf.array());
    } catch (Exception e) {
      throw new IllegalStateException("encrypt failed", e);
    }
  }

  public String decrypt(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      throw new IllegalArgumentException("encoded");
    }
    try {
      byte[] decoded = Base64.getDecoder().decode(encoded);
      if (decoded.length < GCM_IV_LENGTH + 1) {
        throw new IllegalArgumentException("invalid payload");
      }
      ByteBuffer buf = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[GCM_IV_LENGTH];
      buf.get(iv);
      byte[] cipherBytes = new byte[buf.remaining()];
      buf.get(cipherBytes);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] plain = cipher.doFinal(cipherBytes);
      return new String(plain, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("decrypt failed", e);
    }
  }
}
