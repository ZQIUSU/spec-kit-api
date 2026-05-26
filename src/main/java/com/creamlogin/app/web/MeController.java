package com.creamlogin.app.web;

import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.UserRepository;
import com.creamlogin.app.security.AuthPrincipal;
import com.creamlogin.app.security.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

  private final UserRepository userRepository;

  public MeController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public ResponseEntity<?> me() {
    AuthPrincipal p = CurrentUser.require();
    return userRepository
        .findById(p.userId())
        .<ResponseEntity<?>>map(
            u ->
                ResponseEntity.ok(
                    Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "role", u.getRole().name(),
                        "points", u.getPoints())))
        .orElseGet(() -> ResponseEntity.status(404).build());
  }

  @GetMapping("/leaderboard")
  public List<Map<String, Object>> leaderboard() {
    return userRepository.findAllByOrderByPointsDescIdAsc().stream()
        .limit(50)
        .map(this::toEntry)
        .toList();
  }

  @GetMapping("/admin/users")
  public List<Map<String, Object>> adminUsers() {
    AuthPrincipal me = CurrentUser.require();
    if (!"ADMIN".equals(me.role())) {
      throw new SecurityException("admin only");
    }
    return userRepository.findAll().stream().map(this::toEntry).toList();
  }

  private Map<String, Object> toEntry(User u) {
    return Map.of(
        "id", u.getId(),
        "username", u.getUsername(),
        "realName", u.getRealName() == null ? "" : u.getRealName(),
        "role", u.getRole().name(),
        "points", u.getPoints());
  }
}
