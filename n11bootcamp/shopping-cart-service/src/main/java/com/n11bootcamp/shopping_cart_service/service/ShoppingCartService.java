package com.n11bootcamp.shopping_cart_service.service;

import java.util.*;

import com.n11bootcamp.shopping_cart_service.entity.CartItem;
import com.n11bootcamp.shopping_cart_service.entity.Product;
import com.n11bootcamp.shopping_cart_service.entity.ShoppingCart;
import com.n11bootcamp.shopping_cart_service.exception.ResourceNotFoundException;
import com.n11bootcamp.shopping_cart_service.repository.ProductRepository;
import com.n11bootcamp.shopping_cart_service.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    // ✅ Microservice discovery kullanıyorsan:
    private static final String PRODUCT_SERVICE_BASE = "http://PRODUCT-SERVICE";
    // ✅ Local test için istersen bunu açıp kapatabilirsin:
    // private static final String PRODUCT_SERVICE_BASE = "http://localhost:8764";

    public ResponseEntity<ShoppingCart> createCart(String name) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCartName(name);
        return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
    }

    public ResponseEntity<ShoppingCart> addProducts(Long shoppingCartId, List<Product> products) {

        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        // ✅ Ürünleri güvenli şekilde upsert et (NULL overwrite yok)
        List<Product> persistedProducts = new ArrayList<>();

        for (Product incoming : products) {
            if (incoming == null) continue;

            Product entity = productRepository.findById(incoming.getId())
                    .orElseGet(() -> {
                        Product p = new Product();
                        p.setId(incoming.getId());
                        return p;
                    });

            if (incoming.getTitle() != null && !incoming.getTitle().isBlank()) {
                entity.setTitle(incoming.getTitle());
            }
            if (incoming.getCategory() != null && !incoming.getCategory().isBlank()) {
                entity.setCategory(incoming.getCategory());
            }
            if (incoming.getImg() != null && !incoming.getImg().isBlank()) {
                entity.setImg(incoming.getImg());
            }
            if (incoming.getLabels() != null && !incoming.getLabels().isBlank()) {
                entity.setLabels(incoming.getLabels());
            }
            if (incoming.getDescription() != null && !incoming.getDescription().isBlank()) {
                entity.setDescription(incoming.getDescription());
            }
            if (incoming.getPrice() > 0) {
                entity.setPrice(incoming.getPrice());
            }

            Product saved = productRepository.saveAndFlush(entity);
            persistedProducts.add(saved);
        }

        Set<Product> existingProducts = shoppingCart.getProducts();
        if (existingProducts == null) existingProducts = new HashSet<>();
        existingProducts.addAll(persistedProducts);

        shoppingCart.setProducts(existingProducts);
        return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
    }

    public ResponseEntity<ShoppingCart> addItem(Long shoppingCartId, Map<String, Object> itemRequest) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        Long productId = Long.valueOf(itemRequest.get("productId").toString());
        String productName = itemRequest.get("productName").toString();
        Double price = Double.valueOf(itemRequest.get("price").toString());
        Integer quantity = Integer.valueOf(itemRequest.getOrDefault("quantity", 1).toString());

        List<CartItem> items = shoppingCart.getItems();
        if (items == null) {
            items = new ArrayList<>();
            shoppingCart.setItems(items);
        }

        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setProductName(productName);
            item.setPrice(price);
        } else {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setProductName(productName);
            item.setPrice(price);
            item.setQuantity(quantity);
            item.setShoppingCart(shoppingCart);
            items.add(item);
        }

        return ResponseEntity.ok(shoppingCartRepository.save(shoppingCart));
    }

    public ResponseEntity<ShoppingCart> updateItemQuantity(Long shoppingCartId,
                                                           Long productId,
                                                           Map<String, Object> itemRequest) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        Integer quantity = Integer.valueOf(itemRequest.get("quantity").toString());

        if (quantity < 1) {
            return removeItem(shoppingCartId, productId);
        }

        shoppingCart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        return ResponseEntity.ok(shoppingCartRepository.save(shoppingCart));
    }

    public ResponseEntity<ShoppingCart> removeItem(Long shoppingCartId, Long productId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        if (shoppingCart.getItems() != null) {
            shoppingCart.getItems().removeIf(item -> item.getProductId().equals(productId));
        }

        return ResponseEntity.ok(shoppingCartRepository.save(shoppingCart));
    }

    public ResponseEntity<ShoppingCart> removeProduct(Long shoppingCartId, Long productId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        Set<Product> existingProducts = shoppingCart.getProducts();
        if (existingProducts == null) return ResponseEntity.ok().body(shoppingCart);

        existingProducts.removeIf(product -> product.getId() == productId);
        shoppingCart.setProducts(existingProducts);

        return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
    }

    // ✅ Controller compile fix için tek parametreli overload kalsın
    public ResponseEntity<Map<String, String>> getShoppingCartPrice(Long shoppingCartId) {
        return getShoppingCartPriceInternal(shoppingCartId);
    }

    private ResponseEntity<Map<String, String>> getShoppingCartPriceInternal(Long shoppingCartId) {
        Map<String, String> response = new HashMap<>();

        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        if (shoppingCart.getItems() != null && !shoppingCart.getItems().isEmpty()) {
            double totalPrice = shoppingCart.getItems()
                    .stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            response.put("total_price", Double.toString(totalPrice));
            return ResponseEntity.ok().body(response);
        }

        int totalPrice = shoppingCart.getProducts()
                .stream()
                .map(product -> restTemplate.getForObject(
                        PRODUCT_SERVICE_BASE + "/api/product/" + product.getId(), HashMap.class))
                .mapToInt(productResponse -> (int) productResponse.get("price"))
                .sum();

        response.put("total_price", Integer.toString(totalPrice));
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<ShoppingCart> getCartById(Long shoppingCartId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

        return ResponseEntity.ok(shoppingCart);
    }

    public ResponseEntity<ShoppingCart> getCartByShoppingCartName(String shoppingCartName) {
        Optional<ShoppingCart> opt = shoppingCartRepository.findByShoppingCartName(shoppingCartName);

        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            throw new ResourceNotFoundException("Shopping cart not found");
        }
    }

    public ResponseEntity<List<ShoppingCart>> getAllCarts() {
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAll();
        return ResponseEntity.ok(shoppingCarts);
    }

    public ResponseEntity<String> deleteCartById(Long shoppingCartId) {
        if (shoppingCartRepository.existsById(shoppingCartId)) {
            shoppingCartRepository.deleteById(shoppingCartId);
            return ResponseEntity.ok("Shopping Cart deleted successfully");
        } else {
            throw new ResourceNotFoundException("Shopping Cart not found in DB");
        }
    }

    public ResponseEntity<String> deleteAllCarts() {
        shoppingCartRepository.deleteAll();
        return ResponseEntity.ok("All Shopping Carts deleted successfully");
    }
}
