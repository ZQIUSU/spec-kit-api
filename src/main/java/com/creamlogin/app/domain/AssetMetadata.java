package com.creamlogin.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "asset_metadata")
public class AssetMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "logical_key", nullable = false, unique = true)
  private String logicalKey;

  @Column(name = "filesystem_path", nullable = false, length = 512)
  private String filesystemPath;

  @Column(name = "mime_type", length = 128)
  private String mimeType;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public String getLogicalKey() {
    return logicalKey;
  }

  public void setLogicalKey(String logicalKey) {
    this.logicalKey = logicalKey;
  }

  public String getFilesystemPath() {
    return filesystemPath;
  }

  public void setFilesystemPath(String filesystemPath) {
    this.filesystemPath = filesystemPath;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
