package com.n11bootcamp.stock_service.controller;

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
}