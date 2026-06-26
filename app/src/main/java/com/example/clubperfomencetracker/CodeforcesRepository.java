package com.example.clubperfomencetracker;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CodeforcesRepository {
    private static final String TAG = "CodeforcesRepo";
    private final CodeforcesApiService apiService;
    private final RankingDatabaseHelper dbHelper;

    public CodeforcesRepository(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://codeforces.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(CodeforcesApiService.class);
        this.dbHelper = new RankingDatabaseHelper(context);
    }

    public interface OnStandingsLoadedListener {
        void onSuccess(List<Ranking> rankings);
        void onFailure(Throwable t);
    }

    /**
     * Efficiently fetches contest standings by matching against known KIU handles 
     * and performing auto-discovery on top participants.
     */
    public void fetchAndMatchStandings(int contestId, OnStandingsLoadedListener listener) {
        // Step 1: Load known handles from DB
        List<Ranking> knownKiuMembers = dbHelper.getAllRankings();
        Set<String> knownHandles = new HashSet<>();
        for (Ranking r : knownKiuMembers) knownHandles.add(r.getUserName().toLowerCase());

        // Step 2: Fetch top 5,000 participants (balanced for speed and coverage)
        apiService.getContestStandings(contestId, true, 1, 5000).enqueue(new Callback<CodeforcesResponse>() {
            @Override
            public void onResponse(Call<CodeforcesResponse> call, Response<CodeforcesResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !"OK".equals(response.body().getStatus())) {
                    listener.onFailure(new Exception("API Error"));
                    return;
                }

                List<RanklistRow> rows = response.body().getResult().getRows();
                List<Ranking> results = new ArrayList<>();
                List<String> unknownHandles = new ArrayList<>();

                int localRank = 1;
                for (RanklistRow row : rows) {
                    if (row.getParty().getMembers().isEmpty()) continue;
                    String handle = row.getParty().getMembers().get(0).getHandle();
                    
                    if (knownHandles.contains(handle.toLowerCase())) {
                        // Found a known member!
                        String title = "Participant";
                        for (Ranking m : knownKiuMembers) {
                            if (m.getUserName().equalsIgnoreCase(handle)) {
                                title = m.getRankTitle();
                                break;
                            }
                        }
                        results.add(new Ranking(localRank++, handle, (int)row.getPoints(), title, "2024", 1.0));
                    } else if (unknownHandles.size() < 200) {
                        // Collect unknown handles from top participants to check for "Discovery"
                        unknownHandles.add(handle);
                    }
                }

                // Show known results immediately
                listener.onSuccess(new ArrayList<>(results));

                // Step 3: Background Discovery for unknown handles
                if (!unknownHandles.isEmpty()) {
                    discoverNewMembers(unknownHandles, contestId, results, listener);
                }
            }

            @Override
            public void onFailure(Call<CodeforcesResponse> call, Throwable t) {
                listener.onFailure(t);
            }
        });
    }

    private void discoverNewMembers(List<String> handles, int contestId, List<Ranking> existingResults, OnStandingsLoadedListener listener) {
        String handlesParam = String.join(";", handles);
        apiService.getUserInfo(handlesParam).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && "OK".equals(response.body().getStatus())) {
                    boolean foundNew = false;
                    for (UserInfoResponse.User user : response.body().getResult()) {
                        String org = user.getOrganization();
                        if (org != null && (org.toLowerCase().contains("kishoreganj") || org.contains("47972"))) {
                            // NEW MEMBER DISCOVERED (e.g. Gaziaupo)
                            Ranking newRanking = new Ranking(0, user.getHandle(), user.getRating(), user.getRank(), "", 0);
                            dbHelper.addOrUpdateRanking(newRanking);
                            foundNew = true;
                        }
                    }
                    if (foundNew) {
                        // Refresh the leaderboard to include newly discovered members
                        fetchAndMatchStandings(contestId, listener);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Log.e(TAG, "Discovery failed", t);
            }
        });
    }
}