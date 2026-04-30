package com.n11bootcamp.shopping_cart_service.controller;

import java.util.List;
import java.util.Map;

import com.n11bootcamp.shopping_cart_service.entity.Product;
import com.n11bootcamp.shopping_cart_service.entity.ShoppingCart;
import com.n11bootcamp.shopping_cart_service.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://85.159.71.66:3000", "http://94.73.134.50:3000"})
@RequestMapping("api/shopping-cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping
    public ResponseEntity<ShoppingCart> createCart(@RequestParam("name") String name) {
        return shoppingCartService.createCart(name);
    }

    @PostMapping("{id}")
    public ResponseEntity<ShoppingCart> addProductsToCart(
            @PathVariable("id") Long shoppingCartId,
            @RequestBody List<Product> products) {
        return shoppingCartService.addProducts(shoppingCartId, products);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingCart> addItemToCart(
            @PathVariable("id") Long shoppingCartId,
            @RequestBody Map<String, Object> item) {
        return shoppingCartService.addItem(shoppingCartId, item);
    }

    @PutMapping("/{id}/items/{productId}")
    public ResponseEntity<ShoppingCart> updateItemQuantity(
            @PathVariable("id") Long shoppingCartId,
            @PathVariable("productId") Long productId,
            @RequestBody Map<String, Object> item) {
        return shoppingCartService.updateItemQuantity(shoppingCartId, productId, item);
    }

    @DeleteMapping("/{id}/items/{productId}")
    public ResponseEntity<ShoppingCart> removeItem(
            @PathVariable("id") Long shoppingCartId,
            @PathVariable("productId") Long productId) {
        return shoppingCartService.removeItem(shoppingCartId, productId);
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<ShoppingCart> removeProduct(
            @PathVariable("id") Long shoppingCartId,
            @PathVariable("productId") Long productId) {
        return shoppingCartService.removeProduct(shoppingCartId, productId);
    }

    // ✅ (compile fix) signature yine Long tek parametre
    @GetMapping("/totalprice/{id}")
    public ResponseEntity<Map<String, String>> getTotalPrice(
            @PathVariable("id") Long shoppingCartId) {
        return shoppingCartService.getShoppingCartPrice(shoppingCartId);
    }

    @GetMapping("{id}")
    public ResponseEntity<ShoppingCart> getCartById(
            @PathVariable("id") Long shoppingCartId
    ) {
        return shoppingCartService.getCartById(shoppingCartId);
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ShoppingCart> getCartByShoppingCartName(
            @PathVariable("name") String shoppingCartName
    ) {
        return shoppingCartService.getCartByShoppingCartName(shoppingCartName);
    }

    @GetMapping
    public ResponseEntity<List<ShoppingCart>> getAllCarts() {
        return shoppingCartService.getAllCarts();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteCartById(@PathVariable("id") Long shoppingCartId) {
        return shoppingCartService.deleteCartById(shoppingCartId);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllCarts() {
        return shoppingCartService.deleteAllCarts();
    }
}
