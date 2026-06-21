package com.example.clubperfomencetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClassHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Class History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvClassHistory = findViewById(R.id.rvClassHistory);
        rvClassHistory.setLayoutManager(new LinearLayoutManager(this));

        List<ClassRecord> classList = new ArrayList<>();
        classList.add(new ClassRecord("Introduction to Graph Theory", "Oct 26, 2023", "Instructor A", true));
        classList.add(new ClassRecord("Dynamic Programming Basics", "Oct 24, 2023", "Instructor B", true));
        classList.add(new ClassRecord("Number Theory Contest", "Oct 20, 2023", "Instructor A", false));
        classList.add(new ClassRecord("Binary Search & Two Pointers", "Oct 18, 2023", "Instructor C", true));
        classList.add(new ClassRecord("Sorting Algorithms", "Oct 15, 2023", "Instructor B", true));

        ClassAdapter adapter = new ClassAdapter(classList);
        rvClassHistory.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}