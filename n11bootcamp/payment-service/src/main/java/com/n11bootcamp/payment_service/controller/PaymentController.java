package com.n11bootcamp.payment_service.controller;

import com.n11bootcamp.payment_service.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping
    public Boolean pay(@RequestBody PaymentRequest request) {

        boolean success = Math.random() < 0.7;

        if (success) {
            log.info("PAYMENT_SUCCESS orderId={}", request.getOrderId());
        } else {
            log.warn("PAYMENT_FAILED orderId={}", request.getOrderId());
        }

        return success;
    }
}
