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
    private List<Programmer> programmers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programmers, container, false);
        
        RecyclerView rvProgrammers = view.findViewById(R.id.rvProgrammers);
        SearchView searchView = view.findViewById(R.id.searchView);
        
        rvProgrammers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        programmers = new ArrayList<>();
        programmers.add(new Programmer("John Doe", "johndoe123", 1500));
        programmers.add(new Programmer("Jane Smith", "jsmith_pro", 2100));
        programmers.add(new Programmer("Alice Johnson", "alice_j", 1800));
        programmers.add(new Programmer("Bob Brown", "bbrown", 1200));
        
        adapter = new ProgrammerAdapter(programmers, programmer -> {
            Intent intent = new Intent(getActivity(), ProgrammerDetailActivity.class);
            intent.putExtra("NAME", programmer.getName());
            intent.putExtra("CF_ID", programmer.getCfId());
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
}