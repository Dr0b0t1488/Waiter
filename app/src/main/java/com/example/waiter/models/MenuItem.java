package com.example.waiter.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "menu_items")
public class MenuItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int categoryId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    public MenuItem() {
        // Required for Firebase
    }

    public MenuItem(int id, int categoryId, String name, String description, double price, String imageUrl) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
