package com.creamlogin.app.config;

import com.creamlogin.app.domain.AppSetting;
import com.creamlogin.app.domain.AssetMetadata;
import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.AppSettingRepository;
import com.creamlogin.app.repository.AssetMetadataRepository;
import com.creamlogin.app.repository.UserRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

  @Bean
  CommandLineRunner seedUsers(
      UserRepository userRepository,
      AppSettingRepository appSettingRepository,
      AssetMetadataRepository assetMetadataRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.demo-user.password:password123}") String demoPassword) {
    return args -> {
      if (userRepository.count() == 0) {
        User u = new User();
        u.setEmail("demo@example.com");
        u.setPasswordHash(passwordEncoder.encode(demoPassword));
        u.setCreatedAt(Instant.now());
        userRepository.save(u);
      }

      if (appSettingRepository.count() == 0) {
        saveSetting(appSettingRepository, "brand_name", "Aether");
        saveSetting(appSettingRepository, "welcome_title", "Welcome Back");
        saveSetting(
            appSettingRepository,
            "welcome_subtitle",
            "Log in to your Agent Dashboard");
      }

      if (assetMetadataRepository.count() == 0) {
        AssetMetadata logo = new AssetMetadata();
        logo.setLogicalKey("logo");
        logo.setFilesystemPath("public/branding/logo.svg");
        logo.setMimeType("image/svg+xml");
        logo.setUpdatedAt(Instant.now());
        assetMetadataRepository.save(logo);
      }
    };
  }

  private static void saveSetting(AppSettingRepository repo, String key, String value) {
    AppSetting s = new AppSetting();
    s.setSettingKey(key);
    s.setSettingValue(value);
    repo.save(s);
  }
}
