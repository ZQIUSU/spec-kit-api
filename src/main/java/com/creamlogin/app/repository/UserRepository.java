package com.creamlogin.app.repository;

import com.creamlogin.app.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByIdCardNumber(String idCardNumber);

  List<User> findAllByOrderByPointsDescIdAsc();
}
