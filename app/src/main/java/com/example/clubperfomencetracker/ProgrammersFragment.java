package com.example.clubperfomencetracker;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class ProgrammersFragment extends Fragment {
    
    private ProgrammerAdapter adapter;
    private List<Programmer> programmerList = new ArrayList<>();
    private RankingDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programmers, container, false);
        
        dbHelper = new RankingDatabaseHelper(getContext());
        RecyclerView rvProgrammers = view.findViewById(R.id.rvProgrammers);
        SearchView searchView = view.findViewById(R.id.searchView);
        
        rvProgrammers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadProgrammersFromDB();
        
        adapter = new ProgrammerAdapter(programmerList, programmer -> {
            Intent intent = new Intent(getActivity(), ProgrammerDetailActivity.class);
            intent.putExtra("NAME", programmer.getName());
            intent.putExtra("CF_HANDLE", programmer.getCfId());
            intent.putExtra("MAX_RATING", programmer.getMaxRating());
            startActivity(intent);
        });
        
        rvProgrammers.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        
        return view;
    }

    private void loadProgrammersFromDB() {
        List<Ranking> rankings = dbHelper.getAllRankings();
        programmerList.clear();
        for (Ranking r : rankings) {
            programmerList.add(new Programmer(r.getUserName(), r.getUserName(), r.getScore()));
        }
    }
}