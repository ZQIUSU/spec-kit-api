package com.creamlogin.app.web;

import com.creamlogin.app.service.AuthService;
import com.creamlogin.app.web.dto.LoginRequest;
import com.creamlogin.app.web.dto.LoginResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/api/auth/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    return authService
        .login(request.email(), request.password())
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(
            () ->
                ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials", "code", "unauthorized")));
  }
}
