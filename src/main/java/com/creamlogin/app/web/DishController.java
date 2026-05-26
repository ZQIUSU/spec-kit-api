package com.creamlogin.app.web;

import com.creamlogin.app.domain.Dish;
import com.creamlogin.app.repository.DishRepository;
import com.creamlogin.app.security.AuthPrincipal;
import com.creamlogin.app.security.CurrentUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

  private final DishRepository repo;

  public DishController(DishRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Dish> list() {
    return repo.findAll();
  }

  @PostMapping
  public Dish create(@RequestBody DishForm form) {
    AuthPrincipal me = CurrentUser.require();
    Dish d = new Dish();
    d.setName(form.name.trim());
    d.setNote(form.note);
    d.setWeight(form.weight == null ? 1 : form.weight);
    d.setEnabled(form.enabled == null ? true : form.enabled);
    d.setCreatedBy(me.userId());
    d.setCreatedByName(me.username());
    return repo.save(d);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Dish> update(@PathVariable Long id, @RequestBody DishForm form) {
    AuthPrincipal me = CurrentUser.require();
    return repo
        .findById(id)
        .map(
            d -> {
              if (!"ADMIN".equals(me.role()) && !me.userId().equals(d.getCreatedBy())) {
                return ResponseEntity.status(403).<Dish>build();
              }
              if (form.name != null) d.setName(form.name.trim());
              if (form.note != null) d.setNote(form.note);
              if (form.weight != null) d.setWeight(form.weight);
              if (form.enabled != null) d.setEnabled(form.enabled);
              return ResponseEntity.ok(repo.save(d));
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    AuthPrincipal me = CurrentUser.require();
    return repo
        .findById(id)
        .map(
            d -> {
              if (!"ADMIN".equals(me.role()) && !me.userId().equals(d.getCreatedBy())) {
                return ResponseEntity.status(403).<Void>build();
              }
              repo.delete(d);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/draw")
  public ResponseEntity<?> draw() {
    List<Dish> pool = repo.findAllByEnabledTrue();
    if (pool.isEmpty()) {
      return ResponseEntity.status(400).body(Map.of("error", "no_dish", "message", "还没有可选的菜，先去添加吧"));
    }
    long total = pool.stream().mapToLong(d -> Math.max(1, d.getWeight())).sum();
    long r = ThreadLocalRandom.current().nextLong(total);
    long acc = 0;
    for (Dish d : pool) {
      acc += Math.max(1, d.getWeight());
      if (r < acc) {
        return ResponseEntity.ok(d);
      }
    }
    return ResponseEntity.ok(pool.get(pool.size() - 1));
  }

  public static class DishForm {
    @NotBlank
    @Size(max = 128)
    public String name;
    public String note;
    public Integer weight;
    public Boolean enabled;
  }
}
