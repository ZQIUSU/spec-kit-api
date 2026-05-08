package com.creamlogin.app.service;

import com.creamlogin.app.domain.AssetMetadata;
import com.creamlogin.app.repository.AppSettingRepository;
import com.creamlogin.app.repository.AssetMetadataRepository;
import com.creamlogin.app.web.dto.BrandingResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrandingService {

  private final AppSettingRepository appSettingRepository;
  private final AssetMetadataRepository assetMetadataRepository;
  /** Root of the frontend repo (contains public/, src/) — sibling directory in dev. */
  private final Path frontendAssetsBase;

  public BrandingService(
      AppSettingRepository appSettingRepository,
      AssetMetadataRepository assetMetadataRepository,
      @Value("${app.frontend-assets.base-path}") String frontendAssetsBaseRaw) {
    this.appSettingRepository = appSettingRepository;
    this.assetMetadataRepository = assetMetadataRepository;
    this.frontendAssetsBase =
        Path.of(frontendAssetsBaseRaw.trim()).toAbsolutePath().normalize();
  }

  public BrandingResponse buildBranding() {
    Map<String, String> settings = new HashMap<>();
    appSettingRepository
        .findAll()
        .forEach(s -> settings.put(s.getSettingKey(), s.getSettingValue()));

    String brandName = settings.getOrDefault("brand_name", "Aether");
    String welcomeTitle = settings.getOrDefault("welcome_title", "Welcome Back");
    String welcomeSubtitle =
        settings.getOrDefault("welcome_subtitle", "Log in to your Agent Dashboard");

    Map<String, BrandingResponse.AssetDto> assets = new HashMap<>();

    for (AssetMetadata row : assetMetadataRepository.findAll()) {
      String rel = row.getFilesystemPath().replace('\\', '/');
      if (!isAllowedPath(rel)) {
        continue;
      }
      Path abs = frontendAssetsBase.resolve(rel);
      if (!Files.isRegularFile(abs)) {
        continue;
      }
      String url = toPublicUrl(rel);
      if (url != null) {
        assets.put(row.getLogicalKey(), new BrandingResponse.AssetDto(rel, url));
      }
    }

    return new BrandingResponse(brandName, welcomeTitle, welcomeSubtitle, assets);
  }

  static boolean isAllowedPath(String relative) {
    return relative.startsWith("public/") || relative.startsWith("src/assets/");
  }

  static String toPublicUrl(String filesystemPath) {
    if (filesystemPath.startsWith("public/")) {
      return "/" + filesystemPath.substring("public/".length());
    }
    return null;
  }
}
