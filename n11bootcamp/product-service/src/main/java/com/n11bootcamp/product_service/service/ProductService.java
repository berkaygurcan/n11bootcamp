package com.n11bootcamp.product_service.service;

import com.n11bootcamp.product_service.dto.CategoryResponse;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    // 🔥 constructor injection
    public ProductService(ProductRepository productRepository,
                          RestTemplate restTemplate) {
        this.productRepository = productRepository;
        this.restTemplate = restTemplate;
    }

    // ------------------ CATEGORY VALIDATION ------------------

    public CategoryResponse validateCategory(String categoryKey) {
        if (categoryKey == null || categoryKey.isBlank()) {
            throw new RuntimeException("Category key is required!");
        }

        String url = "http://localhost:8763/api/categories/" + categoryKey;

        try {
            CategoryResponse category = restTemplate.getForObject(url, CategoryResponse.class);
            if (category == null) {
                throw new RuntimeException("Category not found!");
            }
            return category;
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Category not found!");
        } catch (Exception e) {
            throw new RuntimeException("Category service error!");
        }
    }

    public ResponseEntity<Product> getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in DB"));
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<List<Product>> allProducts() {
        List<Product> productList = productRepository.findAll();
        return ResponseEntity.ok(productList);
    }

    // 🔥 CREATE (VALIDATION EKLENDİ)
    public ResponseEntity<Product> createProduct(Product product) {

        CategoryResponse category = validateCategory(product.getCategoryKey());
        product.setCategory(category.getName());

        return ResponseEntity.ok().body(productRepository.save(product));
    }

    public ResponseEntity<Product> updateProduct(Long productId, Product updatedProduct) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in DB"));

        product.setImg(updatedProduct.getImg());
        product.setPrice(updatedProduct.getPrice());
        product.setLabels(updatedProduct.getLabels());
        product.setBrand(updatedProduct.getBrand());
        product.setColor(updatedProduct.getColor());
        CategoryResponse category = validateCategory(updatedProduct.getCategoryKey());
        product.setCategoryKey(updatedProduct.getCategoryKey());
        product.setCategory(category.getName());

        productRepository.save(product);
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<String> deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok("Product deleted successfully");
        } else {
            throw new RuntimeException("Product not found in DB");
        }
    }

    public ResponseEntity<String> deleteAllProducts() {
        productRepository.deleteAll();
        return ResponseEntity.ok("All products deleted successfully");
    }


    public Page<Product> getPaged(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return productRepository.findAll(
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        );
    }

    @Transactional
    public void handleCategoryKeyChange(String oldKey, String newKey) {
        try {
            if (oldKey == null || newKey == null || oldKey.equalsIgnoreCase(newKey)) {
                log.debug("Category key update ignored (null or same): {} -> {}", oldKey, newKey);
                return;
            }
            int updatedCount = productRepository.updateCategoryKeyForProducts(oldKey, newKey);
            log.info("Updated {} products: categoryKey '{}' -> '{}'", updatedCount, oldKey, newKey);
        } catch (Exception ex) {
            log.error("Failed to update product categoryKeys for '{}' -> '{}': {}", oldKey, newKey, ex.getMessage(), ex);
        }
    }
}
