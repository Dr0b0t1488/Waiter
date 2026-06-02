package com.example.waiter.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;
import com.example.waiter.models.Order;
import com.example.waiter.models.OrderItem;
import com.example.waiter.models.Table;
import com.example.waiter.models.User;

@Database(entities = {Table.class, Category.class, MenuItem.class, Order.class, OrderItem.class, User.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TableDao tableDao();
    public abstract MenuDao menuDao();
    public abstract OrderDao orderDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "waiter_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
