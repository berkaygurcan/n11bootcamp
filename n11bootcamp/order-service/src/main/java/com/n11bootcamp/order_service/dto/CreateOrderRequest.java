package com.n11bootcamp.order_service.dto;

import java.util.List;

public class CreateOrderRequest {

    private String username;
    private List<OrderItemDto> items;
    private PaymentCardDto paymentCard;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public PaymentCardDto getPaymentCard() { return paymentCard; }
    public void setPaymentCard(PaymentCardDto paymentCard) { this.paymentCard = paymentCard; }

    public static class OrderItemDto {
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

    public static class PaymentCardDto {
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
