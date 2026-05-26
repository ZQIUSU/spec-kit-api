package com.creamlogin.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "task_completions")
public class TaskCompletion {

  public enum Status { PENDING, APPROVED, REJECTED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "task_id", nullable = false)
  private Long taskId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_name", length = 64)
  private String userName;

  @Column(length = 1024)
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Status status = Status.PENDING;

  @Column(name = "points_awarded", nullable = false)
  private int pointsAwarded = 0;

  @Column(name = "submitted_at", nullable = false)
  private Instant submittedAt = Instant.now();

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "reviewed_by")
  private Long reviewedBy;

  public Long getId() { return id; }
  public Long getTaskId() { return taskId; }
  public void setTaskId(Long taskId) { this.taskId = taskId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public int getPointsAwarded() { return pointsAwarded; }
  public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }
  public Instant getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
  public Instant getReviewedAt() { return reviewedAt; }
  public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
  public Long getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
}
