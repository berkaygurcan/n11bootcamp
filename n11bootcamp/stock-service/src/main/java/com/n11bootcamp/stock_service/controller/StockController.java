package com.n11bootcamp.stock_service.controller;

import com.n11bootcamp.stock_service.dto.DecreaseStockRequest;
import com.n11bootcamp.stock_service.dto.StockRequest;
import com.n11bootcamp.stock_service.entity.ProductStock;
import com.n11bootcamp.stock_service.service.StockService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{productId}")
    public ProductStock getStock(@PathVariable Long productId) {
        return stockService.getStock(productId);
    }

    @PostMapping("/decrease")
    public void decreaseStock(@RequestBody DecreaseStockRequest request) {
        stockService.decreaseStock(request);
    }

    @PostMapping("/reserve")
    public void reserve(@RequestBody StockRequest request) {
        stockService.reserveStock(request.getProductId(), request.getQuantity());
    }

    @PostMapping("/release")
    public void release(@RequestBody StockRequest request) {
        stockService.releaseStock(request.getProductId(), request.getQuantity());
    }

    @PostMapping("/commit")
    public void commit(@RequestBody StockRequest request) {
        stockService.commitStock(request.getProductId(), request.getQuantity());
    }
}
