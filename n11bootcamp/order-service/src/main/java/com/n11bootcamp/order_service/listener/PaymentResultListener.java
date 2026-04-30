package com.n11bootcamp.order_service.listener;

import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final OrderRepository orderRepository;

    public PaymentResultListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void listen(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String routingKey) {

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String username = payload.get("username").toString();
        log.info("PAYMENT_RESULT_RECEIVED orderId={} username={} event={}", orderId, username, routingKey);

        if ("payment.success".equals(routingKey)) {
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
            });

            log.info("ORDER_COMPLETED orderId={} username={}", orderId, username);
        }

        else if ("payment.failed".equals(routingKey)) {
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            });

            log.warn("ORDER_CANCELLED orderId={} username={} reason=PAYMENT_FAILED", orderId, username);
        }

        else {
            log.warn("UNKNOWN_PAYMENT_RESULT_EVENT orderId={} username={} event={}", orderId, username, routingKey);
        }
    }
}
