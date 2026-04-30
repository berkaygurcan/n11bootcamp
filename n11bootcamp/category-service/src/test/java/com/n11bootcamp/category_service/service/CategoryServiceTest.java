package com.n11bootcamp.category_service.service;

import com.n11bootcamp.category_service.entity.Category;
import com.n11bootcamp.category_service.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Test
    void createShouldSaveCategory() {
        Category category = category("phone", "Phone");
        CategoryService service = new CategoryService(repository);

        when(repository.save(category)).thenReturn(category);

        Category result = service.create(category);

        assertThat(result).isSameAs(category);
        verify(repository).save(category);
    }

    @Test
    void getAllShouldReturnCategories() {
        Category phone = category("phone", "Phone");
        Category computer = category("computer", "Computer");
        CategoryService service = new CategoryService(repository);

        when(repository.findAll()).thenReturn(List.of(phone, computer));

        List<Category> result = service.getAll();

        assertThat(result).containsExactly(phone, computer);
    }

    @Test
    void getByKeyShouldReturnCategoryWhenFound() {
        Category phone = category("phone", "Phone");
        CategoryService service = new CategoryService(repository);

        when(repository.findByKey("phone")).thenReturn(Optional.of(phone));

        Category result = service.getByKey("phone");

        assertThat(result).isSameAs(phone);
        assertThat(result.getName()).isEqualTo("Phone");
    }

    @Test
    void getByKeyShouldThrowWhenCategoryNotFound() {
        CategoryService service = new CategoryService(repository);

        when(repository.findByKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByKey("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    private Category category(String key, String name) {
        Category category = new Category();
        category.setKey(key);
        category.setName(name);
        return category;
    }
}
