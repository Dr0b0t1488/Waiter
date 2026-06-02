package com.example.waiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.models.Category;

import java.util.List;

public class CategoriesManagementAdapter extends RecyclerView.Adapter<CategoriesManagementAdapter.ViewHolder> {

    private List<Category> categories;
    private final OnCategoryLongClickListener listener;

    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(Category category, View view);
    }

    public CategoriesManagementAdapter(List<Category> categories, OnCategoryLongClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView.setText(category.getName());
        holder.itemView.setOnLongClickListener(v -> {
            listener.onCategoryLongClick(category, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
