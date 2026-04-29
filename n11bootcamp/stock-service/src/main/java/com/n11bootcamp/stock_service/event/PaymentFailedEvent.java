package com.n11bootcamp.stock_service.event;

public class PaymentFailedEvent {
    private Long orderId;
    private String username;
    private String reason;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

