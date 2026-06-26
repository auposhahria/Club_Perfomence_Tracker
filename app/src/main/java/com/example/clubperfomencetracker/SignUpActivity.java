package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private LoadingDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        loadingBar = new LoadingDialog(this);
        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("Please wait, we are registering your data...");

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etCfHandle = findViewById(R.id.etCfHandle);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String handle = etCfHandle.getText().toString().trim();
            String password = etPassword.getText().toString(); // Don't trim password

            if (name.isEmpty() || email.isEmpty() || handle.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            writeNewUser(user.getUid(), name, email, handle);
                        }
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void writeNewUser(String userId, String name, String email, String handle) {
        User user = new User(name, email, handle);
        mDatabase.child("users").child(userId).setValue(user)
            .addOnCompleteListener(task -> {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Success! Welcome " + name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Auth worked, but database failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}