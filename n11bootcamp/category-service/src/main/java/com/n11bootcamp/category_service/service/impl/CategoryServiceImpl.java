package com.n11bootcamp.category_service.service.impl;

import com.n11bootcamp.category_service.entity.Category;
import com.n11bootcamp.category_service.exception.ResourceNotFoundException;
import com.n11bootcamp.category_service.repository.CategoryRepository;
import com.n11bootcamp.category_service.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category create(Category category) {
        return repository.save(category);
    }

    @Override
    public List<Category> getAll() {
        return repository.findAll();
    }

    @Override
    public Category getByKey(String key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
