package com.example.clubperfomencetracker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassRecord> classList;

    public ClassAdapter(List<ClassRecord> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassRecord record = classList.get(position);
        holder.tvTopic.setText(record.getTopic());
        holder.tvDate.setText(record.getDate());
        
        String status = record.getStatus();
        holder.tvStatus.setText(status);

        int bgColor;
        int textColor;

        if ("Present".equalsIgnoreCase(status)) {
            bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_present_bg);
            textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_present_text);
        } else if ("Absent".equalsIgnoreCase(status)) {
            bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_absent_bg);
            textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_absent_text);
        } else { // Late
            bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_late_bg);
            textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.attendance_late_text);
        }

        holder.tvStatus.setTextColor(textColor);
        GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();
        if (background != null) {
            background.setColor(bgColor);
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvDate, tvStatus;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvClassTopic);
            tvDate = itemView.findViewById(R.id.tvClassDate);
            tvStatus = itemView.findViewById(R.id.tvAttendanceStatus);
        }
    }
}