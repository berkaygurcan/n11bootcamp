package com.n11bootcamp.order_service.listener;

import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentResultListener {

    private final OrderRepository orderRepository;

    public PaymentResultListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void listen(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String routingKey) {

        System.out.println("EVENT GELDİ → " + routingKey);
        System.out.println("PAYLOAD → " + payload);

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String username = payload.get("username").toString();

        if ("payment.success".equals(routingKey)) {
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
            });

            System.out.println("✅ PAYMENT SUCCESS → " + orderId);
        }

        else if ("payment.failed".equals(routingKey)) {
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            });

            System.out.println("❌ PAYMENT FAILED → " + orderId);
        }

        else {
            System.out.println("⚠️ UNKNOWN EVENT");
        }
    }
}
