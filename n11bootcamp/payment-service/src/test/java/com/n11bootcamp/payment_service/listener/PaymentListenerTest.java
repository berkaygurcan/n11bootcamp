package com.n11bootcamp.payment_service.listener;

import com.n11bootcamp.payment_service.event.PaymentFailedEvent;
import com.n11bootcamp.payment_service.event.PaymentSuccessEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void stockReservedShouldPublishPaymentSuccessEvent() {
        PaymentListener listener = new PaymentListener(rabbitTemplate);

        listener.handle(payload(), "stock.reserved");

        ArgumentCaptor<PaymentSuccessEvent> captor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("payment.success"), captor.capture());

        PaymentSuccessEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(55L);
        assertThat(event.getUsername()).isEqualTo("demo");
        assertThat(event.getItems()).hasSize(1);
    }

    @Test
    void otherRoutingKeysShouldBeIgnored() {
        PaymentListener listener = new PaymentListener(rabbitTemplate);

        listener.handle(payload(), "order.created");

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void stockReservedShouldPublishPaymentFailedEventWhenBasketTotalIsOverLimit() {
        PaymentListener listener = new PaymentListener(rabbitTemplate);

        listener.handle(payload(3), "stock.reserved");

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("payment.failed"), captor.capture());

        PaymentFailedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(55L);
        assertThat(event.getUsername()).isEqualTo("demo");
        assertThat(event.getReason()).isEqualTo("Basket total is over 100000 TL");
    }

    private Map<String, Object> payload() {
        return payload(2);
    }

    private Map<String, Object> payload(int quantity) {
        return Map.of(
                "orderId", 55L,
                "username", "demo",
                "items", List.of(Map.of(
                        "productId", 1L,
                        "productName", "iPhone 15",
                        "price", 50000,
                        "quantity", quantity
                ))
        );
    }
}
