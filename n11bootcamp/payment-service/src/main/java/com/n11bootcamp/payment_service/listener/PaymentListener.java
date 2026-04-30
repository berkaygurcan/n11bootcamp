package com.n11bootcamp.payment_service.listener;

import com.n11bootcamp.payment_service.event.PaymentFailedEvent;
import com.n11bootcamp.payment_service.event.PaymentSuccessEvent;
import com.n11bootcamp.payment_service.service.IyzicoPaymentClient;
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
    private final IyzicoPaymentClient iyzicoPaymentClient;

    public PaymentListener(RabbitTemplate rabbitTemplate,
                           IyzicoPaymentClient iyzicoPaymentClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.iyzicoPaymentClient = iyzicoPaymentClient;
    }

    @RabbitListener(queues = "payment.queue")
    public void handle(Map<String, Object> payload,
                       @Header("amqp_receivedRoutingKey") String key) {

        if (!"stock.reserved".equals(key)) {
            return;
        }

        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String username = payload.get("username").toString();
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        Map<String, Object> paymentCard = (Map<String, Object>) payload.get("paymentCard");

        log.info("PAYMENT_STARTED orderId={} username={} itemCount={}", orderId, username, items.size());

        double totalPrice = items.stream()
                .mapToDouble(item ->
                        Double.parseDouble(item.get("price").toString())
                                * Integer.parseInt(item.get("quantity").toString()))
                .sum();

        PaymentResult paymentResult = processPayment(orderId, username, items, paymentCard, totalPrice);

        if (paymentResult.success()) {

            PaymentSuccessEvent e = new PaymentSuccessEvent();
            e.setOrderId(orderId);
            e.setUsername(username);
            e.setItems(items);

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "payment.success",
                    e
            );

            log.info("PAYMENT_SUCCESS orderId={} username={}", orderId, username);

        } else {

            publishPaymentFailed(orderId, username, items, paymentResult.reason(), totalPrice);
        }
    }

    private PaymentResult processPayment(Long orderId,
                                         String username,
                                         List<Map<String, Object>> items,
                                         Map<String, Object> paymentCard,
                                         double totalPrice) {
        if (iyzicoPaymentClient.isConfigured()) {
            try {
                log.info("IYZICO_PAYMENT_STARTED orderId={} username={}", orderId, username);
                IyzicoPaymentClient.IyzicoPaymentResult result =
                        iyzicoPaymentClient.pay(orderId, username, items, paymentCard);
                log.info("IYZICO_PAYMENT_RESULT orderId={} username={} success={} reason={}",
                        orderId, username, result.success(), result.reason());
                return new PaymentResult(result.success(), result.reason());
            } catch (Exception exception) {
                log.warn("IYZICO_PAYMENT_ERROR orderId={} username={} error={}",
                        orderId, username, exception.getMessage());
                return new PaymentResult(false, "Iyzico payment error: " + exception.getMessage());
            }
        }

        log.info("MOCK_PAYMENT_STARTED orderId={} username={} totalPrice={}", orderId, username, totalPrice);

        if (totalPrice > 100000) {
            return new PaymentResult(false, "Basket total is over 100000 TL");
        }

        return new PaymentResult(true, null);
    }

    private void publishPaymentFailed(Long orderId,
                                      String username,
                                      List<Map<String, Object>> items,
                                      String reason,
                                      double totalPrice) {
        PaymentFailedEvent e = new PaymentFailedEvent();
        e.setOrderId(orderId);
        e.setUsername(username);
        e.setReason(reason);
        e.setItems(items);

        rabbitTemplate.convertAndSend(
                "order.exchange",
                "payment.failed",
                e
        );

        log.warn("PAYMENT_FAILED orderId={} username={} reason={} totalPrice={}",
                orderId, username, reason, totalPrice);
    }

    private record PaymentResult(boolean success, String reason) {
    }
}
