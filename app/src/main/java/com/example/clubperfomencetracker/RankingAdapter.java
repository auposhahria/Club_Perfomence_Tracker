package com.example.clubperfomencetracker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<Ranking> rankingList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String userName);
    }

    public RankingAdapter(List<Ranking> rankingList, OnUserClickListener listener) {
        this.rankingList = rankingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Ranking ranking = rankingList.get(position);
        holder.tvRank.setText(String.valueOf(ranking.getRank()));
        holder.tvUserName.setText(ranking.getUserName());
        holder.tvScore.setText(String.valueOf(ranking.getScore()));
        
        if (ranking.isCurrentUser()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue highlight
            holder.tvUserName.setTypeface(null, Typeface.BOLD);
            holder.tvRank.setTypeface(null, Typeface.BOLD);
            holder.tvScore.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.tvUserName.setTypeface(null, Typeface.NORMAL);
            holder.tvRank.setTypeface(null, Typeface.NORMAL);
            holder.tvScore.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(ranking.getUserName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUserName, tvScore;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}