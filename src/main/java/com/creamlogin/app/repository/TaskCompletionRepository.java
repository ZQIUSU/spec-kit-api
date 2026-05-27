package com.creamlogin.app.repository;

import com.creamlogin.app.domain.TaskCompletion;
import com.creamlogin.app.domain.TaskCompletion.Status;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
  List<TaskCompletion> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
  List<TaskCompletion> findAllByStatusOrderBySubmittedAtAsc(Status status);
  List<TaskCompletion> findAllByOrderBySubmittedAtDesc();

  Optional<TaskCompletion>
      findFirstByUserIdAndTaskIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
          Long userId, Long taskId, Instant from, Instant to);

  List<TaskCompletion> findAllByUserIdAndSubmittedAtBetween(Long userId, Instant from, Instant to);
}
