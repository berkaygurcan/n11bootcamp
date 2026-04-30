package com.n11bootcamp.order_service.service.impl;


import com.n11bootcamp.order_service.dto.CreateOrderRequest;
import com.n11bootcamp.order_service.dto.OrderResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderItem;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.event.OrderCreatedEvent;
import com.n11bootcamp.order_service.exception.BadRequestException;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(OrderRepository orderRepository,
                            RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        validateCreateOrderRequest(request);

        Order order = new Order();
        order.setUsername(request.getUsername());
        order.setStatus(OrderStatus.CREATED);

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

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent();

        event.setOrderId(saved.getId());
        event.setUsername(saved.getUsername());
        event.setTotalPrice(saved.getTotalPrice());
        event.setPaymentCard(toPaymentCardEvent(request.getPaymentCard()));

        List<OrderCreatedEvent.OrderItem> itemsEvent = saved.getItems().stream().map(i -> {
            OrderCreatedEvent.OrderItem item = new OrderCreatedEvent.OrderItem();
            item.setProductId(i.getProductId());
            item.setProductName(i.getProductName());
            item.setQuantity(i.getQuantity());
            item.setPrice(i.getPrice());
            return item;
        }).toList();

        event.setItems(itemsEvent);

        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.created",
                event
        );

        log.info("ORDER_CREATED_EVENT_SENT orderId={} username={} totalPrice={}",
                saved.getId(), saved.getUsername(), saved.getTotalPrice());

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

    private OrderCreatedEvent.PaymentCard toPaymentCardEvent(CreateOrderRequest.PaymentCardDto dto) {
        if (dto == null) {
            return null;
        }

        OrderCreatedEvent.PaymentCard paymentCard = new OrderCreatedEvent.PaymentCard();
        paymentCard.setCardHolderName(dto.getCardHolderName());
        paymentCard.setCardNumber(dto.getCardNumber());
        paymentCard.setExpireMonth(dto.getExpireMonth());
        paymentCard.setExpireYear(dto.getExpireYear());
        paymentCard.setCvc(dto.getCvc());
        return paymentCard;
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Order request is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order items are required");
        }
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
