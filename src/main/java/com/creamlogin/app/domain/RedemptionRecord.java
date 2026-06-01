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
@Table(name = "redemption_records")
public class RedemptionRecord {

  public enum Status {
    PENDING, // 已兑换，等待管理员发放
    FULFILLED, // 已发放
    CANCELED // 已取消（管理员退还积分）
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_name", length = 64)
  private String userName;

  @Column(name = "reward_id", nullable = false)
  private Long rewardId;

  /** 兑换时的奖品名称快照，奖品被改名/删除后仍保留。 */
  @Column(name = "reward_name", length = 128)
  private String rewardName;

  /** 实际扣除的积分快照。 */
  @Column(nullable = false)
  private int cost;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Status status = Status.PENDING;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "fulfilled_at")
  private Instant fulfilledAt;

  @Column(name = "fulfilled_by")
  private Long fulfilledBy;

  @Column(name = "note", length = 512)
  private String note;

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }
  public Long getRewardId() { return rewardId; }
  public void setRewardId(Long rewardId) { this.rewardId = rewardId; }
  public String getRewardName() { return rewardName; }
  public void setRewardName(String rewardName) { this.rewardName = rewardName; }
  public int getCost() { return cost; }
  public void setCost(int cost) { this.cost = cost; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getFulfilledAt() { return fulfilledAt; }
  public void setFulfilledAt(Instant fulfilledAt) { this.fulfilledAt = fulfilledAt; }
  public Long getFulfilledBy() { return fulfilledBy; }
  public void setFulfilledBy(Long fulfilledBy) { this.fulfilledBy = fulfilledBy; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
}
