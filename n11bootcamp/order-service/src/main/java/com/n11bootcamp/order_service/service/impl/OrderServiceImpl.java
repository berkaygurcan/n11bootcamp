package com.n11bootcamp.order_service.service.impl;


import com.n11bootcamp.order_service.dto.CreateOrderRequest;
import com.n11bootcamp.order_service.dto.OrderResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderItem;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.event.OrderCreatedEvent;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(OrderRepository orderRepository,
                            RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
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

        OrderCreatedEvent event = new OrderCreatedEvent();

        event.setOrderId(saved.getId());
        event.setUsername(saved.getUsername());
        event.setTotalPrice(saved.getTotalPrice());

// items map
        List<OrderCreatedEvent.OrderItem> itemsEvent = saved.getItems().stream().map(i -> {
            OrderCreatedEvent.OrderItem item = new OrderCreatedEvent.OrderItem();
            item.setProductId(i.getProductId());
            item.setProductName(i.getProductName());
            item.setQuantity(i.getQuantity());
            item.setPrice(i.getPrice());
            return item;
        }).toList();

        event.setItems(itemsEvent);

// 🔥 EVENT GÖNDER
        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.created",
                event
        );

        System.out.println("ORDER CREATED EVENT GÖNDERİLDİ → " + saved.getId());

        // 5️⃣ response
        OrderResponse response = new OrderResponse();
        response.setOrderId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setStatus(saved.getStatus().name());
        response.setFailureReason(saved.getFailureReason());
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
            res.setFailureReason(order.getFailureReason());
            res.setTotalPrice(order.getTotalPrice());
            return res;
        }).collect(Collectors.toList());
    }
}
