package com.n11bootcamp.stock_service.service;

import com.n11bootcamp.stock_service.dto.DecreaseStockRequest;
import com.n11bootcamp.stock_service.entity.ProductStock;
import com.n11bootcamp.stock_service.repository.ProductStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void decreaseStock(DecreaseStockRequest request) {

        ProductStock stock = repository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (stock.getAvailableQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock");
        }

        stock.setAvailableQuantity(
                stock.getAvailableQuantity() - request.getQuantity()
        );

        repository.save(stock);
    }

    @Transactional
    public void reserveStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (stock.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);

        repository.save(stock);
    }

    @Transactional
    public void releaseStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);

        repository.save(stock);
    }

    @Transactional
    public void commitStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);

        repository.save(stock);
    }
}