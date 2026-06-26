package com.example.clubperfomencetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private OkHttpClient client;
    private TextView tvCfRating, tvCfRank, tvTotalClasses, tvAttendedClasses;
    private MaterialCardView cardCfRating;
    private String currentCfHandle = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        client = new OkHttpClient();

        tvTotalClasses = view.findViewById(R.id.tvTotalClasses);
        tvAttendedClasses = view.findViewById(R.id.tvAttendedClasses);
        tvCfRating = view.findViewById(R.id.tvCfRating);
        tvCfRank = view.findViewById(R.id.tvCfRank);
        TextView tvClubRating = view.findViewById(R.id.tvClubRating);
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvProgressStats = view.findViewById(R.id.tvProgressStats);
        CircularProgressIndicator progressLadder = view.findViewById(R.id.progressLadder);

        MaterialCardView cardTotal = view.findViewById(R.id.cardTotalClasses);
        MaterialCardView cardAttended = view.findViewById(R.id.cardAttendedClasses);
        cardCfRating = view.findViewById(R.id.cardCfRating);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Load Profile Data
            mDatabase.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentCfHandle = user.cfHandle;
                        tvWelcome.setText("Welcome, " + user.name);
                        tvClubRating.setText(String.valueOf(user.clubRating));
                        
                        if (user.cfHandle != null && !user.cfHandle.isEmpty()) {
                            fetchCodeforcesRating(user.cfHandle);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Calculate Dynamic Attendance Stats
            fetchAttendanceStats(currentUser.getUid());
        }

        // Ladder Progress Logic
        SharedPreferences ladderPrefs = getActivity().getSharedPreferences("ProblemLadderPrefs", Context.MODE_PRIVATE);
        String[] sampleProblems = {"Watermelon", "Way Too Long Words", "Team", "Next Round", "Bit++", 
                                  "Football", "Even Odds", "HQ9+", "Theatre Square", "Lucky Division", "Chat room"};
        int solvedCount = 0;
        for (String p : sampleProblems) {
            if (ladderPrefs.getBoolean(p, false)) solvedCount++;
        }
        
        if (progressLadder != null) {
            progressLadder.setMax(sampleProblems.length);
            progressLadder.setProgress(solvedCount);
        }
        if (tvProgressStats != null) {
            tvProgressStats.setText(solvedCount + "/" + sampleProblems.length + " Problems Solved");
        }

        View.OnClickListener goToHistory = v -> {
            startActivity(new Intent(getActivity(), ClassHistoryActivity.class));
        };

        if (cardTotal != null) cardTotal.setOnClickListener(goToHistory);
        if (cardAttended != null) cardAttended.setOnClickListener(goToHistory);
        
        if (cardCfRating != null) {
            cardCfRating.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CodeforcesContestActivity.class);
                intent.putExtra("CURRENT_HANDLE", currentCfHandle);
                startActivity(intent);
            });
        }

        View ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        if (ivProfileAvatar != null) {
            ivProfileAvatar.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), ProfileActivity.class));
            });
        }

        return view;
    }

    private void fetchAttendanceStats(String uid) {
        // Count total classes
        mDatabase.child("classes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalClasses.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Count attended classes
        mDatabase.child("attendance_user").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvAttendedClasses.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCodeforcesRating(String handle) {
        String url = "https://codeforces.com/api/user.info?handles=" + handle;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if ("OK".equals(jsonObject.getString("status"))) {
                        JSONObject userInfo = jsonObject.getJSONArray("result").getJSONObject(0);
                        int rating = userInfo.optInt("rating", 0);
                        String rank = userInfo.optString("rank", "Unrated");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> updateRatingUI(rating, rank));
                        }
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private void updateRatingUI(int rating, String rank) {
        tvCfRating.setText(rating == 0 ? "Unrated" : String.valueOf(rating));
        if (tvCfRank != null && rank != null) {
            tvCfRank.setText(rank.substring(0, 1).toUpperCase() + rank.substring(1));
        }
        int color = getCfColor(rating);
        tvCfRating.setTextColor(color);
        if (tvCfRank != null) tvCfRank.setTextColor(color);
        if (cardCfRating != null) cardCfRating.setStrokeColor(color);
    }

    private int getCfColor(int rating) {
        if (rating < 1200) return Color.GRAY;
        if (rating < 1400) return Color.parseColor("#008000");
        if (rating < 1600) return Color.parseColor("#03a89e");
        if (rating < 1900) return Color.BLUE;
        if (rating < 2100) return Color.parseColor("#aa00aa");
        if (rating < 2300) return Color.parseColor("#ff8c00");
        return Color.RED;
    }
}