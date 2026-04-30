package com.n11bootcamp.category_service.service;

import com.n11bootcamp.category_service.entity.Category;
import com.n11bootcamp.category_service.exception.ResourceNotFoundException;
import com.n11bootcamp.category_service.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public Category create(Category category) {
        return repository.save(category);
    }

    public List<Category> getAll() {
        return repository.findAll();
    }

    public Category getByKey(String key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
