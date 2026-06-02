package com.example.waiter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.waiter.database.AppDatabase;
import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;
import com.example.waiter.models.Order;
import com.example.waiter.models.OrderItem;
import com.example.waiter.models.Table;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaiterViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<List<Table>> allTables;
    private final LiveData<List<Category>> allCategories;
    
    private final MutableLiveData<Integer> selectedTableId = new MutableLiveData<>();
    private final LiveData<Order> currentOrder;

    public WaiterViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        allTables = db.tableDao().getAllTables();
        allCategories = db.menuDao().getAllCategories();
        
        currentOrder = Transformations.switchMap(selectedTableId, tableId -> 
            db.orderDao().getOpenOrderByTable(tableId)
        );
        translateExistingCategories();
    }

    private void translateExistingCategories() {
        executor.execute(() -> {
            // Translate categories
            translateCategory("Drinks", "Напитки");
            translateCategory("Pizza", "Пицца");
            translateCategory("Desserts", "Десерты");

            // Define standard items and their images
            String colaImg = "https://i.pinimg.com/originals/83/3c/aa/833caadf4ce70819006c30da65e78813.jpg";
            String waterImg = "https://mosnapitki.ru/upload/iblock/a3d/1ay9l5nlsn4h4wnyu732k9b7c232hyr6.jpg";
            String margImg = "https://static.vecteezy.com/system/resources/previews/054/648/928/non_2x/margherita-pizza-top-view-isolated-on-transparent-background-png.png";
            String pepImg = "https://t3.ftcdn.net/jpg/07/15/38/06/360_F_715380620_0cmk5FKzLUPb4t2gtrZBRYpiyS8kqgEY.jpg";
            String cheeseImg = "https://static.tildacdn.com/tild3037-3331-4138-b633-316538663864/New_York_Cheesecake.jpg";

            // Update images and translate item names if they exist in English
            updateAndTranslateItem("Cola", "Кола", colaImg);
            updateAndTranslateItem("Water", "Вода", waterImg);
            updateAndTranslateItem("Margherita", "Маргарита", margImg);
            updateAndTranslateItem("Pepperoni", "Пепперони", pepImg);
            updateAndTranslateItem("Cheesecake", "Чизкейк", cheeseImg);

            // Also check for already translated names to update images
            updateStandardItemImage("Кола", colaImg);
            updateStandardItemImage("Вода", waterImg);
            updateStandardItemImage("Маргарита", margImg);
            updateStandardItemImage("Пепперони", pepImg);
            updateStandardItemImage("Чизкейк", cheeseImg);

            // Rename tables from English to Russian
            List<Table> tables = db.tableDao().getAllTablesNow();
            if (tables != null) {
                for (Table t : tables) {
                    if (t.getName().startsWith("Table ")) {
                        t.setName(t.getName().replace("Table ", "Стол "));
                        db.tableDao().updateTable(t);
                    }
                }
            }
        });
    }

    private void translateCategory(String oldName, String newName) {
        Category cat = db.menuDao().getCategoryByName(oldName);
        if (cat != null) {
            cat.setName(newName);
            db.menuDao().updateCategory(cat);
        }
    }

    private void updateAndTranslateItem(String oldName, String newName, String imgUrl) {
        MenuItem item = db.menuDao().getItemByName(oldName);
        if (item != null) {
            item.setName(newName);
            item.setImageUrl(imgUrl);
            db.menuDao().updateMenuItem(item);
        }
    }

    private void updateStandardItemImage(String name, String url) {
        MenuItem item = db.menuDao().getItemByName(name);
        if (item != null) {
            if (item.getImageUrl() == null || item.getImageUrl().isEmpty() || !item.getImageUrl().equals(url)) {
                item.setImageUrl(url);
                db.menuDao().updateMenuItem(item);
            }
        }
    }

    public LiveData<List<Table>> getAllTables() { return allTables; }
    public LiveData<List<Category>> getAllCategories() { return allCategories; }
    
    public void setSelectedTable(int tableId) {
        selectedTableId.setValue(tableId);
    }
    
    public LiveData<Integer> getSelectedTableId() { return selectedTableId; }

    public LiveData<Order> getCurrentOrder() { return currentOrder; }

    public LiveData<List<MenuItem>> getItemsByCategory(int categoryId) {
        return db.menuDao().getItemsByCategory(categoryId);
    }

    public LiveData<List<MenuItem>> getAllMenuItems() {
        return db.menuDao().getAllMenuItems();
    }

    public LiveData<List<com.example.waiter.models.OrderItemWithDetails>> getOrderItems(int orderId) {
        return db.orderDao().getOrderItems(orderId);
    }

    public void addTable(Table table) {
        executor.execute(() -> db.tableDao().insertTable(table));
    }

    public void addCategory(Category category) {
        executor.execute(() -> db.menuDao().insertCategory(category));
    }

    public void updateCategory(Category category) {
        executor.execute(() -> db.menuDao().updateCategory(category));
    }

    public void deleteCategory(Category category) {
        executor.execute(() -> db.menuDao().deleteCategory(category));
    }

    public void addMenuItem(MenuItem item) {
        executor.execute(() -> db.menuDao().insertMenuItem(item));
    }

    public void ensureCategoryAndAddMenuItem(String categoryName, MenuItem item) {
        executor.execute(() -> {
            Category cat = db.menuDao().getCategoryByName(categoryName);
            int catId;
            if (cat == null) {
                catId = (int) db.menuDao().insertCategoryAndGetId(new Category(0, categoryName, ""));
            } else {
                catId = cat.getId();
            }
            item.setCategoryId(catId);
            db.menuDao().insertMenuItem(item);
        });
    }

    public void createOrder(int tableId) {
        executor.execute(() -> {
            Order order = new Order(0, tableId, "OPEN", System.currentTimeMillis());
            db.orderDao().insertOrder(order);
            
            // Mark table as occupied
            Table table = db.tableDao().getTableById(tableId);
            if (table != null) {
                table.setOccupied(true);
                db.tableDao().updateTable(table);
            }
        });
    }

    public void addItemToOrder(int orderId, MenuItem item) {
        executor.execute(() -> {
            OrderItem orderItem = new OrderItem(0, orderId, item.getId(), 1, item.getPrice());
            db.orderDao().insertOrderItem(orderItem);
        });
    }

    public void removeOrderItem(int orderItemId) {
        executor.execute(() -> db.orderDao().deleteOrderItem(orderItemId));
    }

    public void deleteMenuItem(MenuItem item) {
        executor.execute(() -> db.menuDao().deleteMenuItem(item));
    }

    public void getItemById(int id, OnItemLoadedListener listener) {
        executor.execute(() -> {
            MenuItem item = db.menuDao().getItemById(id);
            listener.onItemLoaded(item);
        });
    }

    public interface OnItemLoadedListener {
        void onItemLoaded(MenuItem item);
    }
    
    public void closeOrder(Order order) {
        executor.execute(() -> {
            order.setStatus("PAID");
            db.orderDao().updateOrder(order);
            
            Table table = db.tableDao().getTableById(order.getTableId());
            if (table != null) {
                table.setOccupied(false);
                db.tableDao().updateTable(table);
            }
        });
    }
}
