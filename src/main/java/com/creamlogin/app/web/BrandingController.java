package com.creamlogin.app.web;

import com.creamlogin.app.service.BrandingService;
import com.creamlogin.app.web.dto.BrandingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BrandingController {

  private final BrandingService brandingService;

  public BrandingController(BrandingService brandingService) {
    this.brandingService = brandingService;
  }

  @GetMapping("/api/branding")
  public BrandingResponse branding() {
    return brandingService.buildBranding();
  }
}
