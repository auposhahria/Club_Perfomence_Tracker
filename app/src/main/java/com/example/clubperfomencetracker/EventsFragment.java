package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        
        RecyclerView rvEvents = view.findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Event> events = new ArrayList<>();
        events.add(new Event("Team Selection Contest 1", "Team Selection"));
        events.add(new Event("Codeforces Round #900", "Codeforces"));
        
        EventAdapter adapter = new EventAdapter(events, event -> {
            if (event.getType().equals("Team Selection")) {
                Intent intent = new Intent(getActivity(), TeamSelectionActivity.class);
                intent.putExtra("EVENT_NAME", event.getName());
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), CodeforcesContestActivity.class);
                intent.putExtra("EVENT_NAME", event.getName());
                startActivity(intent);
            }
        });
        
        rvEvents.setAdapter(adapter);
        
        return view;
    }
}