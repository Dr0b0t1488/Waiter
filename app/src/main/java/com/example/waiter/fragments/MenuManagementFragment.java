package com.example.waiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waiter.R;
import com.example.waiter.adapters.MenuItemsAdapter;
import com.example.waiter.databinding.FragmentMenuManagementBinding;
import com.example.waiter.models.Category;
import com.example.waiter.models.MenuItem;
import com.example.waiter.utils.KeyboardUtils;
import com.example.waiter.viewmodels.AuthViewModel;
import com.example.waiter.viewmodels.WaiterViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManagementFragment extends Fragment {

    private FragmentMenuManagementBinding binding;
    private WaiterViewModel viewModel;
    private MenuItemsAdapter adapter;
    private int editingItemId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        binding.rootLayout.setOnClickListener(v -> KeyboardUtils.hideKeyboard(requireActivity()));

        int itemId = getArguments() != null ? getArguments().getInt("itemId", 0) : 0;
        if (itemId != 0) {
            editingItemId = itemId;
            viewModel.getItemById(itemId, item -> {
                if (item != null) {
                    requireActivity().runOnUiThread(() -> {
                        binding.etMenuName.setText(item.getName());
                        binding.etMenuDescription.setText(item.getDescription());
                        binding.etMenuPrice.setText(String.valueOf(item.getPrice()));
                        binding.etMenuImageUrl.setText(item.getImageUrl());
                        binding.btnAddMenuItem.setText(R.string.save_changes);
                        
                        // Load category name
                        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
                            for (Category cat : categories) {
                                if (cat.getId() == item.getCategoryId()) {
                                    binding.actvCategory.setText(cat.getName(), false);
                                    break;
                                }
                            }
                        });
                    });
                }
            });
        }

        // Setup category autocomplete
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, names);
            binding.actvCategory.setAdapter(catAdapter);
        });

        adapter = new MenuItemsAdapter(new ArrayList<>(), new MenuItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {}

            @Override
            public void onItemLongClick(MenuItem item, View view) {
                // Actions handled in AdminFragment Full Menu tab
            }
        });
        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMenuItems.setAdapter(adapter);

        viewModel.getAllMenuItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
        });

        binding.btnAddMenuItem.setOnClickListener(v -> {
            String categoryName = binding.actvCategory.getText().toString();
            String name = binding.etMenuName.getText().toString();
            String description = binding.etMenuDescription.getText().toString();
            String priceStr = binding.etMenuPrice.getText().toString();
            String imageUrl = binding.etMenuImageUrl.getText().toString();

            if (!categoryName.isEmpty() && !name.isEmpty() && !priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                MenuItem item = new MenuItem(editingItemId, 0, name, description, price, imageUrl);
                
                viewModel.ensureCategoryAndAddMenuItem(categoryName, item);
                
                binding.actvCategory.setText("");
                binding.etMenuName.setText("");
                binding.etMenuDescription.setText("");
                binding.etMenuPrice.setText("");
                binding.etMenuImageUrl.setText("");
                binding.btnAddMenuItem.setText(R.string.add_to_menu_button);
                editingItemId = 0;
                Toast.makeText(getContext(), itemId != 0 ? R.string.save_changes : R.string.item_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.fill_name_price, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
