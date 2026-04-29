package com.n11bootcamp.category_service.repository;

import com.n11bootcamp.category_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByKey(String key);
}