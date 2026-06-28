package com.example.clubperfomencetracker;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContestOrgRankFetcher {

    private static final String TAG = "OrgRankFetcher";

    public interface Callback {
        void onSuccess(List<OrgRankResult> results);
        void onError(String message);
        default void onProgress(int current, int total, String message) {}
    }

    private final CodeforcesApiClient api;
    private final ExecutorService     executor;
    private final Handler             mainHandler;

    public ContestOrgRankFetcher() {
        this.api         = new CodeforcesApiClient();
        this.executor    = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Fetches contest standings for each KIU member one-by-one
     * Uses Rating Delta as the score.
     */
    public void fetchOrgLeaderboard(int contestId,
                                  String targetOrg,
                                  boolean unofficial,
                                  List<String> cachedHandles,
                                  Callback callback) {

        executor.execute(() -> {
            try {
                final String displayOrg = (targetOrg != null && !targetOrg.isEmpty()) 
                        ? targetOrg : "Kishoreganj University";

                List<String> orgMembers;
                if (cachedHandles != null && !cachedHandles.isEmpty()) {
                    orgMembers = cachedHandles;
                } else {
                    mainHandler.post(() -> callback.onProgress(0, 0, "Fetching member list..."));
                    orgMembers = api.fetchOrganizationMembers("47972");
                }

                if (orgMembers == null || orgMembers.isEmpty()) {
                    mainHandler.post(() -> callback.onError("No organization members found."));
                    return;
                }

                final int totalMembersCount = orgMembers.size();
                List<OrgRankResult> participants = new ArrayList<>();
                List<String> participatingHandles = new ArrayList<>();

                // Step 2: Query each user's rating changes one-by-one
                for (int i = 0; i < totalMembersCount; i++) {
                    String handle = orgMembers.get(i);
                    final int currentNum = i + 1;
                    mainHandler.post(() -> callback.onProgress(currentNum, totalMembersCount, "Checking " + handle));

                    try {
                        List<CfModels.RatingChange> ratings = api.fetchUserRating(handle);
                        CfModels.RatingChange match = null;
                        if (ratings != null) {
                            for (CfModels.RatingChange rc : ratings) {
                                if (rc.contestId == contestId) {
                                    match = rc;
                                    break;
                                }
                            }
                        }

                        if (match != null) {
                            int delta = match.newRating - match.oldRating;
                            participants.add(new OrgRankResult(
                                handle,
                                displayOrg,
                                0, // Org Rank (placeholder)
                                0, // Total (placeholder)
                                delta
                            ));
                            participatingHandles.add(handle.toLowerCase());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Rating fetch failed for " + handle + ": " + e.getMessage());
                    }

                    // Respect Codeforces API rate limit
                    if (i < totalMembersCount - 1) {
                        try { Thread.sleep(250); } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                // Sort by rating delta (descending)
                Collections.sort(participants, (a, b) -> Integer.compare(b.score, a.score));

                int finalOrgTotal = participants.size();
                List<OrgRankResult> finalResults = new ArrayList<>();
                for (int i = 0; i < finalOrgTotal; i++) {
                    OrgRankResult r = participants.get(i);
                    finalResults.add(new OrgRankResult(
                        r.handle, r.organization, i + 1, finalOrgTotal, r.score
                    ));
                }

                // Append non-participating members
                for (String handle : orgMembers) {
                    if (!participatingHandles.contains(handle.toLowerCase())) {
                        finalResults.add(new OrgRankResult(handle, displayOrg, -1, finalOrgTotal, 0));
                    }
                }

                mainHandler.post(() -> callback.onSuccess(finalResults));

            } catch (Exception e) {
                Log.e(TAG, "fetchOrgLeaderboard failed", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void fetchOrgLeaderboard(int contestId, String targetOrg, boolean unofficial, Callback callback) {
        fetchOrgLeaderboard(contestId, targetOrg, unofficial, null, callback);
    }

    public void fetchGlobalLeaderboard(String targetOrg, Callback callback) {
        executor.execute(() -> {
            try {
                final String displayOrg = (targetOrg != null && !targetOrg.isEmpty()) 
                        ? targetOrg : "Kishoreganj University";
                
                List<String> handles = api.fetchOrganizationMembers("47972");
                if (handles == null || handles.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
                    return;
                }
                
                List<CfModels.UserInfo> infoList = api.fetchUserInfo(handles);
                Collections.sort(infoList, (a, b) -> Integer.compare(b.rating, a.rating));

                int totalCountSize = infoList.size();
                List<OrgRankResult> results = new ArrayList<>();
                for (int i = 0; i < totalCountSize; i++) {
                    CfModels.UserInfo info = infoList.get(i);
                    results.add(new OrgRankResult(
                            info.handle, info.organization != null ? info.organization : displayOrg,
                            i + 1, totalCountSize, info.rating
                    ));
                }
                mainHandler.post(() -> callback.onSuccess(results));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
