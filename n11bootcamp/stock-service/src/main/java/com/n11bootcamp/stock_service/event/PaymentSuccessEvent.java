package com.n11bootcamp.stock_service.event;

public class PaymentSuccessEvent {
    private Long orderId;
    private String username;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
