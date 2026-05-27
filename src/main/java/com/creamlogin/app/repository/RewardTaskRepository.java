package com.creamlogin.app.repository;

import com.creamlogin.app.domain.RewardTask;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardTaskRepository extends JpaRepository<RewardTask, Long> {

  List<RewardTask> findAllByEnabledTrueAndScheduledDateOrderByIdAsc(LocalDate date);

  List<RewardTask> findAllByEnabledTrueAndScheduledDateIsNullOrderByIdAsc();
}
