package com.example.clubperfomencetracker;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ProblemViewHolder> {

    private List<Problem> problemList;
    private List<Problem> problemListFull;
    private OnProblemStatusChangedListener listener;

    public interface OnProblemStatusChangedListener {
        void onStatusChanged(Problem problem, boolean isSolved);
    }

    public ProblemAdapter(List<Problem> problemList, OnProblemStatusChangedListener listener) {
        this.problemList = problemList;
        this.problemListFull = new ArrayList<>(problemList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_problem, parent, false);
        return new ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        holder.tvName.setText(problem.getName());
        holder.tvDifficulty.setText("Difficulty: " + problem.getDifficulty());
        
        holder.cbSolved.setOnCheckedChangeListener(null);
        holder.cbSolved.setChecked(problem.isSolved());
        
        holder.cbSolved.setOnCheckedChangeListener((buttonView, isChecked) -> {
            problem.setSolved(isChecked);
            if (listener != null) {
                listener.onStatusChanged(problem, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (problem.getUrl() != null && !problem.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(problem.getUrl()));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    public void filter(String query, String difficulty) {
        problemList.clear();
        for (Problem problem : problemListFull) {
            boolean matchesQuery = query.isEmpty() || problem.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesDifficulty = difficulty.equals("All") || problem.getDifficulty().equals(difficulty);
            
            if (matchesQuery && matchesDifficulty) {
                problemList.add(problem);
            }
        }
        notifyDataSetChanged();
    }

    static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDifficulty;
        CheckBox cbSolved;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProblemName);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            cbSolved = itemView.findViewById(R.id.cbSolved);
        }
    }
}