package com.example.clubperfomencetracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        holder.tvDateInstructor.setText(record.getDate() + " | " + record.getInstructor());
        
        if (record.wasAttended()) {
            holder.tvStatus.setText("Attended");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setText("Absent");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvDateInstructor, tvStatus;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvClassTopic);
            tvDateInstructor = itemView.findViewById(R.id.tvClassDateInstructor);
            tvStatus = itemView.findViewById(R.id.tvAttendanceStatus);
        }
    }
}