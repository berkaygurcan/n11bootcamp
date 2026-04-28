package com.n11bootcamp.order_service.service.impl;

import com.n11bootcamp.order_service.dto.CreateOrderRequest;
import com.n11bootcamp.order_service.dto.OrderResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderItem;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1️⃣ Order oluştur
        Order order = new Order();
        order.setUsername(request.getUsername());
        order.setStatus(OrderStatus.CREATED);

        // 2️⃣ OrderItem mapping
        List<OrderItem> items = request.getItems().stream().map(dto -> {
            OrderItem item = new OrderItem();
            item.setProductId(dto.getProductId());
            item.setProductName(dto.getProductName());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);

        // 3️⃣ total price hesapla
        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setTotalPrice(total);

        // 4️⃣ save
        Order saved = orderRepository.save(order);

        // 5️⃣ response
        OrderResponse response = new OrderResponse();
        response.setOrderId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setStatus(saved.getStatus().name());
        response.setTotalPrice(saved.getTotalPrice());

        response.setItems(saved.getItems().stream().map(i -> {
            OrderResponse.Item item = new OrderResponse.Item();
            item.setProductId(i.getProductId());
            item.setProductName(i.getProductName());
            item.setPrice(i.getPrice());
            item.setQuantity(i.getQuantity());
            return item;
        }).collect(Collectors.toList()));

        return response;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(order -> {
            OrderResponse res = new OrderResponse();
            res.setOrderId(order.getId());
            res.setUsername(order.getUsername());
            res.setStatus(order.getStatus().name());
            res.setTotalPrice(order.getTotalPrice());
            return res;
        }).collect(Collectors.toList());
    }
}