package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class AdminMainActivity extends AppCompatActivity {
    private RecyclerView rvAdminClasses;
    private AdminClassAdapter adapter;
    private List<ClassInfo> classList = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Portal");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        rvAdminClasses = findViewById(R.id.rvAdminClasses);
        rvAdminClasses.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminClassAdapter(classList, new AdminClassAdapter.OnClassActionListener() {
            @Override
            public void onEdit(ClassInfo classInfo) {
                Intent intent = new Intent(AdminMainActivity.this, AddClassActivity.class);
                intent.putExtra("CLASS_ID", classInfo.id);
                intent.putExtra("TOPIC", classInfo.topic);
                intent.putExtra("DATE", classInfo.date);
                intent.putExtra("TIME", classInfo.time);
                intent.putExtra("TIMESTAMP", classInfo.timestamp);
                startActivity(intent);
            }

            @Override
            public void onDelete(ClassInfo classInfo) {
                showDeleteConfirmation(classInfo);
            }
        });
        rvAdminClasses.setAdapter(adapter);

        Button btnAddClass = findViewById(R.id.btnAddClass);
        Button btnLogout = findViewById(R.id.btnLogoutAdmin);

        btnAddClass.setOnClickListener(v -> {
            startActivity(new Intent(AdminMainActivity.this, AddClassActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
            finish();
        });

        fetchClasses();
    }

    private void fetchClasses() {
        mDatabase.child("classes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ClassInfo info = postSnapshot.getValue(ClassInfo.class);
                    if (info != null) {
                        info.id = postSnapshot.getKey();
                        classList.add(info);
                    }
                }
                // Show newest classes at the top
                Collections.sort(classList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminMainActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(ClassInfo classInfo) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete '" + classInfo.topic + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child("classes").child(classInfo.id).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(AdminMainActivity.this, "Class deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(AdminMainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
