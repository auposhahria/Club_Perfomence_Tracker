package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CodeforcesContestActivity extends AppCompatActivity {
    private static final String TAG = "CF_Leaderboard";
    private RecyclerView rvCfRankings;
    private RankingAdapter adapter;
    private final List<Ranking> rankingList = new ArrayList<>();
    private OkHttpClient client;
    private String currentHandle;
    private RankingDatabaseHelper dbHelper;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvLastUpdated, tvStatus;
    
    private int scannedCount = 0;
    private int foundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codeforces_contest);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("KIU Leaderboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new RankingDatabaseHelper(this);
        currentHandle = getIntent().getStringExtra("CURRENT_HANDLE");
        if (currentHandle == null) currentHandle = "";

        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.rankingProgressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvCfRankings = findViewById(R.id.rvCfRankings);
        
        rvCfRankings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(rankingList, handle -> {
            Intent intent = new Intent(CodeforcesContestActivity.this, ProgrammerDetailActivity.class);
            intent.putExtra("CF_HANDLE", handle);
            startActivity(intent);
        });
        rvCfRankings.setAdapter(adapter);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        swipeRefresh.setOnRefreshListener(this::fetchOrganizationRankings);

        loadFromCache();

        if (rankingList.isEmpty()) {
            fetchOrganizationRankings();
        }
    }

    private void loadFromCache() {
        List<Ranking> cachedData = dbHelper.getAllRankings();
        String lastUpdated = dbHelper.getLastUpdated();

        if (lastUpdated != null) {
            long time = Long.parseLong(lastUpdated);
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            tvLastUpdated.setText("Last updated: " + relativeTime);
        }

        rankingList.clear();
        if (!cachedData.isEmpty()) {
            for (Ranking r : cachedData) {
                if (r.getUserName().equalsIgnoreCase(currentHandle)) {
                    r.setCurrentUser(true);
                }
                rankingList.add(r);
            }
        }
        adapter.notifyDataSetChanged();
        scrollToMe();
    }

    private void fetchOrganizationRankings() {
        scannedCount = 0;
        foundCount = 0;
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Connecting to Codeforces...");
        
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Request request = new Request.Builder()
                .url("https://codeforces.com/api/user.ratedList?activeOnly=true")
                .header("User-Agent", "ClubPerformanceTracker/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    tvStatus.setText("Connection Failed");
                    Toast.makeText(CodeforcesContestActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        hideLoading();
                        tvStatus.setText("Server Error");
                    });
                    return;
                }

                List<Ranking> liveList = new ArrayList<>();
                try (JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("result")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                Ranking r = readUserFromStream(reader);
                                scannedCount++;
                                if (r != null) {
                                    foundCount++;
                                    liveList.add(r);
                                    updateLiveUI(foundCount, scannedCount, liveList);
                                } else if (scannedCount % 5000 == 0) {
                                    updateLiveUI(foundCount, scannedCount, null);
                                }
                            }
                            reader.endArray();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    // Final sort and save
                    Collections.sort(liveList, (a, b) -> Integer.compare(b.getScore(), a.getScore()));
                    for (int i = 0; i < liveList.size(); i++) {
                        liveList.get(i).setRank(i + 1);
                    }
                    dbHelper.saveRankings(liveList);

                    runOnUiThread(() -> {
                        hideLoading();
                        loadFromCache();
                        tvStatus.setText("Sync Complete: " + foundCount + " members");
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Stream Error", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        tvStatus.setText("Update Incomplete");
                    });
                }
            }
        });
    }

    private void updateLiveUI(int found, int scanned, List<Ranking> currentFound) {
        runOnUiThread(() -> {
            tvStatus.setText("Scanned: " + scanned + " | Found: " + found);
            if (currentFound != null && !currentFound.isEmpty()) {
                // To keep it smooth, only update list every few members or at intervals
                if (found % 5 == 0 || scanned > 80000) {
                    rankingList.clear();
                    rankingList.addAll(currentFound);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private Ranking readUserFromStream(JsonReader reader) throws IOException {
        String handle = "";
        int rating = 0;
        boolean belongsToOrg = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (reader.peek() == JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "handle": handle = reader.nextString(); break;
                case "rating": rating = reader.nextInt(); break;
                case "organization":
                    String org = reader.nextString();
                    if (org.toLowerCase().contains("kiu") || org.contains("47972")) belongsToOrg = true;
                    break;
                default: reader.skipValue(); break;
            }
        }
        reader.endObject();
        return belongsToOrg ? new Ranking(0, handle, rating, "", 0) : null;
    }

    private void scrollToMe() {
        for (int i = 0; i < rankingList.size(); i++) {
            if (rankingList.get(i).isCurrentUser()) {
                rvCfRankings.scrollToPosition(i);
                break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}