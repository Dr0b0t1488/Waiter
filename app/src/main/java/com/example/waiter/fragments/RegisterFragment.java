package com.example.waiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.waiter.R;
import com.example.waiter.databinding.FragmentRegisterBinding;
import com.example.waiter.models.User;
import com.example.waiter.utils.KeyboardUtils;
import com.example.waiter.viewmodels.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding.rootLayout.setOnClickListener(v -> KeyboardUtils.hideKeyboard(requireActivity()));

        binding.rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_admin) {
                binding.tvCodeDesc.setText("Создайте новый код для вашего заведения");
                binding.tilCode.setError(null);
                binding.btnRegister.setText(R.string.register_admin);
            } else {
                binding.tvCodeDesc.setText("Введите существующий код заведения");
                binding.btnRegister.setText(R.string.register_waiter);
            }
        });

        // Set initial button text
        binding.btnRegister.setText(R.string.register_waiter);

        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            String code = binding.etCode.getText().toString();
            String role = binding.rbAdmin.isChecked() ? "ADMIN" : "WAITER";

            if (username.isEmpty() || password.isEmpty() || code.isEmpty()) {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if ("WAITER".equals(role)) {
                // Check if code exists
                authViewModel.isCodeValid(code, result -> {
                    requireActivity().runOnUiThread(() -> {
                        if ("VALID".equals(result)) {
                            binding.tilCode.setError(null);
                            performRegistration(username, password, role, code);
                        } else if ("NOT_FOUND".equals(result)) {
                            binding.tilCode.setError("Код заведения не существует");
                        } else {
                            binding.tilCode.setError(result); // Show detailed error
                        }
                    });
                });
            } else {
                // For Admin, just register
                performRegistration(username, password, role, code);
            }
        });
    }

    private void performRegistration(String user, String pass, String role, String code) {
        authViewModel.registerUser(new User(user, pass, role, code), result -> {
            requireActivity().runOnUiThread(() -> {
                if ("SUCCESS".equals(result)) {
                    Toast.makeText(requireContext(), "Регистрация успешна! Теперь вы можете войти.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Ошибка регистрации: " + result, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
