package com.creamlogin.app.web;

import com.creamlogin.app.domain.RedemptionRecord;
import com.creamlogin.app.domain.RedemptionRecord.Status;
import com.creamlogin.app.domain.Reward;
import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.RedemptionRecordRepository;
import com.creamlogin.app.repository.RewardRepository;
import com.creamlogin.app.repository.UserRepository;
import com.creamlogin.app.security.AuthPrincipal;
import com.creamlogin.app.security.CurrentUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

  private final RewardRepository rewardRepo;
  private final RedemptionRecordRepository recordRepo;
  private final UserRepository userRepo;

  public RewardController(
      RewardRepository rewardRepo,
      RedemptionRecordRepository recordRepo,
      UserRepository userRepo) {
    this.rewardRepo = rewardRepo;
    this.recordRepo = recordRepo;
    this.userRepo = userRepo;
  }

  /** 用户视图：仅返回启用中的奖品。 */
  @GetMapping
  public List<Reward> list() {
    return rewardRepo.findAllByEnabledTrueOrderByIdAsc();
  }

  @GetMapping("/my-records")
  public List<RedemptionRecord> myRecords() {
    AuthPrincipal me = CurrentUser.require();
    return recordRepo.findAllByUserIdOrderByCreatedAtDesc(me.userId());
  }

  /** 用户兑换：原子扣分 + 减库存 + 写记录。 */
  @PostMapping("/{id}/redeem")
  @Transactional
  public ResponseEntity<?> redeem(@PathVariable Long id) {
    AuthPrincipal me = CurrentUser.require();
    Reward r = rewardRepo.findById(id).orElse(null);
    if (r == null || !r.isEnabled()) {
      return ResponseEntity.status(404).body(Map.of("error", "reward_unavailable"));
    }
    if (r.getStock() != null && r.getStock() <= 0) {
      return ResponseEntity.status(409).body(Map.of("error", "out_of_stock", "code", "OUT_OF_STOCK"));
    }
    User u = userRepo.findById(me.userId()).orElse(null);
    if (u == null) return ResponseEntity.status(401).build();
    if (u.getPoints() < r.getCost()) {
      return ResponseEntity.status(409)
          .body(Map.of("error", "insufficient_points", "code", "INSUFFICIENT_POINTS"));
    }

    u.addPoints(-r.getCost());
    userRepo.save(u);

    if (r.getStock() != null) {
      r.setStock(r.getStock() - 1);
      rewardRepo.save(r);
    }

    RedemptionRecord rec = new RedemptionRecord();
    rec.setUserId(me.userId());
    rec.setUserName(me.username());
    rec.setRewardId(r.getId());
    rec.setRewardName(r.getName());
    rec.setCost(r.getCost());
    rec.setStatus(Status.PENDING);
    recordRepo.save(rec);

    return ResponseEntity.ok(Map.of("ok", true, "record", rec, "points", u.getPoints()));
  }

  // === Admin endpoints ===

  @GetMapping("/admin")
  public List<Reward> adminList() {
    requireAdmin();
    return rewardRepo.findAll();
  }

  @PostMapping("/admin")
  public Reward create(@RequestBody RewardForm form) {
    requireAdmin();
    Reward r = new Reward();
    applyForm(r, form);
    return rewardRepo.save(r);
  }

  @PutMapping("/admin/{id}")
  public ResponseEntity<Reward> update(@PathVariable Long id, @RequestBody RewardForm form) {
    requireAdmin();
    return rewardRepo
        .findById(id)
        .map(
            r -> {
              applyForm(r, form);
              return ResponseEntity.ok(rewardRepo.save(r));
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private void applyForm(Reward r, RewardForm form) {
    if (form.name != null) r.setName(form.name.trim());
    if (form.description != null) r.setDescription(form.description);
    if (form.cost != null) r.setCost(Math.max(0, form.cost));
    if (form._stockPresent) r.setStock(form.stock); // null 表示不限量
    if (form.imageUrl != null) r.setImageUrl(form.imageUrl);
    if (form.enabled != null) r.setEnabled(form.enabled);
  }

  @DeleteMapping("/admin/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    requireAdmin();
    if (!rewardRepo.existsById(id)) return ResponseEntity.notFound().build();
    rewardRepo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/admin/records")
  public List<RedemptionRecord> adminRecords() {
    requireAdmin();
    return recordRepo.findAllByOrderByCreatedAtDesc();
  }

  @PostMapping("/admin/records/{id}/fulfill")
  @Transactional
  public ResponseEntity<?> fulfill(@PathVariable Long id) {
    AuthPrincipal me = requireAdmin();
    RedemptionRecord rec = recordRepo.findById(id).orElse(null);
    if (rec == null) return ResponseEntity.notFound().build();
    if (rec.getStatus() != Status.PENDING) {
      return ResponseEntity.status(409).body(Map.of("error", "already_processed"));
    }
    rec.setStatus(Status.FULFILLED);
    rec.setFulfilledAt(Instant.now());
    rec.setFulfilledBy(me.userId());
    return ResponseEntity.ok(recordRepo.save(rec));
  }

  /** 取消兑换：退还积分。 */
  @PostMapping("/admin/records/{id}/cancel")
  @Transactional
  public ResponseEntity<?> cancel(@PathVariable Long id) {
    AuthPrincipal me = requireAdmin();
    RedemptionRecord rec = recordRepo.findById(id).orElse(null);
    if (rec == null) return ResponseEntity.notFound().build();
    if (rec.getStatus() != Status.PENDING) {
      return ResponseEntity.status(409).body(Map.of("error", "already_processed"));
    }
    rec.setStatus(Status.CANCELED);
    rec.setFulfilledAt(Instant.now());
    rec.setFulfilledBy(me.userId());
    recordRepo.save(rec);
    // 退积分
    userRepo
        .findById(rec.getUserId())
        .ifPresent(
            u -> {
              u.addPoints(rec.getCost());
              userRepo.save(u);
            });
    // 库存也尝试回补
    rewardRepo
        .findById(rec.getRewardId())
        .ifPresent(
            r -> {
              if (r.getStock() != null) {
                r.setStock(r.getStock() + 1);
                rewardRepo.save(r);
              }
            });
    return ResponseEntity.ok(rec);
  }

  private static AuthPrincipal requireAdmin() {
    AuthPrincipal me = CurrentUser.require();
    if (!"ADMIN".equals(me.role())) {
      throw new SecurityException("admin only");
    }
    return me;
  }

  public static class RewardForm {
    public String name;
    public String description;
    public Integer cost;
    public Integer stock;
    public transient boolean _stockPresent;
    public String imageUrl;
    public Boolean enabled;

    public void setStock(Integer v) {
      this.stock = v;
      this._stockPresent = true;
    }
  }
}
