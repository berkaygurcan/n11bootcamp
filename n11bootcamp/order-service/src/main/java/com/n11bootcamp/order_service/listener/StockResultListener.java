package com.n11bootcamp.order_service.listener;

import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StockResultListener {

    private static final Logger log = LoggerFactory.getLogger(StockResultListener.class);

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

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        log.info("ORDER_EVENT_RECEIVED orderId={} event={}", orderId, key);

        if ("stock.reserved".equals(key)) {

            log.info("PAYMENT_PROCESS_REQUESTED orderId={}", orderId);

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

            log.warn("ORDER_CANCELLED orderId={} reason=STOCK_FAILED", orderId);
        }

        else if ("payment.success".equals(key)) {

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.COMPLETED);
                order.setFailureReason(null);
                orderRepository.save(order);
            });

            log.info("ORDER_COMPLETED orderId={}", orderId);
        }

        else if ("payment.failed".equals(key)) {

            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                order.setFailureReason("PAYMENT_FAILED");
                orderRepository.save(order);
            });

            log.warn("ORDER_CANCELLED orderId={} reason=PAYMENT_FAILED", orderId);
        }
    }
}
