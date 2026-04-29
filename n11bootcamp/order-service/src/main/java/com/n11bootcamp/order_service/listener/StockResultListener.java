package com.n11bootcamp.order_service.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StockResultListener {

    private final RabbitTemplate rabbitTemplate;

    public StockResultListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
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

            System.out.println("STOCK FAILED → ORDER CANCELLED → " + orderId);
        }
    }
}