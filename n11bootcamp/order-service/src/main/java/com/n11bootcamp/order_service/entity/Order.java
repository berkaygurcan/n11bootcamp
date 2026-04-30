package com.n11bootcamp.order_service.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String failureReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    // Getter Setter
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = items;
        if (items != null) {
            items.forEach(i -> i.setOrder(this));
        }
    }
}
