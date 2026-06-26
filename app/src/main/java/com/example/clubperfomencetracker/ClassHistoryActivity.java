package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassHistoryActivity extends AppCompatActivity {
    private static final String TAG = "ClassHistory";
    private RecyclerView rvClassHistory;
    private ClassAdapter adapter;
    private final List<ClassRecord> classList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private String userId;
    private ProgressBar progressBar;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);

        rvClassHistory = findViewById(R.id.rvClassHistory);
        rvClassHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classList);
        rvClassHistory.setAdapter(adapter);

        fetchClassHistory();
    }

    private void fetchClassHistory() {
        if (userId == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 1. Fetch classes added by Admin
        mDatabase.child("classes").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot classSnapshot) {
                final List<ClassInfo> availableClasses = new ArrayList<>();
                for (DataSnapshot snapshot : classSnapshot.getChildren()) {
                    ClassInfo info = snapshot.getValue(ClassInfo.class);
                    if (info != null) {
                        info.id = snapshot.getKey();
                        availableClasses.add(info);
                    }
                }

                // 2. Fetch Attendance node (attendance/{classId}/{userId})
                mDatabase.child("attendance").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {
                        classList.clear();
                        Collections.reverse(availableClasses); // Show newest first

                        for (ClassInfo info : availableClasses) {
                            String status = "Absent";
                            
                            // Check if this class has a record for this student
                            if (attendanceSnapshot.hasChild(info.id) && 
                                attendanceSnapshot.child(info.id).hasChild(userId)) {
                                Object val = attendanceSnapshot.child(info.id).child(userId).getValue();
                                status = (val != null) ? val.toString() : "Present";
                            }
                            
                            classList.add(new ClassRecord(info.topic, info.date + " • " + info.time, status));
                        }

                        adapter.notifyDataSetChanged();
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (classList.isEmpty() && tvNoData != null) tvNoData.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }
}
