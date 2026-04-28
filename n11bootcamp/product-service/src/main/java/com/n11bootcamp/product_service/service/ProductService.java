package com.n11bootcamp.product_service.service;


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
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // ✅ constructor injection (çalışanları bozmaz)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ------------------ MEVCUT METODLAR (korundu) ------------------

    public ResponseEntity<Product> getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in DB"));
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<List<Product>> allProducts() {
        List<Product> productList = productRepository.findAll();
        return ResponseEntity.ok(productList);
    }

    public ResponseEntity<Product> createProduct(Product product) {
        return ResponseEntity.ok().body(productRepository.save(product));
    }

    public ResponseEntity<Product> updateProduct(Long productId, Product updatedProduct) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in DB"));

        // ⚠️ Yeni şemada title/category/description product'ta yok.
        // Sadece mevcut alanları güncelliyoruz:
        product.setImg(updatedProduct.getImg());
        product.setPrice(updatedProduct.getPrice());
        product.setLabels(updatedProduct.getLabels());
        product.setBrand(updatedProduct.getBrand());
        product.setColor(updatedProduct.getColor());
        product.setCategoryKey(updatedProduct.getCategoryKey());

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

    // --- resim upload (korundu) ---
    public Product uploadImage(Long id, MultipartFile file) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filepath = Paths.get("./images/products/", filename);
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        product.setImg(filename);
        return productRepository.save(product);
    }

    public Page<Product> getPaged(int page, int size) {
        return productRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
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
