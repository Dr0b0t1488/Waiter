package com.example.waiter.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.waiter.models.Order;
import com.example.waiter.models.OrderItem;

import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM orders WHERE tableId = :tableId AND status = 'OPEN' LIMIT 1")
    LiveData<Order> getOpenOrderByTable(int tableId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrder(Order order);

    @Update
    void updateOrder(Order order);

    @androidx.room.Transaction
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    LiveData<List<com.example.waiter.models.OrderItemWithDetails>> getOrderItems(int orderId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItem(OrderItem orderItem);

    @Update
    void updateOrderItem(OrderItem orderItem);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND menuItemId = :menuItemId LIMIT 1")
    OrderItem getOrderItemSync(int orderId, int menuItemId);

    @Query("DELETE FROM order_items WHERE id = :orderItemId")
    void deleteOrderItem(int orderItemId);
}
