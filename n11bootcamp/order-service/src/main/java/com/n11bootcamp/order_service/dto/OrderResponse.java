package com.n11bootcamp.order_service.dto;

import java.util.List;

public class OrderResponse {

    private Long orderId;
    private String username;
    private Double totalPrice;
    private String status;
    private String failureReason;
    private List<Item> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
