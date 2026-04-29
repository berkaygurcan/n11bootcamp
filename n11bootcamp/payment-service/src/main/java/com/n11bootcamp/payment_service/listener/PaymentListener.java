package com.n11bootcamp.payment_service.listener;

import com.n11bootcamp.payment_service.event.PaymentFailedEvent;
import com.n11bootcamp.payment_service.event.PaymentSuccessEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentListener {

    private final RabbitTemplate rabbitTemplate;

    public PaymentListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "payment.queue")
    public void handle(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String key) {

        // ❗ sadece stock.reserved dinle
        if (!"stock.reserved".equals(key)) {
            return;
        }

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String username = payload.get("username").toString();
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        System.out.println("PAYMENT START → " + orderId);

        boolean success = true;

        if (success) {

            PaymentSuccessEvent e = new PaymentSuccessEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setItems(items);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "payment.success",
                    e   // 🔥 EVENT EKLEDİK
            );

            System.out.println("PAYMENT SUCCESS");

        } else {

            PaymentFailedEvent e = new PaymentFailedEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setReason("fail");
            e.setItems(items);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "payment.failed",
                    e
            );

            System.out.println("PAYMENT FAILED");
        }
    }
}
