package com.n11bootcamp.order_service.listener;

import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StockResultListener {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;

    public StockResultListener(RabbitTemplate rabbitTemplate,
                               OrderRepository orderRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = "order.queue")
    public void handle(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String key) {

        System.out.println("ORDER SERVICE EVENT → " + key);

        Long orderId = Long.valueOf(payload.get("orderId").toString());

        // ✅ SADECE burada payment tetikle
        if ("stock.reserved".equals(key)) {

            System.out.println("STOCK OK → PAYMENT START → " + orderId);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "payment.process",
                    payload
            );
        }

        else if ("stock.failed".equals(key)) {

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                order.setFailureReason("STOCK_FAILED");
                orderRepository.save(order);
            });

            System.out.println("STOCK FAILED → ORDER CANCELLED → " + orderId);
        }

        else if ("payment.success".equals(key)) {

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.COMPLETED);
                order.setFailureReason(null);
                orderRepository.save(order);
            });

            System.out.println("PAYMENT SUCCESS → ORDER COMPLETED → " + orderId);
        }

        else if ("payment.failed".equals(key)) {

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                order.setFailureReason("PAYMENT_FAILED");
                orderRepository.save(order);
            });

            System.out.println("PAYMENT FAILED → ORDER CANCELLED → " + orderId);
        }
    }
}
