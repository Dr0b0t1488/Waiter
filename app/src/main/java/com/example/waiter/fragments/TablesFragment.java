package com.example.waiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.adapters.TablesAdapter;
import com.example.waiter.models.Table;
import com.example.waiter.viewmodels.AuthViewModel;
import com.example.waiter.viewmodels.WaiterViewModel;

import java.util.ArrayList;

public class TablesFragment extends Fragment {

    private WaiterViewModel viewModel;
    private TablesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tables, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_tables);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new TablesAdapter(new ArrayList<>(), table -> {
            viewModel.setSelectedTable(table.getId());
            Bundle args = new Bundle();
            args.putInt("tableId", table.getId());
            Navigation.findNavController(view).navigate(R.id.action_tablesFragment_to_orderFragment, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getAllTables().observe(getViewLifecycleOwner(), tables -> {
            adapter.setTables(tables);
        });
    }
}
