package com.n11bootcamp.payment_service.listener;

import com.n11bootcamp.payment_service.event.PaymentFailedEvent;
import com.n11bootcamp.payment_service.event.PaymentSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentListener.class);

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

        log.info("PAYMENT_STARTED orderId={} username={} itemCount={}", orderId, username, items.size());

        double totalPrice = items.stream()
                .mapToDouble(item ->
                        Double.parseDouble(item.get("price").toString())
                                * Integer.parseInt(item.get("quantity").toString()))
                .sum();

        boolean success = totalPrice <= 100000;

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

            log.info("PAYMENT_SUCCESS orderId={} username={}", orderId, username);

        } else {

            PaymentFailedEvent e = new PaymentFailedEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setReason("Basket total is over 100000 TL");
            e.setItems(items);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "payment.failed",
                    e
            );

            log.warn("PAYMENT_FAILED orderId={} username={} reason=Basket total is over 100000 TL totalPrice={}",
                    orderId, username, totalPrice);
        }
    }
}
