package com.creamlogin.app.web;

import com.creamlogin.app.domain.RewardTask;
import com.creamlogin.app.domain.TaskCompletion;
import com.creamlogin.app.domain.TaskCompletion.Status;
import com.creamlogin.app.domain.User;
import com.creamlogin.app.repository.RewardTaskRepository;
import com.creamlogin.app.repository.TaskCompletionRepository;
import com.creamlogin.app.repository.UserRepository;
import com.creamlogin.app.security.AuthPrincipal;
import com.creamlogin.app.security.CurrentUser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequestMapping("/api/tasks")
public class RewardTaskController {

  /** "今日"基于此时区切日，避免依赖客户端本地时间。 */
  private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

  private final RewardTaskRepository taskRepo;
  private final TaskCompletionRepository completionRepo;
  private final UserRepository userRepo;

  public RewardTaskController(
      RewardTaskRepository taskRepo,
      TaskCompletionRepository completionRepo,
      UserRepository userRepo) {
    this.taskRepo = taskRepo;
    this.completionRepo = completionRepo;
    this.userRepo = userRepo;
  }

  @GetMapping
  public List<RewardTask> list() {
    return taskRepo.findAll();
  }

  @GetMapping("/my-completions")
  public List<TaskCompletion> myCompletions() {
    AuthPrincipal me = CurrentUser.require();
    return completionRepo.findAllByUserIdOrderBySubmittedAtDesc(me.userId());
  }

  /** 用户端首页 / 每日任务页拉取今日任务及当前用户今天的提交记录。 */
  @GetMapping("/today")
  public Map<String, Object> today() {
    AuthPrincipal me = CurrentUser.require();
    LocalDate todayDate = LocalDate.now(ZONE);
    RewardTask task = pickTodayTask(me.userId(), todayDate);

    TaskCompletion todayCompletion = null;
    if (task != null) {
      Instant from = todayDate.atStartOfDay(ZONE).toInstant();
      Instant to = todayDate.plusDays(1).atStartOfDay(ZONE).toInstant();
      todayCompletion =
          completionRepo
              .findFirstByUserIdAndTaskIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                  me.userId(), task.getId(), from, to)
              .orElse(null);
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("task", task);
    body.put("todayCompletion", todayCompletion);
    body.put("serverNow", ZonedDateTime.now(ZONE).toOffsetDateTime().toString());
    return body;
  }

  private RewardTask pickTodayTask(long userId, LocalDate today) {
    List<RewardTask> scheduled = taskRepo.findAllByEnabledTrueAndScheduledDateOrderByIdAsc(today);
    if (!scheduled.isEmpty()) {
      return scheduled.get(0);
    }
    List<RewardTask> pool = taskRepo.findAllByEnabledTrueAndScheduledDateIsNullOrderByIdAsc();
    if (pool.isEmpty()) {
      return null;
    }
    // 稳定 hash：同一用户当天看到同一个任务
    long seed = (userId * 31L) ^ today.toEpochDay();
    int idx = Math.floorMod(Long.hashCode(seed), pool.size());
    return pool.get(idx);
  }

  @PostMapping("/{id}/submit")
  public ResponseEntity<?> submit(
      @PathVariable Long id, @RequestBody(required = false) SubmitForm form) {
    AuthPrincipal me = CurrentUser.require();
    var task = taskRepo.findById(id).orElse(null);
    if (task == null || !task.isEnabled()) {
      return ResponseEntity.status(404).body(Map.of("error", "task_unavailable"));
    }

    LocalDate todayDate = LocalDate.now(ZONE);
    RewardTask todayTask = pickTodayTask(me.userId(), todayDate);
    if (todayTask == null || !todayTask.getId().equals(task.getId())) {
      return ResponseEntity.status(400)
          .body(Map.of("error", "not_today_task", "code", "NOT_TODAY_TASK"));
    }

    Instant from = todayDate.atStartOfDay(ZONE).toInstant();
    Instant to = todayDate.plusDays(1).atStartOfDay(ZONE).toInstant();
    Optional<TaskCompletion> existing =
        completionRepo.findFirstByUserIdAndTaskIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
            me.userId(), task.getId(), from, to);
    if (existing.isPresent()) {
      return ResponseEntity.status(409)
          .body(
              Map.of(
                  "error",
                  "already_submitted_today",
                  "code",
                  "ALREADY_SUBMITTED_TODAY",
                  "status",
                  existing.get().getStatus().name()));
    }

    TaskCompletion c = new TaskCompletion();
    c.setTaskId(task.getId());
    c.setUserId(me.userId());
    c.setUserName(me.username());
    c.setNote(form == null ? null : form.note);
    c.setStatus(Status.PENDING);
    return ResponseEntity.ok(completionRepo.save(c));
  }

  // === Admin endpoints ===

  @PostMapping("/admin")
  public RewardTask create(@RequestBody TaskForm form) {
    requireAdmin();
    RewardTask t = new RewardTask();
    applyForm(t, form);
    return taskRepo.save(t);
  }

  @PutMapping("/admin/{id}")
  public ResponseEntity<RewardTask> update(@PathVariable Long id, @RequestBody TaskForm form) {
    requireAdmin();
    return taskRepo
        .findById(id)
        .map(
            t -> {
              applyForm(t, form);
              return ResponseEntity.ok(taskRepo.save(t));
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private void applyForm(RewardTask t, TaskForm form) {
    if (form.title != null) t.setTitle(form.title.trim());
    if (form.description != null) t.setDescription(form.description);
    if (form.points != null) t.setPoints(Math.max(0, form.points));
    if (form.enabled != null) t.setEnabled(form.enabled);
    // scheduledDate 显式区分"未提供"和"清空"：用一个 sentinel field 比较麻烦，
    // 这里约定：只要 payload 里出现该字段，就以传入值（含 null）覆盖。
    if (form._scheduledDatePresent) {
      if (form.scheduledDate == null || form.scheduledDate.isBlank()) {
        t.setScheduledDate(null);
      } else {
        t.setScheduledDate(LocalDate.parse(form.scheduledDate));
      }
    }
  }

  @DeleteMapping("/admin/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    requireAdmin();
    if (!taskRepo.existsById(id)) return ResponseEntity.notFound().build();
    taskRepo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/admin/completions")
  public List<Map<String, Object>> allCompletions() {
    requireAdmin();
    List<TaskCompletion> rows = completionRepo.findAllByOrderBySubmittedAtDesc();
    Map<Long, RewardTask> taskMap = new HashMap<>();
    taskRepo.findAll().forEach(t -> taskMap.put(t.getId(), t));
    return rows.stream()
        .map(
            c -> {
              Map<String, Object> m = new HashMap<>();
              m.put("completion", c);
              m.put("task", taskMap.get(c.getTaskId()));
              return m;
            })
        .toList();
  }

  @PostMapping("/admin/completions/{id}/approve")
  @Transactional
  public ResponseEntity<?> approve(@PathVariable Long id) {
    AuthPrincipal me = requireAdmin();
    TaskCompletion c = completionRepo.findById(id).orElse(null);
    if (c == null) return ResponseEntity.notFound().build();
    if (c.getStatus() != Status.PENDING) {
      return ResponseEntity.status(409).body(Map.of("error", "already_reviewed"));
    }
    RewardTask task = taskRepo.findById(c.getTaskId()).orElse(null);
    int award = task == null ? 0 : task.getPoints();
    c.setStatus(Status.APPROVED);
    c.setPointsAwarded(award);
    c.setReviewedAt(Instant.now());
    c.setReviewedBy(me.userId());
    completionRepo.save(c);
    if (award > 0) {
      userRepo
          .findById(c.getUserId())
          .ifPresent(
              u -> {
                u.addPoints(award);
                userRepo.save(u);
              });
    }
    return ResponseEntity.ok(c);
  }

  @PostMapping("/admin/completions/{id}/reject")
  @Transactional
  public ResponseEntity<?> reject(@PathVariable Long id) {
    AuthPrincipal me = requireAdmin();
    TaskCompletion c = completionRepo.findById(id).orElse(null);
    if (c == null) return ResponseEntity.notFound().build();
    if (c.getStatus() != Status.PENDING) {
      return ResponseEntity.status(409).body(Map.of("error", "already_reviewed"));
    }
    c.setStatus(Status.REJECTED);
    c.setReviewedAt(Instant.now());
    c.setReviewedBy(me.userId());
    return ResponseEntity.ok(completionRepo.save(c));
  }

  @PostMapping("/admin/users/{userId}/award")
  @Transactional
  public ResponseEntity<?> award(@PathVariable Long userId, @RequestBody AwardForm form) {
    requireAdmin();
    User u = userRepo.findById(userId).orElse(null);
    if (u == null) return ResponseEntity.notFound().build();
    int delta = form == null || form.points == null ? 0 : form.points;
    u.addPoints(delta);
    userRepo.save(u);
    return ResponseEntity.ok(Map.of("ok", true, "points", u.getPoints()));
  }

  private static AuthPrincipal requireAdmin() {
    AuthPrincipal me = CurrentUser.require();
    if (!"ADMIN".equals(me.role())) {
      throw new SecurityException("admin only");
    }
    return me;
  }

  public static class TaskForm {
    public String title;
    public String description;
    public Integer points;
    public Boolean enabled;
    /** YYYY-MM-DD；null 或缺省表示"随机轮转"。 */
    public String scheduledDate;
    /** Jackson 反序列化后无法区分"未传"和"传 null"；通过 setter 标记。 */
    public transient boolean _scheduledDatePresent;

    public void setScheduledDate(String v) {
      this.scheduledDate = v;
      this._scheduledDatePresent = true;
    }
  }

  public static class SubmitForm {
    public String note;
  }

  public static class AwardForm {
    public Integer points;
  }
}
