package com.n11bootcamp.stock_service.listener;

import com.n11bootcamp.stock_service.event.StockReservedEvent;
import com.n11bootcamp.stock_service.event.StockFailedEvent;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.n11bootcamp.stock_service.service.StockService;

import java.util.List;
import java.util.Map;

@Component
public class StockListener {

    private static final Logger log = LoggerFactory.getLogger(StockListener.class);

    private final RabbitTemplate rabbitTemplate;
    private final StockService stockService;

    public StockListener(RabbitTemplate rabbitTemplate, StockService stockService) {
        this.rabbitTemplate = rabbitTemplate;
        this.stockService = stockService;
    }

    @RabbitListener(queues = "stock.queue")
    public void handle(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String routingKey) {

        Long orderId = Long.valueOf(payload.get("orderId").toString());

        if ("payment.failed".equals(routingKey)) {
            releaseReservedStock(payload, orderId);
            return;
        }

        if ("payment.success".equals(routingKey)) {
            commitReservedStock(payload, orderId);
            return;
        }

        if (!"order.created".equals(routingKey)) {
            return;
        }

        String username = payload.get("username").toString();
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        log.info("STOCK CHECK → {}", orderId);

        try {
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                stockService.reserveStock(productId, quantity);
            }

            StockReservedEvent e = new StockReservedEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setItems(items);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "stock.reserved",
                    e
            );

            log.info("STOCK RESERVED → {}", orderId);

        } catch (RuntimeException exception) {

            StockFailedEvent e = new StockFailedEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setReason(exception.getMessage());

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "stock.failed",
                    e
            );

            log.warn("STOCK FAILED → {}", orderId);
        }
    }

    private void releaseReservedStock(Map<String, Object> payload, Long orderId) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        if (items == null) {
            log.warn("STOCK RELEASE SKIPPED → items empty for order {}", orderId);
            return;
        }

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            stockService.releaseStock(productId, quantity);
        }

        log.info("STOCK RELEASED → {}", orderId);
    }

    private void commitReservedStock(Map<String, Object> payload, Long orderId) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        if (items == null) {
            log.warn("STOCK COMMIT SKIPPED → items empty for order {}", orderId);
            return;
        }

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            stockService.commitStock(productId, quantity);
        }

        log.info("STOCK COMMITTED → {}", orderId);
    }
}
