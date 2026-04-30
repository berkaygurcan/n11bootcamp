package com.n11bootcamp.order_service.listener;

import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockResultListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void stockReservedShouldStartPaymentProcess() {
        StockResultListener listener = new StockResultListener(rabbitTemplate, orderRepository);
        Map<String, Object> payload = Map.of("orderId", 55L);

        listener.handle(payload, "stock.reserved");

        verify(rabbitTemplate).convertAndSend("order.exchange", "payment.process", payload);
    }

    @Test
    void stockFailedShouldCancelOrder() {
        Order order = order(55L);
        when(orderRepository.findById(55L)).thenReturn(Optional.of(order));
        StockResultListener listener = new StockResultListener(rabbitTemplate, orderRepository);

        listener.handle(Map.of("orderId", 55L), "stock.failed");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getFailureReason()).isEqualTo("STOCK_FAILED");
        verify(orderRepository).save(order);
    }

    @Test
    void paymentSuccessShouldCompleteOrder() {
        Order order = order(55L);
        order.setFailureReason("PAYMENT_FAILED");
        when(orderRepository.findById(55L)).thenReturn(Optional.of(order));
        StockResultListener listener = new StockResultListener(rabbitTemplate, orderRepository);

        listener.handle(Map.of("orderId", 55L), "payment.success");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getFailureReason()).isNull();
        verify(orderRepository).save(order);
    }

    @Test
    void paymentFailedShouldCancelOrder() {
        Order order = order(55L);
        when(orderRepository.findById(55L)).thenReturn(Optional.of(order));
        StockResultListener listener = new StockResultListener(rabbitTemplate, orderRepository);

        listener.handle(Map.of("orderId", 55L), "payment.failed");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getFailureReason()).isEqualTo("PAYMENT_FAILED");
        verify(orderRepository).save(order);
    }

    private Order order(Long id) {
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", id);
        order.setStatus(OrderStatus.CREATED);
        return order;
    }
}
