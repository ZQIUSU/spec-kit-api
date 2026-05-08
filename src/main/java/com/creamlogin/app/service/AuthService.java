package com.creamlogin.app.service;

import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.UserRepository;
import com.creamlogin.app.web.dto.LoginResponse;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<LoginResponse> login(String email, String rawPassword) {
    return userRepository
        .findByEmail(email.trim().toLowerCase())
        .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
        .map(
            u ->
                new LoginResponse(
                    true,
                    new LoginResponse.UserDto(u.getId(), u.getEmail())));
  }
}
