package com.example.clubperfomencetracker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
        
        holder.tvRank.setText(ranking.getRank() <= 0 ? "-" : String.valueOf(ranking.getRank()));
        holder.tvUserName.setText(ranking.getUserName());
        
        // Show rating delta with + sign if positive
        if (ranking.getScore() == 0) {
            holder.tvScore.setText("0");
            holder.tvScore.setTextColor(Color.GRAY);
        } else {
            String scoreStr = (ranking.getScore() > 0 ? "+" : "") + ranking.getScore();
            holder.tvScore.setText(scoreStr);
            holder.tvScore.setTextColor(ranking.getScore() > 0 ? Color.parseColor("#0D6759") : Color.RED);
        }
        
        holder.tvUserRank.setText(ranking.getRank() <= 0 ? "Absent" : ranking.getRankTitle());

        // Set Rank Title Color
        holder.tvUserRank.setTextColor(getRankColor(ranking.getRankTitle()));

        // Hide Global Rank and Solved count as they are no longer required for this view
        if (holder.tvGlobalRank != null) holder.tvGlobalRank.setVisibility(View.GONE);
        if (holder.tvSolved != null) holder.tvSolved.setVisibility(View.GONE);

        // Style for Rank 1
        if (ranking.getRank() == 1) {
            holder.flRankBadge.setBackgroundResource(R.drawable.bg_avatar_circle); 
            holder.tvRank.setVisibility(View.GONE);
            holder.ivCrown.setVisibility(View.VISIBLE);
        } else {
            holder.flRankBadge.setBackgroundResource(R.drawable.bg_rank_badge); 
            holder.tvRank.setVisibility(View.VISIBLE);
            holder.ivCrown.setVisibility(View.GONE);
        }

        // Highlight current user
        if (ranking.isCurrentUser()) {
            holder.itemView.setAlpha(0.85f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(ranking.getUserName());
            }
        });
    }

    private int getRankColor(String title) {
        if (title == null) return Color.parseColor("#5DCAA5");
        switch (title) {
            case "Legendary Grandmaster":
            case "International Grandmaster":
            case "Grandmaster":
                return Color.parseColor("#FF0000"); // Red
            case "International Master":
            case "Master":
                return Color.parseColor("#FF8C00"); // Orange
            case "Candidate Master":
                return Color.parseColor("#AA00AA"); // Violet
            case "Expert":
                return Color.parseColor("#0000FF"); // Blue
            case "Specialist":
                return Color.parseColor("#03A89E"); // Cyan
            case "Pupil":
                return Color.parseColor("#008000"); // Green
            case "Newbie":
                return Color.parseColor("#808080"); // Gray
            default:
                return Color.parseColor("#5DCAA5"); // Default teal
        }
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUserName, tvScore, tvUserRank, tvGlobalRank, tvSolved;
        FrameLayout flRankBadge;
        ImageView ivCrown;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvUserRank = itemView.findViewById(R.id.tvUserRank);
            tvGlobalRank = itemView.findViewById(R.id.tvGlobalRank);
            tvSolved = itemView.findViewById(R.id.tvSolved);
            flRankBadge = itemView.findViewById(R.id.flRankBadge);
            ivCrown = itemView.findViewById(R.id.ivCrown);
        }
    }
}
