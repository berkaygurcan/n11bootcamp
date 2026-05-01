package com.n11bootcamp.category_service.service;

import com.n11bootcamp.category_service.entity.Category;

import java.util.List;

public interface CategoryService {
    Category create(Category category);
    List<Category> getAll();
    Category getByKey(String key);
}
