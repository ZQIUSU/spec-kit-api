package com.creamlogin.app.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

  private CurrentUser() {}

  public static AuthPrincipal require() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal p)) {
      throw new IllegalStateException("No authenticated user");
    }
    return p;
  }
}
