package com.creamlogin.app.repository;

import com.creamlogin.app.domain.RedemptionRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedemptionRecordRepository extends JpaRepository<RedemptionRecord, Long> {
  List<RedemptionRecord> findAllByUserIdOrderByCreatedAtDesc(Long userId);
  List<RedemptionRecord> findAllByOrderByCreatedAtDesc();
}
