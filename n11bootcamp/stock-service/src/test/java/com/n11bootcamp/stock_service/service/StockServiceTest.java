package com.n11bootcamp.stock_service.service;

import com.n11bootcamp.stock_service.dto.DecreaseStockRequest;
import com.n11bootcamp.stock_service.entity.ProductStock;
import com.n11bootcamp.stock_service.repository.ProductStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductStockRepository repository;

    @Test
    void reserveStockShouldMoveAvailableQuantityToReservedQuantity() {
        ProductStock stock = new ProductStock(1L, "iPhone 15", 10);
        StockService service = new StockService(repository);

        when(repository.findById(1L)).thenReturn(Optional.of(stock));

        service.reserveStock(1L, 3);

        assertThat(stock.getAvailableQuantity()).isEqualTo(7);
        assertThat(stock.getReservedQuantity()).isEqualTo(3);
        verify(repository).save(stock);
    }

    @Test
    void reserveStockShouldThrowWhenAvailableQuantityIsNotEnough() {
        ProductStock stock = new ProductStock(1L, "iPhone 15", 1);
        StockService service = new StockService(repository);

        when(repository.findById(1L)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> service.reserveStock(1L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not enough stock");
    }

    @Test
    void releaseStockShouldMoveReservedQuantityBackToAvailableQuantity() {
        ProductStock stock = new ProductStock(1L, "iPhone 15", 7);
        stock.setReservedQuantity(3);
        StockService service = new StockService(repository);

        when(repository.findById(1L)).thenReturn(Optional.of(stock));

        service.releaseStock(1L, 2);

        assertThat(stock.getAvailableQuantity()).isEqualTo(9);
        assertThat(stock.getReservedQuantity()).isEqualTo(1);
        verify(repository).save(stock);
    }

    @Test
    void commitStockShouldDecreaseReservedQuantity() {
        ProductStock stock = new ProductStock(1L, "iPhone 15", 7);
        stock.setReservedQuantity(3);
        StockService service = new StockService(repository);

        when(repository.findById(1L)).thenReturn(Optional.of(stock));

        service.commitStock(1L, 3);

        assertThat(stock.getAvailableQuantity()).isEqualTo(7);
        assertThat(stock.getReservedQuantity()).isZero();
        verify(repository).save(stock);
    }

    @Test
    void decreaseStockShouldDecreaseAvailableQuantity() {
        ProductStock stock = new ProductStock(1L, "iPhone 15", 10);
        DecreaseStockRequest request = new DecreaseStockRequest();
        request.setProductId(1L);
        request.setQuantity(4);
        StockService service = new StockService(repository);

        when(repository.findById(1L)).thenReturn(Optional.of(stock));

        service.decreaseStock(request);

        assertThat(stock.getAvailableQuantity()).isEqualTo(6);
        verify(repository).save(stock);
    }
}
