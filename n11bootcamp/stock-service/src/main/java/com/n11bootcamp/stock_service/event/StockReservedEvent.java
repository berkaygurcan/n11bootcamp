package com.n11bootcamp.stock_service.event;

import java.util.List;
import java.util.Map;

public class StockReservedEvent {
    private Long orderId;
    private String username;
    private List<Map<String, Object>> items;
    private Map<String, Object> paymentCard;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }

    public Map<String, Object> getPaymentCard() { return paymentCard; }
    public void setPaymentCard(Map<String, Object> paymentCard) { this.paymentCard = paymentCard; }
}
