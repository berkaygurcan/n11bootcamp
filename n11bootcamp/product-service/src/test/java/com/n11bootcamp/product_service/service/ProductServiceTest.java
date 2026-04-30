package com.n11bootcamp.product_service.service;

import com.n11bootcamp.product_service.dto.CategoryResponse;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void createProductShouldValidateCategoryAndSaveProduct() {
        Product product = product();
        ProductService service = new ProductService(productRepository, restTemplate);

        when(restTemplate.getForObject("http://localhost:8763/api/categories/phone", CategoryResponse.class))
                .thenReturn(category("Phone"));
        when(productRepository.save(product)).thenReturn(product);

        ResponseEntity<Product> response = service.createProduct(product);

        assertThat(response.getBody()).isSameAs(product);
        assertThat(product.getCategory()).isEqualTo("Phone");
        verify(productRepository).save(product);
    }

    @Test
    void validateCategoryShouldThrowWhenCategoryKeyIsBlank() {
        ProductService service = new ProductService(productRepository, restTemplate);

        assertThatThrownBy(() -> service.validateCategory(" "))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category key is required!");
    }

    @Test
    void validateCategoryShouldThrowWhenCategoryNotFound() {
        ProductService service = new ProductService(productRepository, restTemplate);

        when(restTemplate.getForObject("http://localhost:8763/api/categories/missing", CategoryResponse.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        null,
                        null
                ));

        assertThatThrownBy(() -> service.validateCategory("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found!");
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        Product product = product();
        ProductService service = new ProductService(productRepository, restTemplate);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ResponseEntity<Product> response = service.getProductById(1L);

        assertThat(response.getBody()).isSameAs(product);
    }

    @Test
    void updateProductShouldUpdateFieldsAndCategory() {
        Product existing = product();
        Product updated = product();
        updated.setImg("new.png");
        updated.setPrice(2000);
        updated.setLabels("new");
        updated.setBrand("Apple");
        updated.setColor("black");
        updated.setCategoryKey("phone");

        ProductService service = new ProductService(productRepository, restTemplate);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(restTemplate.getForObject("http://localhost:8763/api/categories/phone", CategoryResponse.class))
                .thenReturn(category("Phone"));

        ResponseEntity<Product> response = service.updateProduct(1L, updated);

        Product result = response.getBody();
        assertThat(result.getImg()).isEqualTo("new.png");
        assertThat(result.getPrice()).isEqualTo(2000);
        assertThat(result.getLabels()).isEqualTo("new");
        assertThat(result.getBrand()).isEqualTo("Apple");
        assertThat(result.getColor()).isEqualTo("black");
        assertThat(result.getCategory()).isEqualTo("Phone");
        verify(productRepository).save(existing);
    }

    @Test
    void getPagedShouldReturnRepositoryPage() {
        Product product = product();
        ProductService service = new ProductService(productRepository, restTemplate);

        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<Product> response = service.getPaged(0, 10, "id", "desc");

        assertThat(response.getContent()).containsExactly(product);
    }

    @Test
    void deleteProductShouldDeleteWhenProductExists() {
        ProductService service = new ProductService(productRepository, restTemplate);

        when(productRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<String> response = service.deleteProduct(1L);

        assertThat(response.getBody()).isEqualTo("Product deleted successfully");
        verify(productRepository).deleteById(1L);
    }

    private Product product() {
        Product product = new Product();
        product.setTitle("iPhone 15");
        product.setCategoryKey("phone");
        product.setPrice(1000);
        return product;
    }

    private CategoryResponse category(String name) {
        CategoryResponse category = new CategoryResponse();
        category.setKey("phone");
        category.setName(name);
        return category;
    }
}
