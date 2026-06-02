package com.example.waiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.adapters.OrderItemsAdapter;
import com.example.waiter.viewmodels.WaiterViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class OrderFragment extends Fragment {

    private WaiterViewModel viewModel;
    private OrderItemsAdapter adapter;
    private int tableId;
    private int currentOrderId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableId = getArguments() != null ? getArguments().getInt("tableId") : -1;
        viewModel = new ViewModelProvider(requireActivity()).get(WaiterViewModel.class);

        TextView tvTitle = view.findViewById(R.id.tv_order_title);
        tvTitle.setText(getString(R.string.table_name_format, tableId));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_order_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderItemsAdapter(new ArrayList<>(), item -> {
            viewModel.removeOrderItem(item.orderItem.getId());
            Toast.makeText(getContext(), R.string.item_removed, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        TextView tvTotal = view.findViewById(R.id.tv_total_price);

        viewModel.getCurrentOrder().observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                currentOrderId = order.getId();
                viewModel.getOrderItems(order.getId()).observe(getViewLifecycleOwner(), items -> {
                    adapter.setItems(items);
                    double total = 0;
                    for (com.example.waiter.models.OrderItemWithDetails item : items) {
                        total += item.orderItem.getPriceAtOrder() * item.orderItem.getQuantity();
                    }
                    tvTotal.setText(getString(R.string.total_format, total));
                });
            } else {
                currentOrderId = -1;
                adapter.setItems(new ArrayList<>());
                tvTotal.setText(getString(R.string.total_format, 0.0));
            }
        });

        Button btnAdd = view.findViewById(R.id.btn_add_items);
        btnAdd.setOnClickListener(v -> {
            if (currentOrderId == -1) {
                viewModel.createOrder(tableId);
                // The observer will pick up the new order and we'll navigate then or just assume it's created.
                // For simplicity, let's wait for the order to be created.
                Toast.makeText(getContext(), R.string.opening_order, Toast.LENGTH_SHORT).show();
            } else {
                Bundle args = new Bundle();
                args.putInt("orderId", currentOrderId);
                Navigation.findNavController(view).navigate(R.id.action_orderFragment_to_menuFragment, args);
            }
        });

        Button btnPay = view.findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(v -> {
            viewModel.getCurrentOrder().observe(getViewLifecycleOwner(), order -> {
                if (order != null && "OPEN".equals(order.getStatus())) {
                    viewModel.closeOrder(order);
                    Toast.makeText(getContext(), R.string.order_closed, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                }
            });
        });
    }
}
