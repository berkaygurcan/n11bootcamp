package com.n11bootcamp.order_service.event;

import java.io.Serializable;
import java.util.List;

public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private String username;
    private Double totalPrice;
    private List<OrderItem> items;
    private PaymentCard paymentCard;

    public static class OrderItem implements Serializable {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;

        // getters / setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    // getters / setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public PaymentCard getPaymentCard() { return paymentCard; }
    public void setPaymentCard(PaymentCard paymentCard) { this.paymentCard = paymentCard; }

    public static class PaymentCard implements Serializable {
        private String cardHolderName;
        private String cardNumber;
        private String expireMonth;
        private String expireYear;
        private String cvc;

        public String getCardHolderName() { return cardHolderName; }
        public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public String getExpireMonth() { return expireMonth; }
        public void setExpireMonth(String expireMonth) { this.expireMonth = expireMonth; }
        public String getExpireYear() { return expireYear; }
        public void setExpireYear(String expireYear) { this.expireYear = expireYear; }
        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }
    }
}
