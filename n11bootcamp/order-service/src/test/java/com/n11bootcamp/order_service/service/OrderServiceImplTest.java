package com.n11bootcamp.order_service.service;

import com.n11bootcamp.order_service.dto.CreateOrderRequest;
import com.n11bootcamp.order_service.dto.OrderResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.event.OrderCreatedEvent;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void createOrderShouldSaveOrderAndPublishOrderCreatedEvent() {
        OrderServiceImpl service = new OrderServiceImpl(orderRepository, rabbitTemplate);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 55L);
            return order;
        });

        OrderResponse response = service.createOrder(orderRequest());

        assertThat(response.getOrderId()).isEqualTo(55L);
        assertThat(response.getUsername()).isEqualTo("demo");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(response.getTotalPrice()).isEqualTo(100000.0);
        assertThat(response.getItems()).hasSize(1);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("order.created"), captor.capture());

        OrderCreatedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(55L);
        assertThat(event.getUsername()).isEqualTo("demo");
        assertThat(event.getTotalPrice()).isEqualTo(100000.0);
        assertThat(event.getItems()).hasSize(1);
    }

    private CreateOrderRequest orderRequest() {
        CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
        item.setProductId(1L);
        item.setProductName("iPhone 15");
        item.setPrice(50000.0);
        item.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUsername("demo");
        request.setItems(List.of(item));
        return request;
    }
}
