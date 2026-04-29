package com.n11bootcamp.order_service.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentResultListener {

    @RabbitListener(queues = "order.queue")
    public void listen(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String routingKey) {

        System.out.println("EVENT GELDİ → " + routingKey);
        System.out.println("PAYLOAD → " + payload);

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String username = payload.get("username").toString();

        if ("payment.success".equals(routingKey)) {
            System.out.println("✅ PAYMENT SUCCESS → " + orderId);

            // TODO: order status CONFIRMED yap
        }

        else if ("payment.failed".equals(routingKey)) {
            System.out.println("❌ PAYMENT FAILED → " + orderId);

            // TODO: order status CANCELLED yap
        }

        else {
            System.out.println("⚠️ UNKNOWN EVENT");
        }
    }
}