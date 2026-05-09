package com.creamlogin.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Login handle (product term: username). Persisted in column {@code username}. Some databases
   * also retain legacy column {@code email} — see {@link #legacyEmail}.
   */
  @Column(name = "username", nullable = false, unique = true, length = 255)
  private String username;

  /**
   * Legacy column kept in sync with {@link #username} until a one-shot DB migration drops {@code
   * email}.
   */
  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String legacyEmail;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "password_recovery_enc", nullable = false, length = 512)
  private String passwordRecoveryEnc;

  @Column(name = "real_name", nullable = false, length = 64)
  private String realName;

  @Column(name = "id_card_number", nullable = false, unique = true, length = 18)
  private String idCardNumber;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @PostLoad
  void hydrateFromLegacyEmail() {
    if (username == null || username.isBlank()) {
      if (legacyEmail != null && !legacyEmail.isBlank()) {
        username = legacyEmail.trim().toLowerCase();
      }
    }
  }

  @PrePersist
  @PreUpdate
  void syncLegacyEmailColumn() {
    if (username != null && !username.isBlank()) {
      legacyEmail = username;
    } else if (legacyEmail != null && !legacyEmail.isBlank()) {
      username = legacyEmail.trim().toLowerCase();
      legacyEmail = username;
    }
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username == null ? null : username.trim().toLowerCase();
    if (this.username != null && !this.username.isBlank()) {
      this.legacyEmail = this.username;
    }
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getPasswordRecoveryEnc() {
    return passwordRecoveryEnc;
  }

  public void setPasswordRecoveryEnc(String passwordRecoveryEnc) {
    this.passwordRecoveryEnc = passwordRecoveryEnc;
  }

  public String getRealName() {
    return realName;
  }

  public void setRealName(String realName) {
    this.realName = realName;
  }

  public String getIdCardNumber() {
    return idCardNumber;
  }

  public void setIdCardNumber(String idCardNumber) {
    this.idCardNumber = idCardNumber;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
