package com.creamlogin.app.web;

import com.creamlogin.app.service.AuthService;
import com.creamlogin.app.web.dto.LoginRequest;
import com.creamlogin.app.web.dto.LoginResponse;
import com.creamlogin.app.web.dto.RecoverPasswordRequest;
import com.creamlogin.app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
        .login(request.username(), request.password())
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(
            () ->
                ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials", "code", "unauthorized")));
  }

  @PostMapping("/api/auth/register")
  public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    LoginResponse body = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @PostMapping("/api/auth/recover-password")
  public ResponseEntity<?> recover(@Valid @RequestBody RecoverPasswordRequest request) {
    return authService
        .recoverPassword(request.username(), request.realName(), request.idCardNumber())
        .<ResponseEntity<?>>map(
            msg -> ResponseEntity.ok(Map.of("ok", true, "message", msg)))
        .orElseGet(
            () ->
                ResponseEntity.status(401)
                    .body(
                        Map.of(
                            "error",
                            "Unable to verify identity",
                            "code",
                            "recovery_failed")));
  }
}
