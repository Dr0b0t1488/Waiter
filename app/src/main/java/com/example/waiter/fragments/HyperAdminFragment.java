package com.example.waiter.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.databinding.FragmentHyperAdminBinding;
import com.example.waiter.viewmodels.WaiterViewModel;

import java.util.ArrayList;
import java.util.List;

public class HyperAdminFragment extends Fragment {

    private FragmentHyperAdminBinding binding;
    private WaiterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHyperAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        EstablishmentAdapter adapter = new EstablishmentAdapter(new ArrayList<>());
        binding.rvEstablishments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEstablishments.setAdapter(adapter);

        viewModel.getAllEstablishments().observe(getViewLifecycleOwner(), adapter::setCodes);
    }

    private class EstablishmentAdapter extends RecyclerView.Adapter<EstablishmentAdapter.ViewHolder> {
        private List<String> codes;

        public EstablishmentAdapter(List<String> codes) {
            this.codes = codes;
        }

        public void setCodes(List<String> codes) {
            this.codes = codes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String code = codes.get(position);
            holder.tv.setText("Код заведения: " + code);
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удалить заведение?")
                        .setMessage("Вы уверены, что хотите полностью удалить заведение с кодом " + code + "? Все данные будут потеряны.")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            viewModel.deleteEstablishment(code);
                            Toast.makeText(getContext(), "Заведение удалено", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return codes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
