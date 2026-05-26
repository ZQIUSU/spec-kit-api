package com.creamlogin.app.repository;

import com.creamlogin.app.domain.TaskCompletion;
import com.creamlogin.app.domain.TaskCompletion.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
  List<TaskCompletion> findAllByUserIdOrderBySubmittedAtDesc(Long userId);
  List<TaskCompletion> findAllByStatusOrderBySubmittedAtAsc(Status status);
  List<TaskCompletion> findAllByOrderBySubmittedAtDesc();
}
