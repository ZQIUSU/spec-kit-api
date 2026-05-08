package com.creamlogin.app.web.dto;

import java.util.Map;

public record BrandingResponse(
    String brandName,
    String welcomeTitle,
    String welcomeSubtitle,
    Map<String, AssetDto> assets) {

  public record AssetDto(String path, String url) {}
}
