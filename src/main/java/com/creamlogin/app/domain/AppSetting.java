package com.creamlogin.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_settings")
public class AppSetting {

  @Id
  @Column(name = "setting_key", length = 128)
  private String settingKey;

  @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
  private String settingValue;

  public String getSettingKey() {
    return settingKey;
  }

  public void setSettingKey(String settingKey) {
    this.settingKey = settingKey;
  }

  public String getSettingValue() {
    return settingValue;
  }

  public void setSettingValue(String settingValue) {
    this.settingValue = settingValue;
  }
}
