package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeforcesContestActivity extends AppCompatActivity {
    private static final String TAG = "CF_Leaderboard";
    private RecyclerView rvCfRankings;
    private RankingAdapter adapter;
    private final List<Ranking> rankingList = new ArrayList<>();
    private String currentHandle;
    private RankingDatabaseHelper dbHelper;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvLastUpdated, tvStatus, tvContestName;
    private View layoutStatusBar;
    private ImageView ivBack, ivRefresh;
    private FloatingActionButton fabScrollToMe;
    
    private int contestId = -1;
    private ContestOrgRankFetcher fetcher;
    private static final String TARGET_ORG = "Kishoreganj University";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codeforces_contest);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new RankingDatabaseHelper(this);
        currentHandle = getIntent().getStringExtra("CURRENT_HANDLE");
        if (currentHandle == null) currentHandle = "";
        
        contestId = getIntent().getIntExtra("CONTEST_ID", -1);
        String eventName = getIntent().getStringExtra("EVENT_NAME");

        tvContestName = findViewById(R.id.tvContestName);
        if (eventName != null) {
            tvContestName.setText(eventName);
        } else if (contestId == -1) {
            tvContestName.setText("KIU Global Leaderboard");
        } else {
            tvContestName.setText("Contest Leaderboard");
        }

        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvStatus = findViewById(R.id.tvStatus);
        layoutStatusBar = findViewById(R.id.layoutStatusBar);
        progressBar = findViewById(R.id.rankingProgressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvCfRankings = findViewById(R.id.rvCfRankings);
        ivBack = findViewById(R.id.ivBack);
        ivRefresh = findViewById(R.id.ivRefresh);
        fabScrollToMe = findViewById(R.id.fabScrollToMe);

        TextView tvHeaderScore = findViewById(R.id.tvHeaderScore);
        if (tvHeaderScore != null) {
            if (contestId == -1) {
                tvHeaderScore.setText("Rating");
            } else {
                tvHeaderScore.setText("Rating ∆");
            }
        }
        
        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> refreshData());
        fabScrollToMe.setOnClickListener(v -> scrollToMe());

        rvCfRankings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(rankingList, handle -> {
            Intent intent = new Intent(CodeforcesContestActivity.this, ProgrammerDetailActivity.class);
            intent.putExtra("CF_HANDLE", handle);
            intent.putExtra("NAME", handle);
            startActivity(intent);
        });
        rvCfRankings.setAdapter(adapter);

        fetcher = new ContestOrgRankFetcher();

        swipeRefresh.setOnRefreshListener(this::refreshData);

        if (contestId == -1) {
            loadFromCache();
        } else {
            refreshData();
        }
    }

    private void refreshData() {
        if (contestId == -1) {
            fetchGlobalData();
        } else {
            fetchContestData();
        }
    }

    private void fetchGlobalData() {
        layoutStatusBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Refreshing Global Stats...");
        progressBar.setIndeterminate(true);
        swipeRefresh.setRefreshing(true);

        fetcher.fetchGlobalLeaderboard(TARGET_ORG, new ContestOrgRankFetcher.Callback() {
            @Override
            public void onSuccess(List<OrgRankResult> results) {
                runOnUiThread(() -> {
                    layoutStatusBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    rankingList.clear();
                    
                    List<Ranking> toSave = new ArrayList<>();
                    for (OrgRankResult res : results) {
                        Ranking r = new Ranking(res.orgRank, res.handle, res.score, "Member", "", 0);
                        if (res.handle.equalsIgnoreCase(currentHandle)) {
                            r.setCurrentUser(true);
                        }
                        rankingList.add(r);
                        toSave.add(r);
                    }
                    
                    dbHelper.saveRankings(toSave);
                    adapter.notifyDataSetChanged();
                    updateLastUpdatedTime();
                    
                    if (currentHandle != null && !currentHandle.isEmpty()) {
                        fabScrollToMe.show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    layoutStatusBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(CodeforcesContestActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchContestData() {
        layoutStatusBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading member list...");
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        swipeRefresh.setRefreshing(true);

        // Get cached handles from the database (populated by KIU Global Leaderboard)
        List<Ranking> cachedData = dbHelper.getAllRankings();
        List<String> cachedHandles = new ArrayList<>();
        final java.util.Map<String, String> rankTitleMap = new java.util.HashMap<>();
        for (Ranking cached : cachedData) {
            cachedHandles.add(cached.getUserName());
            rankTitleMap.put(cached.getUserName().toLowerCase(), cached.getRankTitle());
        }

        fetcher.fetchOrgLeaderboard(contestId, TARGET_ORG, true, cachedHandles, new ContestOrgRankFetcher.Callback() {
            @Override
            public void onSuccess(List<OrgRankResult> results) {
                runOnUiThread(() -> {
                    layoutStatusBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    rankingList.clear();
                    
                    for (OrgRankResult res : results) {
                        String title = rankTitleMap.getOrDefault(res.handle.toLowerCase(), "Participant");
                        Ranking r = new Ranking(res.orgRank, res.handle, res.score, title, "", 0);
                        if (res.handle.equalsIgnoreCase(currentHandle)) {
                            r.setCurrentUser(true);
                        }
                        rankingList.add(r);
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    // Count participants (rank > 0)
                    int participantCount = 0;
                    for (OrgRankResult res : results) {
                        if (res.orgRank > 0) participantCount++;
                    }
                    
                    if (participantCount == 0) {
                        Toast.makeText(CodeforcesContestActivity.this, "No KIU students found in this contest", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CodeforcesContestActivity.this, participantCount + " KIU students participated!", Toast.LENGTH_SHORT).show();
                    }
                    
                    updateLastUpdatedTime();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    layoutStatusBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(CodeforcesContestActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int current, int total, String currentHandle) {
                runOnUiThread(() -> {
                    if (total > 0) {
                        tvStatus.setText("Checking " + currentHandle + " (" + current + "/" + total + ")");
                        progressBar.setMax(total);
                        progressBar.setProgress(current);
                    } else {
                        tvStatus.setText(currentHandle);
                    }
                });
            }
        });
    }

    private void loadFromCache() {
        List<Ranking> cachedData = dbHelper.getAllRankings();
        String lastUpdated = dbHelper.getLastUpdated();

        if (lastUpdated != null) {
            try {
                long time = Long.parseLong(lastUpdated);
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                tvLastUpdated.setText("Last updated: " + relativeTime);
            } catch (Exception ignored) {}
        }

        rankingList.clear();
        boolean handleFound = false;
        if (!cachedData.isEmpty()) {
            for (Ranking r : cachedData) {
                if (r.getUserName().equalsIgnoreCase(currentHandle)) {
                    r.setCurrentUser(true);
                    handleFound = true;
                }
                rankingList.add(r);
            }
        }
        adapter.notifyDataSetChanged();
        
        if (handleFound) {
            fabScrollToMe.show();
        } else {
            fabScrollToMe.hide();
        }
    }

    private void updateLastUpdatedTime() {
        dbHelper.updateLastUpdated();
        String lastUpdated = dbHelper.getLastUpdated();
        if (lastUpdated != null) {
            long time = Long.parseLong(lastUpdated);
            tvLastUpdated.setText("Last updated: " + DateUtils.getRelativeTimeSpanString(time));
        }
    }

    private void scrollToMe() {
        for (int i = 0; i < rankingList.size(); i++) {
            if (rankingList.get(i).getUserName().equalsIgnoreCase(currentHandle)) {
                rvCfRankings.smoothScrollToPosition(i);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetcher != null) {
            fetcher.shutdown();
        }
    }
}