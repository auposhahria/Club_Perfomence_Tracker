package com.example.clubperfomencetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class LadderFragment extends Fragment {
    
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProblemLadderPrefs";
    private ProblemAdapter adapter;
    private String currentDifficulty = "800";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ladder, container, false);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        RecyclerView rvLadder = view.findViewById(R.id.rvLadder);
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutLadder);
        SearchView searchView = view.findViewById(R.id.searchProblems);

        rvLadder.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Problem> allProblems = new ArrayList<>();
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

        return view;
    }

    private void addProblem(List<Problem> list, String name, String difficulty, String url) {
        boolean isSolved = sharedPreferences.getBoolean(name, false);
        list.add(new Problem(name, difficulty, isSolved, url));
    }

    private void updateSolvedCount() {
        // This could notify HomeFragment to refresh, or we can just let HomeFragment read from Prefs on start
        int solvedCount = 0;
        // In a real app, we'd iterate over all problems or keep a counter
    }
}