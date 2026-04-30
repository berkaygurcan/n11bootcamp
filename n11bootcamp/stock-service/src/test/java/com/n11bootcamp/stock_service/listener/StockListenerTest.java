package com.n11bootcamp.stock_service.listener;

import com.n11bootcamp.stock_service.event.StockFailedEvent;
import com.n11bootcamp.stock_service.event.StockReservedEvent;
import com.n11bootcamp.stock_service.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StockService stockService;

    @Test
    void orderCreatedShouldReserveStockAndPublishReservedEvent() {
        StockListener listener = new StockListener(rabbitTemplate, stockService);

        listener.handle(payload(), "order.created");

        verify(stockService).reserveStock(1L, 2);

        ArgumentCaptor<StockReservedEvent> captor = ArgumentCaptor.forClass(StockReservedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("stock.reserved"), captor.capture());

        StockReservedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(55L);
        assertThat(event.getUsername()).isEqualTo("demo");
        assertThat(event.getItems()).hasSize(1);
        assertThat(event.getPaymentCard().get("cardNumber")).isEqualTo("5528790000000008");
    }

    @Test
    void orderCreatedShouldPublishFailedEventWhenStockReserveFails() {
        StockListener listener = new StockListener(rabbitTemplate, stockService);
        doThrow(new RuntimeException("Not enough stock")).when(stockService).reserveStock(1L, 2);

        listener.handle(payload(), "order.created");

        ArgumentCaptor<StockFailedEvent> captor = ArgumentCaptor.forClass(StockFailedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("stock.failed"), captor.capture());

        StockFailedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(55L);
        assertThat(event.getUsername()).isEqualTo("demo");
        assertThat(event.getReason()).isEqualTo("Not enough stock");
    }

    @Test
    void paymentFailedShouldReleaseReservedStock() {
        StockListener listener = new StockListener(rabbitTemplate, stockService);

        listener.handle(payload(), "payment.failed");

        verify(stockService).releaseStock(1L, 2);
    }

    @Test
    void paymentSuccessShouldCommitReservedStock() {
        StockListener listener = new StockListener(rabbitTemplate, stockService);

        listener.handle(payload(), "payment.success");

        verify(stockService).commitStock(1L, 2);
    }

    private Map<String, Object> payload() {
        return Map.of(
                "orderId", 55L,
                "username", "demo",
                "paymentCard", Map.of(
                        "cardHolderName", "John Doe",
                        "cardNumber", "5528790000000008",
                        "expireMonth", "12",
                        "expireYear", "2030",
                        "cvc", "123"
                ),
                "items", List.of(Map.of(
                        "productId", 1L,
                        "productName", "iPhone 15",
                        "price", 50000,
                        "quantity", 2
                ))
        );
    }
}
