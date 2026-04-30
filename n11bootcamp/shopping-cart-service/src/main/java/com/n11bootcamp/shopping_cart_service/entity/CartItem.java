package com.n11bootcamp.shopping_cart_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id")
    @JsonIgnore
    private ShoppingCart shoppingCart;

    public Long getId() { return id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public ShoppingCart getShoppingCart() { return shoppingCart; }
    public void setShoppingCart(ShoppingCart shoppingCart) { this.shoppingCart = shoppingCart; }
}
