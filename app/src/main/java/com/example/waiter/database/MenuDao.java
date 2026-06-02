package com.example.waiter.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;

import java.util.List;

@Dao
public interface MenuDao {
    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId")
    LiveData<List<MenuItem>> getItemsByCategory(int categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category category);

    @Update
    void updateCategory(Category category);

    @androidx.room.Delete
    void deleteCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMenuItem(MenuItem menuItem);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategoryAndGetId(Category category);

    @androidx.room.Delete
    void deleteMenuItem(MenuItem menuItem);

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    MenuItem getItemById(int id);

    @Query("SELECT * FROM menu_items WHERE name = :name LIMIT 1")
    MenuItem getItemByName(String name);

    @Update
    void updateMenuItem(MenuItem item);

    @Query("SELECT * FROM menu_items")
    LiveData<List<MenuItem>> getAllMenuItems();
}
