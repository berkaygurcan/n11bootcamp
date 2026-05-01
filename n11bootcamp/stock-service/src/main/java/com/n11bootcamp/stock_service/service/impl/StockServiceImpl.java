package com.n11bootcamp.stock_service.service.impl;

import com.n11bootcamp.stock_service.dto.DecreaseStockRequest;
import com.n11bootcamp.stock_service.entity.ProductStock;
import com.n11bootcamp.stock_service.exception.BadRequestException;
import com.n11bootcamp.stock_service.exception.ResourceNotFoundException;
import com.n11bootcamp.stock_service.repository.ProductStockRepository;
import com.n11bootcamp.stock_service.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockService {

    private final ProductStockRepository repository;

    public StockServiceImpl(ProductStockRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductStock getStock(Long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
    }

    @Override
    @Transactional
    public void decreaseStock(DecreaseStockRequest request) {

        ProductStock stock = repository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if (stock.getAvailableQuantity() < request.getQuantity()) {
            throw new BadRequestException("Not enough stock");
        }

        stock.setAvailableQuantity(
                stock.getAvailableQuantity() - request.getQuantity()
        );

        repository.save(stock);
    }

    @Override
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if (stock.getAvailableQuantity() < quantity) {
            throw new BadRequestException("Not enough stock");
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);

        repository.save(stock);
    }

    @Override
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);

        repository.save(stock);
    }

    @Override
    @Transactional
    public void commitStock(Long productId, Integer quantity) {

        ProductStock stock = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);

        repository.save(stock);
    }
}
