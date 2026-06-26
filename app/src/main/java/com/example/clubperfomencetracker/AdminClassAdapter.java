package com.example.clubperfomencetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminClassAdapter extends RecyclerView.Adapter<AdminClassAdapter.AdminClassViewHolder> {

    private List<ClassInfo> classList;
    private OnClassActionListener listener;

    public interface OnClassActionListener {
        void onEdit(ClassInfo classInfo);
        void onDelete(ClassInfo classInfo);
    }

    public AdminClassAdapter(List<ClassInfo> classList, OnClassActionListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_class, parent, false);
        return new AdminClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminClassViewHolder holder, int position) {
        ClassInfo classInfo = classList.get(position);
        holder.tvTopic.setText(classInfo.topic);
        holder.tvDateTime.setText(classInfo.date + " • " + classInfo.time);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(classInfo));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(classInfo));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class AdminClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvDateTime;
        ImageButton btnEdit, btnDelete;

        public AdminClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvAdminClassTopic);
            tvDateTime = itemView.findViewById(R.id.tvAdminClassDateTime);
            btnEdit = itemView.findViewById(R.id.btnEditClass);
            btnDelete = itemView.findViewById(R.id.btnDeleteClass);
        }
    }
}
