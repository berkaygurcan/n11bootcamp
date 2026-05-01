package com.n11bootcamp.stock_service.service;

import com.n11bootcamp.stock_service.dto.DecreaseStockRequest;
import com.n11bootcamp.stock_service.entity.ProductStock;
public interface StockService {
    ProductStock getStock(Long productId);
    void decreaseStock(DecreaseStockRequest request);
    void reserveStock(Long productId, Integer quantity);
    void releaseStock(Long productId, Integer quantity);
    void commitStock(Long productId, Integer quantity);
}
