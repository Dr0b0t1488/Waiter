package com.example.waiter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waiter.R;
import com.example.waiter.models.User;

import java.util.List;

public class WaitersAdapter extends RecyclerView.Adapter<WaitersAdapter.WaiterViewHolder> {

    private List<User> waiters;
    private final OnWaiterLongClickListener longClickListener;

    public interface OnWaiterLongClickListener {
        void onWaiterLongClick(User waiter, View view);
    }

    public WaitersAdapter(List<User> waiters, OnWaiterLongClickListener longClickListener) {
        this.waiters = waiters;
        this.longClickListener = longClickListener;
    }

    public void setWaiters(List<User> waiters) {
        this.waiters = waiters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WaiterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waiter, parent, false);
        return new WaiterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaiterViewHolder holder, int position) {
        User user = waiters.get(position);
        holder.tvUsername.setText(user.getUsername());
        
        String roleText = "WAITER".equals(user.getRole()) ? 
                holder.itemView.getContext().getString(R.string.role_waiter) : 
                "Роль: Администратор";
        holder.tvRole.setText(roleText);
        
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onWaiterLongClick(user, v);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return waiters == null ? 0 : waiters.size();
    }

    static class WaiterViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole;

        public WaiterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_waiter_username);
            tvRole = itemView.findViewById(R.id.tv_waiter_role);
        }
    }
}
