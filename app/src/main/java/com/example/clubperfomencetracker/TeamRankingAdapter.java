package com.example.clubperfomencetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeamRankingAdapter extends RecyclerView.Adapter<TeamRankingAdapter.TeamRankingViewHolder> {

    private List<Ranking> rankingList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String userName);
    }

    public TeamRankingAdapter(List<Ranking> rankingList, OnUserClickListener listener) {
        this.rankingList = rankingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamRankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_ranking, parent, false);
        return new TeamRankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamRankingViewHolder holder, int position) {
        Ranking ranking = rankingList.get(position);
        holder.tvRank.setText(String.valueOf(ranking.getRank()));
        holder.tvUserName.setText(ranking.getUserName());
        holder.tvScore.setText(String.valueOf(ranking.getScore()));
        holder.tvDate.setText(ranking.getDate());
        holder.tvWeight.setText(String.valueOf(ranking.getWeight()));

        holder.tvUserName.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(ranking.getUserName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    static class TeamRankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUserName, tvScore, tvDate, tvWeight;

        public TeamRankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
        }
    }
}