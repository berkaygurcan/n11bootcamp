package com.n11bootcamp.payment_service.event;

import java.util.List;
import java.util.Map;

public class PaymentFailedEvent {
    private Long orderId;
    private String username;
    private String reason;
    private List<Map<String, Object>> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}
