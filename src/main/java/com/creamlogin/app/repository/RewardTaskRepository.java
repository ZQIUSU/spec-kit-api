package com.creamlogin.app.repository;

import com.creamlogin.app.domain.RewardTask;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardTaskRepository extends JpaRepository<RewardTask, Long> {

  /** 今日任务 = 启用 且（每日重复 或 指定日期=今天）。 */
  @Query(
      "SELECT t FROM RewardTask t WHERE t.enabled = true AND "
          + "(t.recurring = true OR t.scheduledDate = :today) "
          + "ORDER BY t.id ASC")
  List<RewardTask> findTodayTasks(@Param("today") LocalDate today);
}
