package com.n11bootcamp.payment_service.controller;

import com.n11bootcamp.payment_service.dto.PaymentRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @PostMapping
    public Boolean pay(@RequestBody PaymentRequest request) {

        boolean success = Math.random() < 0.7;

        if (success) {
            System.out.println("PAYMENT SUCCESS: " + request.getOrderId());
        } else {
            System.out.println("PAYMENT FAILED: " + request.getOrderId());
        }

        return success;
    }
}