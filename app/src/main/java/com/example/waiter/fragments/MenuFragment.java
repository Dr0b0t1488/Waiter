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
import com.example.waiter.viewmodels.AuthViewModel;
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
}
