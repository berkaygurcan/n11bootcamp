package com.n11bootcamp.category_service.controller;

import com.n11bootcamp.category_service.entity.Category;
import com.n11bootcamp.category_service.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    public Category create(@RequestBody Category category) {
        return service.create(category);
    }

    @GetMapping
    public List<Category> getAll() {
        return service.getAll();
    }

    @GetMapping("/{key}")
    public Category getByKey(@PathVariable String key) {
        return service.getByKey(key);
    }
}