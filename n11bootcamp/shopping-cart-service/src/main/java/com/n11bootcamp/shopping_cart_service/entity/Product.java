package com.n11bootcamp.shopping_cart_service.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    private long id;

    private String title;
    private String img;
    private String labels;
    private long price;
    private String description;

    // DB NOT NULL
    private String category;

    @Column(name = "category_key")
    private String categoryKey;

    @ManyToMany(mappedBy = "products")
    private Set<ShoppingCart> shoppingCarts;


    // --- getters/setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryKey() { return categoryKey; }
    public void setCategoryKey(String categoryKey) { this.categoryKey = categoryKey; }

    @JsonIgnore
    public Set<ShoppingCart> getShoppingCarts() { return shoppingCarts; }
    public void setShoppingCarts(Set<ShoppingCart> shoppingCarts) { this.shoppingCarts = shoppingCarts; }
}
