package com.example.waiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waiter.R;
import com.example.waiter.adapters.CategoriesManagementAdapter;
import com.example.waiter.adapters.MenuItemsAdapter;
import com.example.waiter.adapters.WaitersAdapter;
import com.example.waiter.databinding.FragmentAdminBinding;
import com.example.waiter.databinding.FragmentAdminCategoriesBinding;
import com.example.waiter.databinding.FragmentAdminMenuBinding;
import com.example.waiter.databinding.FragmentAdminWaitersBinding;
import com.example.waiter.models.Category;
import com.example.waiter.models.User;
import com.example.waiter.utils.KeyboardUtils;
import com.example.waiter.viewmodels.AuthViewModel;
import com.example.waiter.viewmodels.WaiterViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    private AuthViewModel authViewModel;
    private WaiterViewModel waiterViewModel;
    private User editingWaiter = null;
    private Category editingCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        waiterViewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        setupTabs();
    }

    private void setupTabs() {
        showWaitersTab();

        binding.adminTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: showWaitersTab(); break;
                    case 1: showCategoriesTab(); break;
                    case 2: showMenuTab(); break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showWaitersTab() {
        binding.adminContainer.removeAllViews();
        FragmentAdminWaitersBinding waitersBinding = FragmentAdminWaitersBinding.inflate(getLayoutInflater(), binding.adminContainer, true);
        
        waitersBinding.getRoot().setOnClickListener(v -> KeyboardUtils.hideKeyboard(requireActivity()));

        WaitersAdapter adapter = new WaitersAdapter(new ArrayList<>(), (waiter, view) -> {
            showWaiterPopup(waiter, view, waitersBinding);
        });
        waitersBinding.rvWaiters.setLayoutManager(new LinearLayoutManager(getContext()));
        waitersBinding.rvWaiters.setAdapter(adapter);

        authViewModel.getWaiters().observe(getViewLifecycleOwner(), adapter::setWaiters);

        waitersBinding.btnAddWaiter.setOnClickListener(v -> {
            String username = waitersBinding.etNewWaiterUsername.getText().toString();
            String password = waitersBinding.etNewWaiterPassword.getText().toString();
            if (!username.isEmpty() && !password.isEmpty()) {
                if (editingWaiter != null) {
                    editingWaiter.setUsername(username);
                    editingWaiter.setPassword(password);
                    authViewModel.updateWaiter(editingWaiter);
                    editingWaiter = null;
                    waitersBinding.btnAddWaiter.setText(R.string.add_waiter_button);
                    Toast.makeText(getContext(), R.string.waiter_updated, Toast.LENGTH_SHORT).show();
                } else {
                    authViewModel.addWaiter(username, password);
                    Toast.makeText(getContext(), R.string.waiter_added, Toast.LENGTH_SHORT).show();
                }
                waitersBinding.etNewWaiterUsername.setText("");
                waitersBinding.etNewWaiterPassword.setText("");
            } else {
                Toast.makeText(getContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWaiterPopup(User waiter, View view, FragmentAdminWaitersBinding waitersBinding) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add(getString(R.string.edit));
        popup.getMenu().add(getString(R.string.delete));

        popup.setOnMenuItemClickListener(menuItem -> {
            CharSequence title = menuItem.getTitle();
            if (title == null) return false;
            
            if (getString(R.string.delete).contentEquals(title)) {
                authViewModel.deleteWaiter(waiter);
                Toast.makeText(getContext(), R.string.waiter_deleted, Toast.LENGTH_SHORT).show();
            } else if (getString(R.string.edit).contentEquals(title)) {
                editingWaiter = waiter;
                waitersBinding.etNewWaiterUsername.setText(waiter.getUsername());
                waitersBinding.etNewWaiterPassword.setText(waiter.getPassword());
                waitersBinding.btnAddWaiter.setText(R.string.save_waiter);
            }
            return true;
        });
        popup.show();
    }

    private void showCategoriesTab() {
        binding.adminContainer.removeAllViews();
        FragmentAdminCategoriesBinding catBinding = FragmentAdminCategoriesBinding.inflate(getLayoutInflater(), binding.adminContainer, true);
        
        catBinding.getRoot().setOnClickListener(v -> KeyboardUtils.hideKeyboard(requireActivity()));

        CategoriesManagementAdapter adapter = new CategoriesManagementAdapter(new ArrayList<>(), (category, view) -> {
            showCategoryPopup(category, view, catBinding);
        });
        catBinding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        catBinding.rvCategories.setAdapter(adapter);

        waiterViewModel.getAllCategories().observe(getViewLifecycleOwner(), adapter::setCategories);

        catBinding.btnAddCategory.setOnClickListener(v -> {
            String name = catBinding.etCategoryName.getText().toString();
            if (!name.isEmpty()) {
                if (editingCategory != null) {
                    editingCategory.setName(name);
                    waiterViewModel.updateCategory(editingCategory);
                    editingCategory = null;
                    catBinding.btnAddCategory.setText(R.string.add_waiter_button);
                    Toast.makeText(getContext(), R.string.category_updated, Toast.LENGTH_SHORT).show();
                } else {
                    waiterViewModel.addCategory(new Category(0, name, ""));
                    Toast.makeText(getContext(), R.string.category_added, Toast.LENGTH_SHORT).show();
                }
                catBinding.etCategoryName.setText("");
            }
        });
    }

    private void showCategoryPopup(Category category, View view, FragmentAdminCategoriesBinding catBinding) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add(getString(R.string.edit));
        popup.getMenu().add(getString(R.string.delete));

        popup.setOnMenuItemClickListener(menuItem -> {
            CharSequence title = menuItem.getTitle();
            if (title == null) return false;

            if (getString(R.string.delete).contentEquals(title)) {
                waiterViewModel.deleteCategory(category);
                Toast.makeText(getContext(), R.string.category_deleted, Toast.LENGTH_SHORT).show();
            } else if (getString(R.string.edit).contentEquals(title)) {
                editingCategory = category;
                catBinding.etCategoryName.setText(category.getName());
                catBinding.btnAddCategory.setText(R.string.save_waiter);
            }
            return true;
        });
        popup.show();
    }

    private void showMenuTab() {
        binding.adminContainer.removeAllViews();
        FragmentAdminMenuBinding menuBinding = FragmentAdminMenuBinding.inflate(getLayoutInflater(), binding.adminContainer, true);
        
        MenuItemsAdapter adapter = new MenuItemsAdapter(new ArrayList<>(), new MenuItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.example.waiter.models.MenuItem item) {}

            @Override
            public void onItemLongClick(com.example.waiter.models.MenuItem item, View view) {
                showMenuPopup(item, view);
            }
        });
        menuBinding.rvFullMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        menuBinding.rvFullMenu.setAdapter(adapter);

        waiterViewModel.getAllMenuItems().observe(getViewLifecycleOwner(), adapter::setItems);

        menuBinding.btnGotoManage.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_adminFragment_to_menuManagementFragment);
        });
    }

    private void showMenuPopup(com.example.waiter.models.MenuItem item, View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add(getString(R.string.edit));
        popup.getMenu().add(getString(R.string.delete));

        popup.setOnMenuItemClickListener(menuItem -> {
            CharSequence title = menuItem.getTitle();
            if (title == null) return false;

            if (getString(R.string.delete).contentEquals(title)) {
                waiterViewModel.deleteMenuItem(item);
                Toast.makeText(getContext(), R.string.item_removed, Toast.LENGTH_SHORT).show();
            } else if (getString(R.string.edit).contentEquals(title)) {
                Bundle args = new Bundle();
                args.putInt("itemId", item.getId());
                Navigation.findNavController(requireView()).navigate(R.id.action_adminFragment_to_menuManagementFragment, args);
            }
            return true;
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
