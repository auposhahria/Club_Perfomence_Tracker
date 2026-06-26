package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextInputEditText etCfHandle = findViewById(R.id.etProfileCfHandle);
        TextInputEditText etRfidTag = findViewById(R.id.etProfileRfidTag);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Load initial data from Firebase
            mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        tvName.setText(user.name);
                        tvEmail.setText(user.email);
                        etCfHandle.setText(user.cfHandle);
                        etRfidTag.setText(user.rfidTag);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });

            btnSave.setOnClickListener(v -> {
                String newHandle = etCfHandle.getText().toString().trim();
                String newRfid = etRfidTag.getText().toString().trim();

                Map<String, Object> updates = new HashMap<>();
                updates.put("cfHandle", newHandle);
                updates.put("rfidTag", newRfid);

                // Update multiple fields at once
                mDatabase.child("users").child(currentUser.getUid()).updateChildren(updates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            btnResetPassword.setOnClickListener(v -> {
                String email = currentUser.getEmail();
                if (email != null) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    String error = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                                    Toast.makeText(ProfileActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}