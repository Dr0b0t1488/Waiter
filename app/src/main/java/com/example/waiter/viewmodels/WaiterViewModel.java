package com.example.waiter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;
import com.example.waiter.models.Order;
import com.example.waiter.models.OrderItem;
import com.example.waiter.models.OrderItemWithDetails;
import com.example.waiter.models.Table;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WaiterViewModel extends AndroidViewModel {
    private final DatabaseReference mDatabase;
    private final MutableLiveData<String> establishmentCode = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedTableId = new MutableLiveData<>();

    private final LiveData<List<Table>> allTables;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<MenuItem>> allMenuItems;

    public WaiterViewModel(@NonNull Application application) {
        super(application);
        mDatabase = FirebaseDatabase.getInstance("https://waiter-b309e-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        allTables = Transformations.switchMap(establishmentCode, code -> {
            MutableLiveData<List<Table>> data = new MutableLiveData<>();
            if (code != null) {
                mDatabase.child("establishments").child(code).child("tables")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Table> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Table t = s.getValue(Table.class);
                            if (t != null) list.add(t);
                        }
                        data.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            return data;
        });

        allCategories = Transformations.switchMap(establishmentCode, code -> {
            MutableLiveData<List<Category>> data = new MutableLiveData<>();
            if (code != null) {
                mDatabase.child("establishments").child(code).child("categories")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Category> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Category c = s.getValue(Category.class);
                            if (c != null) list.add(c);
                        }
                        data.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            return data;
        });

        allMenuItems = Transformations.switchMap(establishmentCode, code -> {
            MutableLiveData<List<MenuItem>> data = new MutableLiveData<>();
            if (code != null) {
                mDatabase.child("establishments").child(code).child("menu_items")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MenuItem> list = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            MenuItem m = s.getValue(MenuItem.class);
                            if (m != null) list.add(m);
                        }
                        data.postValue(list);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            return data;
        });
    }

    public void setEstablishmentCode(String code) {
        if (code != null && !code.equals(establishmentCode.getValue())) {
            establishmentCode.setValue(code);
        }
    }

    public LiveData<List<Table>> getAllTables() { return allTables; }
    public LiveData<List<Category>> getAllCategories() { return allCategories; }
    public LiveData<List<MenuItem>> getAllMenuItems() { return allMenuItems; }

    public void setSelectedTable(int tableId) {
        selectedTableId.setValue(tableId);
    }

    public LiveData<Order> getCurrentOrder() {
        return Transformations.switchMap(selectedTableId, tableId -> {
            MutableLiveData<Order> order = new MutableLiveData<>();
            String code = establishmentCode.getValue();
            if (code != null) {
                mDatabase.child("establishments").child(code).child("orders")
                        .orderByChild("tableId").equalTo(tableId)
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Order o = s.getValue(Order.class);
                            if (o != null && "OPEN".equals(o.getStatus())) {
                                order.postValue(o);
                                return;
                            }
                        }
                        order.postValue(null);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            return order;
        });
    }

    public LiveData<List<MenuItem>> getItemsByCategory(int categoryId) {
        return Transformations.map(allMenuItems, items -> {
            List<MenuItem> filtered = new ArrayList<>();
            for (MenuItem m : items) {
                if (m.getCategoryId() == categoryId) filtered.add(m);
            }
            return filtered;
        });
    }

    public void updateTableCount(String code, int count) {
        mDatabase.child("establishments").child(code).child("tables").removeValue()
                .addOnCompleteListener(task -> {
                    for (int i = 1; i <= count; i++) {
                        Table t = new Table(i, "Стол " + i, 4, false, code);
                        mDatabase.child("establishments").child(code).child("tables").child(String.valueOf(i)).setValue(t);
                    }
                });
    }

    public void ensureCategoryAndAddMenuItem(String categoryName, MenuItem item) {
        String code = establishmentCode.getValue();
        if (code == null) return;

        mDatabase.child("establishments").child(code).child("categories").orderByChild("name").equalTo(categoryName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot catSnapshot = snapshot.getChildren().iterator().next();
                    Category cat = catSnapshot.getValue(Category.class);
                    if (cat != null) {
                        saveMenuItem(cat.getId(), item);
                    }
                } else {
                    int newId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                    Category newCat = new Category(newId, categoryName, "", code);
                    mDatabase.child("establishments").child(code).child("categories").child(String.valueOf(newId)).setValue(newCat)
                            .addOnCompleteListener(task -> saveMenuItem(newId, item));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveMenuItem(int catId, MenuItem item) {
        String code = establishmentCode.getValue();
        if (item.getId() == 0) {
            item.setId((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        item.setCategoryId(catId);
        mDatabase.child("establishments").child(code).child("menu_items").child(String.valueOf(item.getId())).setValue(item);
    }

    public void deleteMenuItem(MenuItem item) {
        mDatabase.child("establishments").child(establishmentCode.getValue()).child("menu_items").child(String.valueOf(item.getId())).removeValue();
    }

    public void addCategory(Category category) {
        if (category.getId() == 0) category.setId((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        mDatabase.child("establishments").child(establishmentCode.getValue()).child("categories").child(String.valueOf(category.getId())).setValue(category);
    }

    public void updateCategory(Category category) {
        addCategory(category);
    }

    public void deleteCategory(Category category) {
        mDatabase.child("establishments").child(establishmentCode.getValue()).child("categories").child(String.valueOf(category.getId())).removeValue();
    }

    public void getItemById(int id, OnItemLoadedListener listener) {
        mDatabase.child("establishments").child(establishmentCode.getValue()).child("menu_items").child(String.valueOf(id))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onItemLoaded(snapshot.getValue(MenuItem.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public interface OnItemLoadedListener {
        void onItemLoaded(MenuItem item);
    }

    public void createOrder(int tableId) {
        String code = establishmentCode.getValue();
        int orderId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        Order order = new Order(orderId, tableId, "OPEN", System.currentTimeMillis(), code);
        mDatabase.child("establishments").child(code).child("orders").child(String.valueOf(orderId)).setValue(order);
        mDatabase.child("establishments").child(code).child("tables").child(String.valueOf(tableId)).child("occupied").setValue(true);
    }

    public void addItemToOrder(int orderId, MenuItem item) {
        String code = establishmentCode.getValue();
        DatabaseReference orderItemsRef = mDatabase.child("establishments").child(code).child("orders").child(String.valueOf(orderId)).child("items");
        
        orderItemsRef.child(String.valueOf(item.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OrderItem existing = snapshot.getValue(OrderItem.class);
                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity() + 1);
                        orderItemsRef.child(String.valueOf(item.getId())).setValue(existing);
                    }
                } else {
                    OrderItem newItem = new OrderItem(item.getId(), orderId, item.getId(), 1, item.getPrice());
                    orderItemsRef.child(String.valueOf(item.getId())).setValue(newItem);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public LiveData<List<OrderItemWithDetails>> getOrderItems(int orderId) {
        MutableLiveData<List<OrderItemWithDetails>> items = new MutableLiveData<>();
        String code = establishmentCode.getValue();
        
        mDatabase.child("establishments").child(code).child("orders").child(String.valueOf(orderId)).child("items")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderItemWithDetails> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    OrderItem oi = s.getValue(OrderItem.class);
                    if (oi != null) {
                        fetchMenuItem(oi, list, snapshot.getChildrenCount(), items);
                    }
                }
                if (snapshot.getChildrenCount() == 0) items.postValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return items;
    }

    private void fetchMenuItem(OrderItem oi, List<OrderItemWithDetails> list, long total, MutableLiveData<List<OrderItemWithDetails>> liveData) {
        mDatabase.child("establishments").child(establishmentCode.getValue()).child("menu_items").child(String.valueOf(oi.getMenuItemId()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MenuItem mi = snapshot.getValue(MenuItem.class);
                OrderItemWithDetails details = new OrderItemWithDetails();
                details.orderItem = oi;
                details.menuItem = mi;
                list.add(details);
                if (list.size() == total) liveData.postValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void removeOrderItem(int orderItemId) {
        // Logic to remove item from current order
    }
    
    public void closeOrder(Order order) {
        String code = establishmentCode.getValue();
        order.setStatus("PAID");
        mDatabase.child("establishments").child(code).child("orders").child(String.valueOf(order.getId())).setValue(order);
        mDatabase.child("establishments").child(code).child("tables").child(String.valueOf(order.getTableId())).child("occupied").setValue(false);
    }

    // Hyper Admin methods
    public LiveData<List<String>> getAllEstablishments() {
        MutableLiveData<List<String>> list = new MutableLiveData<>();
        mDatabase.child("establishments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> codes = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    codes.add(s.getKey());
                }
                list.postValue(codes);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return list;
    }

    public void deleteEstablishment(String code) {
        mDatabase.child("establishments").child(code).removeValue();
        // Also delete users associated with this code
        mDatabase.child("users").orderByChild("establishmentCode").equalTo(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    s.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
