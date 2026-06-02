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
import com.example.waiter.databinding.FragmentAdminEstablishmentSettingsBinding;
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
                    case 3: showEstablishmentSettingsTab(); break;
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

        // Admins List
        WaitersAdapter adminAdapter = new WaitersAdapter(new ArrayList<>(), (admin, view) -> {
            showUserEditDialog(admin);
        });
        waitersBinding.rvAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
        waitersBinding.rvAdmins.setAdapter(adminAdapter);
        authViewModel.getAdmins().observe(getViewLifecycleOwner(), adminAdapter::setWaiters);

        // Waiters List
        WaitersAdapter waiterAdapter = new WaitersAdapter(new ArrayList<>(), (waiter, view) -> {
            showUserEditDialog(waiter);
        });
        waitersBinding.rvWaiters.setLayoutManager(new LinearLayoutManager(getContext()));
        waitersBinding.rvWaiters.setAdapter(waiterAdapter);
        authViewModel.getWaiters().observe(getViewLifecycleOwner(), waiterAdapter::setWaiters);
    }

    private void showUserEditDialog(User user) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        
        com.google.android.material.textfield.TextInputEditText etUser = view.findViewById(R.id.et_username);
        com.google.android.material.textfield.TextInputEditText etPass = view.findViewById(R.id.et_password);
        com.google.android.material.textfield.TextInputEditText etCode = view.findViewById(R.id.et_code);
        com.google.android.material.textfield.TextInputLayout tilCode = view.findViewById(R.id.til_code);

        etUser.setText(user.getUsername());
        etPass.setText(user.getPassword());
        
        if ("ADMIN".equals(user.getRole())) {
            tilCode.setVisibility(View.VISIBLE);
            etCode.setText(user.getEstablishmentCode());
        } else {
            tilCode.setVisibility(View.GONE);
        }

        builder.setView(view)
                .setTitle("Изменить профиль")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String username = etUser.getText().toString();
                    String password = etPass.getText().toString();
                    if (!username.isEmpty() && !password.isEmpty()) {
                        user.setUsername(username);
                        user.setPassword(password);
                        if ("ADMIN".equals(user.getRole())) {
                            user.setEstablishmentCode(etCode.getText().toString());
                            authViewModel.updateCurrentUser(user);
                        } else {
                            authViewModel.updateWaiter(user);
                        }
                        Toast.makeText(getContext(), R.string.waiter_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Удалить", (dialog, which) -> {
                    if (!"ADMIN".equals(user.getRole())) {
                        authViewModel.deleteWaiter(user);
                        Toast.makeText(getContext(), R.string.waiter_deleted, Toast.LENGTH_SHORT).show();
                    } else {
                        authViewModel.getAdminCount(user.getEstablishmentCode(), count -> {
                            requireActivity().runOnUiThread(() -> {
                                if (count > 1) {
                                    authViewModel.deleteWaiter(user);
                                    Toast.makeText(getContext(), R.string.waiter_deleted, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Нельзя удалить последнего администратора", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .setNeutralButton("Отмена", null)
                .show();
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
                    User current = authViewModel.getCurrentUser().getValue();
                    String code = current != null ? current.getEstablishmentCode() : "";
                    waiterViewModel.addCategory(new Category(0, name, "", code));
                    Toast.makeText(getContext(), R.string.category_added, Toast.LENGTH_SHORT).show();
                }
                catBinding.etCategoryName.setText("");
                KeyboardUtils.hideKeyboard(requireActivity());
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

    private void showEstablishmentSettingsTab() {
        binding.adminContainer.removeAllViews();
        FragmentAdminEstablishmentSettingsBinding setBinding = FragmentAdminEstablishmentSettingsBinding.inflate(getLayoutInflater(), binding.adminContainer, true);

        User currentUser = authViewModel.getCurrentUser().getValue();
        if (currentUser == null) return;

        setBinding.btnUpdateTables.setOnClickListener(v -> {
            String countStr = setBinding.etTableCount.getText().toString();
            if (!countStr.isEmpty()) {
                int count = Integer.parseInt(countStr);
                if (count >= 1 && count <= 100) {
                    waiterViewModel.updateTableCount(currentUser.getEstablishmentCode(), count);
                    Toast.makeText(getContext(), "Количество столов обновлено: " + count, Toast.LENGTH_SHORT).show();
                    KeyboardUtils.hideKeyboard(requireActivity());
                } else {
                    Toast.makeText(getContext(), "Введите число от 1 до 100", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
