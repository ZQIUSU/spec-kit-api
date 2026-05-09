package com.creamlogin.app.service;

import com.creamlogin.app.crypto.PasswordRecoveryCipher;
import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.UserRepository;
import com.creamlogin.app.validation.IdCardValidator;
import com.creamlogin.app.web.dto.LoginResponse;
import com.creamlogin.app.web.dto.RegisterRequest;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordRecoveryCipher passwordRecoveryCipher;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PasswordRecoveryCipher passwordRecoveryCipher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordRecoveryCipher = passwordRecoveryCipher;
  }

  public Optional<LoginResponse> login(String username, String rawPassword) {
    String key = normalizeUsername(username);
    return userRepository
        .findByUsername(key)
        .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
        .map(
            u ->
                new LoginResponse(
                    true, new LoginResponse.UserDto(u.getId(), u.getUsername())));
  }

  @Transactional
  public LoginResponse register(RegisterRequest request) {
    String username = normalizeUsername(request.username());
    String idCard = IdCardValidator.normalize(request.idCardNumber());
    String realName = request.realName().trim();

    if (!IdCardValidator.isValid(idCard)) {
      throw new IllegalArgumentException("Invalid ID card number");
    }
    if (userRepository.existsByUsername(username)) {
      throw new ConflictException("username_taken", "Username already taken");
    }
    if (userRepository.existsByIdCardNumber(idCard)) {
      throw new ConflictException("id_card_taken", "ID card already registered");
    }

    User u = new User();
    u.setUsername(username);
    u.setRealName(realName);
    u.setIdCardNumber(idCard);
    u.setPasswordHash(passwordEncoder.encode(request.password()));
    u.setPasswordRecoveryEnc(passwordRecoveryCipher.encrypt(request.password()));
    userRepository.save(u);

    return new LoginResponse(true, new LoginResponse.UserDto(u.getId(), u.getUsername()));
  }

  @Transactional(readOnly = true)
  public Optional<String> recoverPassword(
      String username, String realName, String idCardNumber) {
    String idCard = IdCardValidator.normalize(idCardNumber);
    if (!IdCardValidator.isValid(idCard)) {
      return Optional.empty();
    }
    String key = normalizeUsername(username);
    String name = realName.trim();

    Optional<User> found = userRepository.findByUsername(key);
    if (found.isEmpty()) {
      return Optional.empty();
    }
    User u = found.get();
    if (!u.getRealName().trim().equals(name)) {
      return Optional.empty();
    }
    if (!u.getIdCardNumber().equalsIgnoreCase(idCard)) {
      return Optional.empty();
    }
    String plain = passwordRecoveryCipher.decrypt(u.getPasswordRecoveryEnc());
    return Optional.of("你的密码是：" + plain);
  }

  private static String normalizeUsername(String raw) {
    return raw == null ? "" : raw.trim().toLowerCase();
  }
}
