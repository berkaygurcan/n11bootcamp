package com.n11bootcamp.shopping_cart_service.service;

import com.n11bootcamp.shopping_cart_service.entity.CartItem;
import com.n11bootcamp.shopping_cart_service.entity.ShoppingCart;
import com.n11bootcamp.shopping_cart_service.repository.ProductRepository;
import com.n11bootcamp.shopping_cart_service.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    private ShoppingCartService service;

    @BeforeEach
    void setUp() {
        service = new ShoppingCartService();
        ReflectionTestUtils.setField(service, "shoppingCartRepository", shoppingCartRepository);
        ReflectionTestUtils.setField(service, "productRepository", productRepository);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    @Test
    void addItemShouldAddNewItemToCart() {
        ShoppingCart cart = cart();
        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addItem(1L, Map.of(
                "productId", 10L,
                "productName", "iPhone 15",
                "price", 50000,
                "quantity", 2
        ));

        assertThat(cart.getItems()).hasSize(1);
        CartItem item = cart.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(10L);
        assertThat(item.getProductName()).isEqualTo("iPhone 15");
        assertThat(item.getPrice()).isEqualTo(50000.0);
        assertThat(item.getQuantity()).isEqualTo(2);
        verify(shoppingCartRepository).save(cart);
    }

    @Test
    void addItemShouldIncreaseQuantityWhenItemAlreadyExists() {
        ShoppingCart cart = cart();
        CartItem existingItem = item(10L, "Old Name", 100.0, 1);
        cart.getItems().add(existingItem);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addItem(1L, Map.of(
                "productId", 10L,
                "productName", "iPhone 15",
                "price", 50000,
                "quantity", 2
        ));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        assertThat(existingItem.getProductName()).isEqualTo("iPhone 15");
        assertThat(existingItem.getPrice()).isEqualTo(50000.0);
    }

    @Test
    void updateItemQuantityShouldUpdateQuantity() {
        ShoppingCart cart = cart();
        CartItem item = item(10L, "iPhone 15", 50000.0, 1);
        cart.getItems().add(item);

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateItemQuantity(1L, 10L, Map.of("quantity", 4));

        assertThat(item.getQuantity()).isEqualTo(4);
        verify(shoppingCartRepository).save(cart);
    }

    @Test
    void updateItemQuantityShouldRemoveItemWhenQuantityIsZero() {
        ShoppingCart cart = cart();
        cart.getItems().add(item(10L, "iPhone 15", 50000.0, 1));

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateItemQuantity(1L, 10L, Map.of("quantity", 0));

        assertThat(cart.getItems()).isEmpty();
        verify(shoppingCartRepository).save(cart);
    }

    @Test
    void removeItemShouldDeleteMatchingItem() {
        ShoppingCart cart = cart();
        cart.getItems().add(item(10L, "iPhone 15", 50000.0, 1));
        cart.getItems().add(item(11L, "MacBook", 70000.0, 1));

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.removeItem(1L, 10L);

        assertThat(cart.getItems()).extracting(CartItem::getProductId).containsExactly(11L);
    }

    @Test
    void getShoppingCartPriceShouldUseItemQuantity() {
        ShoppingCart cart = cart();
        cart.getItems().add(item(10L, "iPhone 15", 50000.0, 2));
        cart.getItems().add(item(11L, "Case", 500.0, 3));

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Map<String, String> response = service.getShoppingCartPrice(1L).getBody();

        assertThat(response).containsEntry("total_price", "101500.0");
    }

    private ShoppingCart cart() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        cart.setShoppingCartName("demo");
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private CartItem item(Long productId, String productName, Double price, Integer quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setProductName(productName);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }
}
