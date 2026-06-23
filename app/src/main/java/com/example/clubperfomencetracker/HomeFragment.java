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
    private TextView tvCfRating, tvCfRank;
    private MaterialCardView cardCfRating;
    private String currentCfHandle = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        client = new OkHttpClient();

        TextView tvTotalClasses = view.findViewById(R.id.tvTotalClasses);
        TextView tvAttendedClasses = view.findViewById(R.id.tvAttendedClasses);
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
            mDatabase.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        currentCfHandle = user.cfHandle;
                        tvWelcome.setText("Welcome, " + user.name + " (" + user.cfHandle + ")");
                        tvTotalClasses.setText(String.valueOf(user.totalClasses));
                        tvAttendedClasses.setText(String.valueOf(user.attendedClasses));
                        tvClubRating.setText(String.valueOf(user.clubRating));
                        
                        if (user.cfHandle != null && !user.cfHandle.isEmpty()) {
                            fetchCodeforcesRating(user.cfHandle);
                        } else {
                            tvCfRating.setText("N/A");
                            if (tvCfRank != null) tvCfRank.setText("No Handle");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

        return view;
    }

    private void fetchCodeforcesRating(String handle) {
        String url = "https://codeforces.com/api/user.info?handles=" + handle;
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvCfRating.setText("--");
                        if (tvCfRank != null) tvCfRank.setText("Offline");
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvCfRating.setText("--");
                            if (tvCfRank != null) tvCfRank.setText("Error");
                        });
                    }
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    
                    if (!"OK".equals(jsonObject.getString("status"))) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvCfRating.setText("--");
                                if (tvCfRank != null) tvCfRank.setText("N/A");
                            });
                        }
                        return;
                    }
                    
                    JSONArray result = jsonObject.getJSONArray("result");
                    if (result.length() > 0) {
                        JSONObject userInfo = result.getJSONObject(0);
                        int rating = userInfo.optInt("rating", 0);
                        String rank = userInfo.optString("rank", "Unrated");
                        
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> updateRatingUI(rating, rank));
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvCfRating.setText("--");
                                if (tvCfRank != null) tvCfRank.setText("Not Found");
                            });
                        }
                    }
                } catch (Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvCfRating.setText("--");
                            if (tvCfRank != null) tvCfRank.setText("Error");
                        });
                    }
                }
            }
        });
    }

    private void updateRatingUI(int rating, String rank) {
        if (rating == 0) {
            tvCfRating.setText("Unrated");
        } else {
            tvCfRating.setText(String.valueOf(rating));
        }
        
        if (tvCfRank != null && rank != null && !rank.isEmpty()) {
            tvCfRank.setText(rank.substring(0, 1).toUpperCase() + rank.substring(1));
        }
        
        int color;
        if (rating < 1200) {
            color = Color.parseColor("#808080"); // Newbie (Gray)
        } else if (rating < 1400) {
            color = Color.parseColor("#008000"); // Pupil (Green)
        } else if (rating < 1600) {
            color = Color.parseColor("#03a89e"); // Specialist (Cyan)
        } else if (rating < 1900) {
            color = Color.parseColor("#0000FF"); // Expert (Blue)
        } else if (rating < 2100) {
            color = Color.parseColor("#aa00aa"); // Candidate Master (Violet)
        } else if (rating < 2300) {
            color = Color.parseColor("#ff8c00"); // Master (Orange)
        } else {
            color = Color.parseColor("#FF0000"); // Grandmaster+ (Red)
        }
        
        tvCfRating.setTextColor(color);
        if (tvCfRank != null) tvCfRank.setTextColor(color);
        if (cardCfRating != null) {
            cardCfRating.setStrokeColor(color);
        }
    }
}