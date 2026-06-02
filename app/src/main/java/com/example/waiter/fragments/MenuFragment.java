package com.example.waiter.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.adapters.CategoriesAdapter;
import com.example.waiter.adapters.MenuItemsAdapter;
import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;
import com.example.waiter.viewmodels.WaiterViewModel;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private WaiterViewModel viewModel;
    private int orderId;
    private MenuItemsAdapter itemsAdapter;
    private List<MenuItem> currentCategoryItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderId = getArguments() != null ? getArguments().getInt("orderId") : -1;
        viewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        RecyclerView recyclerCats = view.findViewById(R.id.recycler_categories);
        recyclerCats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        CategoriesAdapter catsAdapter = new CategoriesAdapter(new ArrayList<>(), category -> {
            viewModel.getItemsByCategory(category.getId()).observe(getViewLifecycleOwner(), items -> {
                currentCategoryItems = items;
                filterItems(((EditText) view.findViewById(R.id.et_search)).getText().toString());
            });
        });
        recyclerCats.setAdapter(catsAdapter);

        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        RecyclerView recyclerItems = view.findViewById(R.id.recycler_menu_items);
        recyclerItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        itemsAdapter = new MenuItemsAdapter(new ArrayList<>(), new MenuItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                viewModel.addItemToOrder(orderId, item);
            }

            @Override
            public void onItemLongClick(MenuItem item, View view) {
                // No long click action for waiters in menu selection
            }
        });
        recyclerItems.setAdapter(itemsAdapter);

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories.isEmpty()) {
                seedMenu();
            }
            catsAdapter.setCategories(categories);
            if (!categories.isEmpty()) {
                viewModel.getItemsByCategory(categories.get(0).getId()).observe(getViewLifecycleOwner(), items -> {
                    currentCategoryItems = items;
                    filterItems(etSearch.getText().toString());
                });
            }
        });
    }

    private void filterItems(String query) {
        List<MenuItem> filteredList = new ArrayList<>();
        for (MenuItem item : currentCategoryItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        itemsAdapter.setItems(filteredList);
    }

    private void seedMenu() {
        viewModel.addCategory(new Category(1, "Напитки", ""));
        viewModel.addCategory(new Category(2, "Пицца", ""));
        viewModel.addCategory(new Category(3, "Десерты", ""));

        viewModel.addMenuItem(new MenuItem(0, 1, "Кола", "0.5л", 150.00, "https://i.pinimg.com/originals/83/3c/aa/833caadf4ce70819006c30da65e78813.jpg"));
        viewModel.addMenuItem(new MenuItem(0, 1, "Вода", "0.5л", 100.00, "https://mosnapitki.ru/upload/iblock/a3d/1ay9l5nlsn4h4wnyu732k9b7c232hyr6.jpg"));
        viewModel.addMenuItem(new MenuItem(0, 2, "Маргарита", "Томаты, Моцарелла, Базилик", 550.00, "https://static.vecteezy.com/system/resources/previews/054/648/928/non_2x/margherita-pizza-top-view-isolated-on-transparent-background-png.png"));
        viewModel.addMenuItem(new MenuItem(0, 2, "Пепперони", "Салями, Моцарелла, Специи", 650.00, "https://t3.ftcdn.net/jpg/07/15/38/06/360_F_715380620_0cmk5FKzLUPb4t2gtrZBRYpiyS8kqgEY.jpg"));
        viewModel.addMenuItem(new MenuItem(0, 3, "Чизкейк", "Сливочный сыр, бисквит", 350.00, "https://images.gastronom.ru/MvP5F_VwHhE6-K6mI6N1v96WfM9AclG6-N_eD-I_nIs/pr:recipe-single-zoom/g:ce/rs:auto:0:0/L2Ntcy9hbGxiYWtlcy82MTNhMmEwYy05YmY0LTQyOTAtOTFiMy00M2VlNmVmZTg3M2YuanBn.jpg"));
    }
}
