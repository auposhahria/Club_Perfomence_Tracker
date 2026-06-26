package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectionActivity extends AppCompatActivity {
    private RecyclerView rvTeamRankings;
    private TeamRankingAdapter adapter;
    private final List<Ranking> rankingList = new ArrayList<>();
    private RankingDatabaseHelper dbHelper;
    private ContestOrgRankFetcher fetcher;
    private int contestId = -1;
    private ProgressBar progressBar;
    private static final String TARGET_ORG = "Kishoreganj University of Science and Technology";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_selection);

        dbHelper = new RankingDatabaseHelper(this);
        fetcher = new ContestOrgRankFetcher();

        contestId = getIntent().getIntExtra("CONTEST_ID", -1);
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        
        TextView tvContestName = findViewById(R.id.tvContestName);
        if (eventName != null) {
            tvContestName.setText(eventName);
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        rvTeamRankings = findViewById(R.id.rvTeamRankings);
        rvTeamRankings.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeamRankingAdapter(rankingList, userName -> {
            Intent intent = new Intent(TeamSelectionActivity.this, ProgrammerDetailActivity.class);
            intent.putExtra("CF_HANDLE", userName);
            startActivity(intent);
        });
        rvTeamRankings.setAdapter(adapter);

        progressBar = findViewById(R.id.rankingProgressBar);

        if (contestId != -1) {
            fetchRealContestData();
        } else {
            loadDummyData();
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadDummyData() {
        rankingList.clear();
        rankingList.add(new Ranking(1, "Alice", 500, "Participant", "Oct 27, 2023", 1.0));
        rankingList.add(new Ranking(2, "Bob", 450, "Participant", "Oct 27, 2023", 1.0));
        rankingList.add(new Ranking(3, "Charlie", 400, "Participant", "Oct 27, 2023", 0.8));
        rankingList.add(new Ranking(4, "Diana", 350, "Participant", "Oct 27, 2023", 0.8));
        adapter.notifyDataSetChanged();
    }

    private void fetchRealContestData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        fetcher.fetchOrgLeaderboard(contestId, TARGET_ORG, true, new ContestOrgRankFetcher.Callback() {
            @Override
            public void onSuccess(List<OrgRankResult> results) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    rankingList.clear();
                    
                    // Create a map from handle to rank title from local DB
                    List<Ranking> cachedData = dbHelper.getAllRankings();
                    java.util.Map<String, String> rankTitleMap = new java.util.HashMap<>();
                    for (Ranking cached : cachedData) {
                        rankTitleMap.put(cached.getUserName().toLowerCase(), cached.getRankTitle());
                    }
                    
                    for (OrgRankResult res : results) {
                        String title = rankTitleMap.getOrDefault(res.handle.toLowerCase(), "Participant");
                        // Using the constructor: Ranking(rank, userName, score, rankTitle, date, weight)
                        rankingList.add(new Ranking(res.orgRank, res.handle, res.score, title, "2024", 1.0));
                        
                        // Also update/sync discovered members into local DB
                        dbHelper.addOrUpdateRanking(new Ranking(0, res.handle, res.score, title, "", 0));
                    }
                    
                    adapter.notifyDataSetChanged();
                    if (rankingList.isEmpty()) {
                        Toast.makeText(TeamSelectionActivity.this, "No students from " + TARGET_ORG + " found.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(TeamSelectionActivity.this, "Fetch Error: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetcher != null) {
            fetcher.shutdown();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}