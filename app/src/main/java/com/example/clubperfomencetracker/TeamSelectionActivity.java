package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TeamSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_selection);

        String eventName = getIntent().getStringExtra("EVENT_NAME");
        TextView tvContestName = findViewById(R.id.tvContestName);
        if (eventName != null) {
            tvContestName.setText(eventName);
        }

        RecyclerView rvTeamRankings = findViewById(R.id.rvTeamRankings);
        rvTeamRankings.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for Team Selection contest
        List<Ranking> rankings = new ArrayList<>();
        rankings.add(new Ranking(1, "Alice", 500, "2023-10-27", 1.0));
        rankings.add(new Ranking(2, "Bob", 450, "2023-10-27", 1.0));
        rankings.add(new Ranking(3, "Charlie", 400, "2023-10-27", 0.8));
        rankings.add(new Ranking(4, "Diana", 350, "2023-10-27", 0.8));

        TeamRankingAdapter adapter = new TeamRankingAdapter(rankings, userName -> {
            // Redirect to individual programmer page
            Intent intent = new Intent(TeamSelectionActivity.this, ProgrammerDetailActivity.class);
            intent.putExtra("NAME", userName);
            intent.putExtra("CF_ID", userName.toLowerCase()); 
            intent.putExtra("MAX_RATING", 1500); // Dummy
            startActivity(intent);
        });

        rvTeamRankings.setAdapter(adapter);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}