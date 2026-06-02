package com.example.waiter.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_items")
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int orderId;
    private int menuItemId;
    private int quantity;
    private double priceAtOrder; // Store price in case it changes in menu

    public OrderItem() {}

    public OrderItem(int id, int orderId, int menuItemId, int quantity, double priceAtOrder) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(double priceAtOrder) { this.priceAtOrder = priceAtOrder; }
}
