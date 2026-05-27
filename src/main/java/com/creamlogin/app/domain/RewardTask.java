package com.creamlogin.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reward_tasks")
public class RewardTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String title;

  @Column(length = 1024)
  private String description;

  @Column(nullable = false)
  private int points = 1;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  /** 指定投放日；null 表示该任务不绑定具体日期。 */
  @Column(name = "scheduled_date")
  private LocalDate scheduledDate;

  /** true = 每日重复出现；false = 仅在 scheduledDate 当天出现。 */
  @Column(name = "recurring", nullable = false)
  private boolean recurring = false;

  public Long getId() { return id; }
  public LocalDate getScheduledDate() { return scheduledDate; }
  public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
  public boolean isRecurring() { return recurring; }
  public void setRecurring(boolean recurring) { this.recurring = recurring; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public int getPoints() { return points; }
  public void setPoints(int points) { this.points = points; }
  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
