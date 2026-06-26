package com.example.clubperfomencetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class LadderFragment extends Fragment {
    
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProblemLadderPrefs";
    private ProblemAdapter adapter;
    private String currentDifficulty = "800";
    private String currentQuery = "";
    private List<Problem> allProblems = new ArrayList<>();
    private LoadingDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ladder, container, false);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        RecyclerView rvLadder = view.findViewById(R.id.rvLadder);
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutLadder);
        SearchView searchView = view.findViewById(R.id.searchProblems);
        MaterialButton btnSyncCodeforces = view.findViewById(R.id.btnSyncCodeforces);

        rvLadder.setLayoutManager(new LinearLayoutManager(getContext()));

        allProblems.clear();
        // 800
        addProblem(allProblems, "Watermelon", "800", "https://codeforces.com/problemset/problem/4/A");
        addProblem(allProblems, "Way Too Long Words", "800", "https://codeforces.com/problemset/problem/71/A");
        addProblem(allProblems, "Team", "800", "https://codeforces.com/problemset/problem/231/A");
        addProblem(allProblems, "Next Round", "800", "https://codeforces.com/problemset/problem/158/A");
        addProblem(allProblems, "Bit++", "800", "https://codeforces.com/problemset/problem/282/A");
        // 900
        addProblem(allProblems, "Football", "900", "https://codeforces.com/problemset/problem/96/A");
        addProblem(allProblems, "Even Odds", "900", "https://codeforces.com/problemset/problem/318/A");
        addProblem(allProblems, "HQ9+", "900", "https://codeforces.com/problemset/problem/133/A");
        // 1000
        addProblem(allProblems, "Theatre Square", "1000", "https://codeforces.com/problemset/problem/1/A");
        addProblem(allProblems, "Lucky Division", "1000", "https://codeforces.com/problemset/problem/122/A");
        addProblem(allProblems, "Chat room", "1000", "https://codeforces.com/problemset/problem/58/A");

        adapter = new ProblemAdapter(allProblems, (problem, isSolved) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(problem.getName(), isSolved);
            editor.apply();
            updateSolvedCount();
        });
        
        rvLadder.setAdapter(adapter);
        adapter.filter("", "800");

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentDifficulty = tab.getText().toString();
                adapter.filter(currentQuery, currentDifficulty);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                adapter.filter(currentQuery, currentDifficulty);
                return true;
            }
        });

        if (btnSyncCodeforces != null) {
            btnSyncCodeforces.setOnClickListener(v -> syncSolvedProblems());
        }

        return view;
    }

    private void addProblem(List<Problem> list, String name, String difficulty, String url) {
        boolean isSolved = sharedPreferences.getBoolean(name, false);
        list.add(new Problem(name, difficulty, isSolved, url));
    }

    private void updateSolvedCount() {
        // This could notify HomeFragment to refresh, or we can just let HomeFragment read from Prefs on start
    }

    private void syncSolvedProblems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressDialog == null) {
            progressDialog = new LoadingDialog(getContext());
        }
        progressDialog.setTitle("Syncing");
        progressDialog.setMessage("Loading user profile...");
        progressDialog.show();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User dbUser = snapshot.getValue(User.class);
                if (dbUser != null && dbUser.cfHandle != null && !dbUser.cfHandle.trim().isEmpty()) {
                    fetchUserStatusFromCodeforces(dbUser.cfHandle.trim());
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Please configure your Codeforces handle in your Profile first", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserStatusFromCodeforces(String handle) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> progressDialog.setMessage("Fetching submissions for " + handle + "..."));

        OkHttpClient client = new OkHttpClient();
        String url = "https://codeforces.com/api/user.status?handle=" + handle + "&from=1&count=1000";
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to connect to Codeforces: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;
                
                if (!response.isSuccessful() || response.body() == null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Codeforces API error. Please try again later.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    if (!"OK".equals(obj.optString("status"))) {
                        getActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed: " + obj.optString("comment", "API Error"), Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    JSONArray results = obj.getJSONArray("result");
                    Set<String> solvedProblemNames = new HashSet<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject sub = results.getJSONObject(i);
                        if ("OK".equals(sub.optString("verdict"))) {
                            JSONObject problem = sub.getJSONObject("problem");
                            String problemName = problem.optString("name", "");
                            if (!problemName.isEmpty()) {
                                solvedProblemNames.add(problemName.toLowerCase().trim());
                            }
                        }
                    }

                    // Compare and update SharedPreferences & problems list
                    int newlySolved = 0;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (Problem p : allProblems) {
                        if (solvedProblemNames.contains(p.getName().toLowerCase().trim())) {
                            if (!p.isSolved()) {
                                editor.putBoolean(p.getName(), true);
                                p.setSolved(true);
                                newlySolved++;
                            }
                        }
                    }
                    editor.apply();

                    final int count = newlySolved;
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        adapter.filter(currentQuery, currentDifficulty);
                        Toast.makeText(getContext(), "Sync complete! Marked " + count + " new problems as solved.", Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}