package com.n11bootcamp.shopping_cart_service.service;

import java.util.*;

import com.n11bootcamp.shopping_cart_service.entity.Product;
import com.n11bootcamp.shopping_cart_service.entity.ShoppingCart;
import org.springframework.http.*;
public interface ShoppingCartService {
    ResponseEntity<ShoppingCart> createCart(String name);
    ResponseEntity<ShoppingCart> addProducts(Long shoppingCartId, List<Product> products);
    ResponseEntity<ShoppingCart> addItem(Long shoppingCartId, Map<String, Object> itemRequest);
    ResponseEntity<ShoppingCart> updateItemQuantity(Long shoppingCartId, Long productId, Map<String, Object> itemRequest);
    ResponseEntity<ShoppingCart> removeItem(Long shoppingCartId, Long productId);
    ResponseEntity<ShoppingCart> removeProduct(Long shoppingCartId, Long productId);
    ResponseEntity<Map<String, String>> getShoppingCartPrice(Long shoppingCartId);
    ResponseEntity<ShoppingCart> getCartById(Long shoppingCartId);
    ResponseEntity<ShoppingCart> getCartByShoppingCartName(String shoppingCartName);
    ResponseEntity<List<ShoppingCart>> getAllCarts();
    ResponseEntity<String> deleteCartById(Long shoppingCartId);
    ResponseEntity<String> deleteAllCarts();
}
