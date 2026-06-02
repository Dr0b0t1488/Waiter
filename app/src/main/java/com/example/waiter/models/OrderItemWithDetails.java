package com.example.waiter.models;

import androidx.room.Embedded;
import androidx.room.Relation;

public class OrderItemWithDetails {
    @Embedded
    public OrderItem orderItem;

    @Relation(
        parentColumn = "menuItemId",
        entityColumn = "id"
    )
    public MenuItem menuItem;
}
