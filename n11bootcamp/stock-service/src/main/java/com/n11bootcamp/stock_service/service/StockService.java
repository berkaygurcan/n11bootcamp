package com.n11bootcamp.stock_service.service;

import com.n11bootcamp.stock_service.entity.ProductStock;
import com.n11bootcamp.stock_service.repository.ProductStockRepository;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final ProductStockRepository repository;

    public StockService(ProductStockRepository repository) {
        this.repository = repository;
    }

    public ProductStock getStock(Long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
    }
}