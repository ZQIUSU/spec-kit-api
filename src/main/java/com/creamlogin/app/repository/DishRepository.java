package com.creamlogin.app.repository;

import com.creamlogin.app.domain.Dish;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
  List<Dish> findAllByEnabledTrue();
}
