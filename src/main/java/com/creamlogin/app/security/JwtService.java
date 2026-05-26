package com.creamlogin.app.security;

import com.creamlogin.app.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long ttlSeconds;

  public JwtService(
      @Value("${app.jwt.secret:cream-login-default-secret-change-me-please}") String secret,
      @Value("${app.jwt.ttl-seconds:604800}") long ttlSeconds) {
    this.key = Keys.hmacShaKeyFor(sha256(secret));
    this.ttlSeconds = ttlSeconds;
  }

  public String issue(long userId, String username, Role role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    return jws.getPayload();
  }

  private static byte[] sha256(String s) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
