package com.creamlogin.app.repository;

import com.creamlogin.app.domain.Reward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, Long> {
  List<Reward> findAllByEnabledTrueOrderByIdAsc();
}
