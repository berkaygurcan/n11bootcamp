package com.n11bootcamp.product_service.service;

import com.n11bootcamp.product_service.dto.CategoryResponse;
import com.n11bootcamp.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ProductService {
    CategoryResponse validateCategory(String categoryKey);
    ResponseEntity<Product> getProductById(Long productId);
    ResponseEntity<List<Product>> allProducts();
    ResponseEntity<Product> createProduct(Product product);
    ResponseEntity<Product> updateProduct(Long productId, Product updatedProduct);
    ResponseEntity<String> deleteProduct(Long id);
    ResponseEntity<String> deleteAllProducts();
    Page<Product> getPaged(int page, int size, String sortBy, String direction);
    void handleCategoryKeyChange(String oldKey, String newKey);
}
