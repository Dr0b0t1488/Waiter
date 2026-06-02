package com.example.waiter.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.models.Table;

import java.util.List;

public class TablesAdapter extends RecyclerView.Adapter<TablesAdapter.ViewHolder> {

    private List<Table> tables;
    private final OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(Table table);
    }

    public TablesAdapter(List<Table> tables, OnTableClickListener listener) {
        this.tables = tables;
        this.listener = listener;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Table table = tables.get(position);
        holder.tvName.setText(table.getName());
        
        if (table.isOccupied()) {
            holder.tvStatus.setText(R.string.table_occupied);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.table_occupied));
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_occupied);
        } else {
            holder.tvStatus.setText(R.string.table_free);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.table_free));
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_free);
        }

        holder.itemView.setOnClickListener(v -> listener.onTableClick(table));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_table_name);
            tvStatus = itemView.findViewById(R.id.tv_table_status);
        }
    }
}
