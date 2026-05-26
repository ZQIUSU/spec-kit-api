package com.creamlogin.app.security;

public record AuthPrincipal(Long userId, String username, String role) {}
