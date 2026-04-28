package com.n11bootcamp.order_service.service.impl;

import com.n11bootcamp.order_service.dto.CreateOrderRequest;
import com.n11bootcamp.order_service.dto.DecreaseStockRequest;
import com.n11bootcamp.order_service.dto.OrderResponse;
import com.n11bootcamp.order_service.dto.ProductStock;
import com.n11bootcamp.order_service.dto.payment.PaymentRequest;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderItem;
import com.n11bootcamp.order_service.entity.OrderStatus;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate;

    private static final String STOCK_URL = "http://localhost:8763/api/stock";
    private static final String PAYMENT_URL = "http://localhost:8763/api/payment";

    public OrderServiceImpl(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        for (var item : request.getItems()) {

            ProductStock stock = restTemplate.getForObject(
                    STOCK_URL + "/" + item.getProductId(),
                    ProductStock.class
            );

            if (stock == null) {
                throw new RuntimeException("Stock bulunamadı! productId=" + item.getProductId());
            }

            if (stock.getAvailableQuantity() < item.getQuantity()) {
                throw new RuntimeException("Stok yetersiz! productId=" + item.getProductId());
            }
        }

        for (var item : request.getItems()) {

            DecreaseStockRequest stockRequest = new DecreaseStockRequest();
            stockRequest.setProductId(item.getProductId());
            stockRequest.setQuantity(item.getQuantity());

            restTemplate.postForObject(
                    STOCK_URL + "/reserve",
                    stockRequest,
                    Void.class
            );
        }

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

        // 🔥 PAYMENT
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(saved.getId());
        paymentRequest.setAmount(saved.getTotalPrice());

        Boolean paymentSuccess = restTemplate.postForObject(
                PAYMENT_URL,
                paymentRequest,
                Boolean.class
        );

        if (paymentSuccess != null && paymentSuccess) {

            // ✅ COMMIT
            for (var item : request.getItems()) {
                DecreaseStockRequest req = new DecreaseStockRequest();
                req.setProductId(item.getProductId());
                req.setQuantity(item.getQuantity());

                restTemplate.postForObject(
                        STOCK_URL + "/commit",
                        req,
                        Void.class
                );
            }

            saved.setStatus(OrderStatus.COMPLETED);

        } else {

            // ❌ RELEASE
            for (var item : request.getItems()) {
                DecreaseStockRequest req = new DecreaseStockRequest();
                req.setProductId(item.getProductId());
                req.setQuantity(item.getQuantity());

                restTemplate.postForObject(
                        STOCK_URL + "/release",
                        req,
                        Void.class
                );
            }
            saved.setStatus(OrderStatus.CANCELLED);

            throw new RuntimeException("Payment failed!");
        }
        orderRepository.save(saved);

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