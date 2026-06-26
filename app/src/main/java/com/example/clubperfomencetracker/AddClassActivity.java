package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddClassActivity extends AppCompatActivity {
    private TextInputEditText etTopic, etDate, etTime;
    private DatabaseReference mDatabase;
    private String existingClassId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        TextView tvTitle = findViewById(android.R.id.text1); // Using default if needed or specific ID
        etTopic = findViewById(R.id.etClassTopic);
        etDate = findViewById(R.id.etClassDate);
        etTime = findViewById(R.id.etClassTime);
        Button btnSave = findViewById(R.id.btnSaveClass);

        // Check if we are in Edit Mode
        existingClassId = getIntent().getStringExtra("CLASS_ID");
        if (existingClassId != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Update Class");
            }
            etTopic.setText(getIntent().getStringExtra("TOPIC"));
            etDate.setText(getIntent().getStringExtra("DATE"));
            etTime.setText(getIntent().getStringExtra("TIME"));
            btnSave.setText("Update Class");
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Class");
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSave.setOnClickListener(v -> saveClass());
    }

    private void saveClass() {
        String topic = etTopic.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (TextUtils.isEmpty(topic) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String classId = (existingClassId != null) ? existingClassId : mDatabase.child("classes").push().getKey();
        if (classId == null) return;

        long timestamp = (existingClassId != null) ? getIntent().getLongExtra("TIMESTAMP", System.currentTimeMillis()) : System.currentTimeMillis();
        ClassInfo classInfo = new ClassInfo(classId, topic, date, time, timestamp);

        mDatabase.child("classes").child(classId).setValue(classInfo)
                .addOnSuccessListener(aVoid -> {
                    String msg = (existingClassId != null) ? "Class updated!" : "Class added!";
                    Toast.makeText(AddClassActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddClassActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
