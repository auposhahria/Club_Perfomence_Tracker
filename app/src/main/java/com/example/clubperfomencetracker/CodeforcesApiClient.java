package com.example.clubperfomencetracker;

import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CodeforcesApiClient {

    private static final String TAG = "CfApiClient";
    private static final String BASE_URL = "https://codeforces.com/api";
    private static final int    HANDLE_BATCH = 300; 

    private final OkHttpClient client;
    private final Gson gson;

    public CodeforcesApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson   = new Gson();
    }

    /**
     * Fetch all rating changes for a user.
     */
    public List<CfModels.RatingChange> fetchUserRating(String handle) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL + "/user.rating").newBuilder()
                .addQueryParameter("handle", handle)
                .build();

        CfModels.RatingChangeResponse resp = get(url, CfModels.RatingChangeResponse.class);
        if (resp != null && "OK".equals(resp.status) && resp.result != null) {
            return resp.result;
        }
        return new ArrayList<>();
    }

    /**
     * Fetch standings for the entire contest (paginated).
     * This is the most reliable way to find all participants without handle quirks.
     */
    public List<CfModels.RankRow> fetchAllStandings(int contestId, boolean unofficial) throws IOException {
        List<CfModels.RankRow> allRows = new ArrayList<>();
        int from = 1;
        int count = 5000;
        
        while (true) {
            HttpUrl url = HttpUrl.parse(BASE_URL + "/contest.standings").newBuilder()
                    .addQueryParameter("contestId", String.valueOf(contestId))
                    .addQueryParameter("from", String.valueOf(from))
                    .addQueryParameter("count", String.valueOf(count))
                    .addQueryParameter("showUnofficial", String.valueOf(unofficial))
                    .build();

            CfModels.StandingsResponse resp = get(url, CfModels.StandingsResponse.class);
            if (resp == null || !"OK".equals(resp.status) || resp.result == null || resp.result.rows == null || resp.result.rows.isEmpty()) {
                break;
            }
            
            allRows.addAll(resp.result.rows);
            
            if (resp.result.rows.size() < count) break; // End of list
            
            from += count;
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        return allRows;
    }

    /**
     * Fetch contest standings for a SINGLE handle.
     */
    public CfModels.RankRow fetchSingleHandleStandings(int contestId, String handle, boolean unofficial) {
        HttpUrl url = HttpUrl.parse(BASE_URL + "/contest.standings").newBuilder()
                .addQueryParameter("contestId", String.valueOf(contestId))
                .addQueryParameter("handles", handle)
                .addQueryParameter("showUnofficial", String.valueOf(unofficial))
                .build();

        try {
            CfModels.StandingsResponse resp = get(url, CfModels.StandingsResponse.class);
            if (resp != null && "OK".equals(resp.status) && resp.result != null
                    && resp.result.rows != null && !resp.result.rows.isEmpty()) {
                return resp.result.rows.get(0);
            }
        } catch (IOException e) {
            Log.w(TAG, "Standings fetch failed for " + handle + " in contest " + contestId + ": " + e.getMessage());
        }
        return null;
    }

    public List<CfModels.RankRow> fetchStandingsForHandles(int contestId, List<String> handles, boolean unofficial) 
            throws IOException {
        if (handles == null || handles.isEmpty()) return new ArrayList<>();
        
        List<CfModels.RankRow> allRows = new ArrayList<>();
        for (int i = 0; i < handles.size(); i += HANDLE_BATCH) {
            List<String> chunk = handles.subList(i, Math.min(i + HANDLE_BATCH, handles.size()));
            String joined = String.join(";", chunk);

            HttpUrl url = HttpUrl.parse(BASE_URL + "/contest.standings").newBuilder()
                    .addQueryParameter("contestId", String.valueOf(contestId))
                    .addQueryParameter("handles", joined)
                    .addQueryParameter("showUnofficial", String.valueOf(unofficial))
                    .build();

            try {
                CfModels.StandingsResponse resp = get(url, CfModels.StandingsResponse.class);
                if (resp != null && "OK".equals(resp.status) && resp.result != null && resp.result.rows != null) {
                    allRows.addAll(resp.result.rows);
                }
            } catch (IOException e) {
                Log.w(TAG, "Batch failed: " + e.getMessage());
            }
            
            if (i + HANDLE_BATCH < handles.size()) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) { 
                    Thread.currentThread().interrupt(); break; 
                }
            }
        }
        return allRows;
    }

    public List<CfModels.UserInfo> fetchUserInfo(List<String> handles) throws IOException, InterruptedException {
        List<CfModels.UserInfo> results = new ArrayList<>();
        for (int i = 0; i < handles.size(); i += HANDLE_BATCH) {
            List<String> batch = handles.subList(i, Math.min(i + HANDLE_BATCH, handles.size()));
            String joined = String.join(";", batch);

            HttpUrl url = HttpUrl.parse(BASE_URL + "/user.info").newBuilder()
                    .addQueryParameter("handles", joined)
                    .build();

            CfModels.UserInfoResponse resp = get(url, CfModels.UserInfoResponse.class);
            if (resp != null && "OK".equals(resp.status) && resp.result != null) {
                results.addAll(resp.result);
            }
            if (i + HANDLE_BATCH < handles.size()) Thread.sleep(300);
        }
        return results;
    }

    public List<String> fetchOrganizationMembers(String orgId) throws IOException {
        String url = "https://codeforces.com/ratings/organization/" + orgId;
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String html = response.body().string();
            
            List<String> handles = new ArrayList<>();
            Pattern tablePattern = Pattern.compile("<table[^>]*>(.*?)</table>", Pattern.DOTALL);
            Matcher matcher = tablePattern.matcher(html);
            while (matcher.find()) {
                String tableHtml = matcher.group(1);
                if (tableHtml.contains("Who")) {
                    Pattern profilePattern = Pattern.compile("/profile/([a-zA-Z0-9_\\-]+)");
                    Matcher profileMatcher = profilePattern.matcher(tableHtml);
                    Set<String> uniqueHandles = new LinkedHashSet<>();
                    while (profileMatcher.find()) {
                        uniqueHandles.add(profileMatcher.group(1));
                    }
                    handles.addAll(uniqueHandles);
                    break;
                }
            }
            return handles;
        }
    }

    private <T> T get(HttpUrl url, Class<T> clazz) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = "";
                try { if (response.body() != null) errorBody = response.body().string(); } catch (Exception ignored) {}
                throw new IOException("HTTP " + response.code() + " " + errorBody);
            }
            return gson.fromJson(response.body().charStream(), clazz);
        }
    }
}