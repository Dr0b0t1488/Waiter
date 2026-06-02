package com.example.waiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.models.OrderItemWithDetails;

import java.util.List;
import java.util.Locale;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {

    private List<OrderItemWithDetails> items;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(OrderItemWithDetails item);
    }

    public OrderItemsAdapter(List<OrderItemWithDetails> items, OnItemLongClickListener longClickListener) {
        this.items = items;
        this.longClickListener = longClickListener;
    }

    public void setItems(List<OrderItemWithDetails> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItemWithDetails item = items.get(position);
        if (item.menuItem != null) {
            holder.tvName.setText(item.menuItem.getName());
        } else {
            holder.tvName.setText("Блюдо #" + item.orderItem.getMenuItemId());
        }
        holder.tvQuantity.setText("x" + item.orderItem.getQuantity());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%.0f ₽", item.orderItem.getPriceAtOrder() * item.orderItem.getQuantity()));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
        }
    }
}
