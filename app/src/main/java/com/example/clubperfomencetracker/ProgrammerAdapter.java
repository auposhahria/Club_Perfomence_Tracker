package com.example.clubperfomencetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProgrammerAdapter extends RecyclerView.Adapter<ProgrammerAdapter.ProgrammerViewHolder> {

    private List<Programmer> programmerList;
    private List<Programmer> programmerListFull;
    private OnProgrammerClickListener listener;

    public interface OnProgrammerClickListener {
        void onProgrammerClick(Programmer programmer);
    }

    public ProgrammerAdapter(List<Programmer> programmerList, OnProgrammerClickListener listener) {
        this.programmerList = programmerList;
        this.programmerListFull = new ArrayList<>(programmerList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgrammerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_programmer, parent, false);
        return new ProgrammerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammerViewHolder holder, int position) {
        Programmer programmer = programmerList.get(position);
        holder.tvName.setText(programmer.getName());
        holder.tvCfId.setText("CF ID: " + programmer.getCfId());
        holder.tvMaxRating.setText("Max Rating: " + programmer.getMaxRating());
        holder.itemView.setOnClickListener(v -> listener.onProgrammerClick(programmer));
    }

    @Override
    public int getItemCount() {
        return programmerList.size();
    }

    public void filter(String text) {
        programmerList.clear();
        if (text.isEmpty()) {
            programmerList.addAll(programmerListFull);
        } else {
            text = text.toLowerCase();
            for (Programmer item : programmerListFull) {
                if (item.getName().toLowerCase().contains(text) || item.getCfId().toLowerCase().contains(text)) {
                    programmerList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ProgrammerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCfId, tvMaxRating;

        public ProgrammerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProgrammerName);
            tvCfId = itemView.findViewById(R.id.tvCfId);
            tvMaxRating = itemView.findViewById(R.id.tvMaxRating);
        }
    }
}