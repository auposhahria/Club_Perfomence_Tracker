package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventsFragment extends Fragment {
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private final List<Event> events = new ArrayList<>();
    private OkHttpClient client;
    private ProgressBar progressBar;
    private String currentCfHandle = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        
        rvEvents = view.findViewById(R.id.rvEvents);
        progressBar = view.findViewById(R.id.progressBarEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new EventAdapter(events, event -> {
            if (event.isUpcoming()) {
                Toast.makeText(getContext(), "This contest has not started yet!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (event.getType().equals("Team Selection")) {
                Intent intent = new Intent(getActivity(), TeamSelectionActivity.class);
                intent.putExtra("EVENT_NAME", event.getName());
                intent.putExtra("CONTEST_ID", event.getContestId() == 0 ? -1 : event.getContestId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), CodeforcesContestActivity.class);
                intent.putExtra("EVENT_NAME", event.getName());
                intent.putExtra("CONTEST_ID", event.getContestId());
                intent.putExtra("CURRENT_HANDLE", currentCfHandle);
                startActivity(intent);
            }
        });
        
        rvEvents.setAdapter(adapter);
        client = new OkHttpClient();
        
        // Fetch current user's CF handle from Firebase
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUser.getUid()).child("cfHandle")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        String handle = snapshot.getValue(String.class);
                        if (handle != null) currentCfHandle = handle;
                    }
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
        }
        
        fetchCodeforcesContests();
        
        return view;
    }

    private void fetchCodeforcesContests() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        Request request = new Request.Builder()
                .url("https://codeforces.com/api/contest.list?gym=false")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to fetch contests", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                
                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    if (!"OK".equals(jsonObject.getString("status"))) return;
                    
                    JSONArray result = jsonObject.getJSONArray("result");
                    List<Event> fetchedEvents = new ArrayList<>();
                    
                    // Add Team Selection contests (manually for now or from DB if available)
                    fetchedEvents.add(new Event("Team Selection Contest 2024", "Team Selection", 0, "Ongoing", false));
                    
                    long oneYearAgo = (System.currentTimeMillis() / 1000L) - (12 * 30 * 24 * 60 * 60L);
                    
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject contest = result.getJSONObject(i);
                        long startTimeSeconds = contest.optLong("startTimeSeconds", 0);
                        String phase = contest.getString("phase");
                        
                        // Show finished contests from last 1 year OR upcoming contests
                        if (startTimeSeconds > oneYearAgo || phase.equals("BEFORE")) {
                            String name = contest.getString("name");
                            int id = contest.getInt("id");
                            String dateStr = "";
                            if (startTimeSeconds > 0) {
                                Date date = new Date(startTimeSeconds * 1000);
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                                dateStr = sdf.format(date);
                            } else {
                                dateStr = "TBD";
                            }
                            
                            boolean isUpcoming = "BEFORE".equals(phase);
                            fetchedEvents.add(new Event(name, "Codeforces", id, dateStr, isUpcoming));
                        }
                        
                        if (fetchedEvents.size() > 50) break; // Limit for UI performance
                    }
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            events.clear();
                            events.addAll(fetchedEvents);
                            adapter.notifyDataSetChanged();
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                        });
                    }
                } catch (Exception e) {
                    Log.e("EventsFragment", "Error parsing contests", e);
                }
            }
        });
    }
}