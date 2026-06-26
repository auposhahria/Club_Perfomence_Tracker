package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private LoadingDialog loadingBar;
    private boolean isAttemptingAdminLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        loadingBar = new LoadingDialog(this);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            isAttemptingAdminLogin = false;
            performLogin(etEmail, etPassword);
        });

        btnAdminLogin.setOnClickListener(v -> {
            isAttemptingAdminLogin = true;
            performLogin(etEmail, etPassword);
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Reset email sent!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void performLogin(TextInputEditText etEmail, TextInputEditText etPassword) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Logging In");
        loadingBar.show();
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    checkUserRole();
                } else {
                    loadingBar.dismiss();
                    handleLoginError(task.getException());
                }
            });
    }

    private void checkUserRole() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    loadingBar.dismiss();
                    
                    // Directly check for the field to ensure accuracy
                    Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                    if (isAdmin == null) isAdmin = false;

                    if (isAttemptingAdminLogin && !isAdmin) {
                        // User tried to log in as admin but isn't one
                        FirebaseAuth.getInstance().signOut();
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Access Denied")
                                .setMessage("This account does not have Admin privileges. If you are an admin, ensure 'isAdmin: true' is set in the database.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else if (isAdmin) {
                        Toast.makeText(LoginActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleLoginError(Exception e) {
        String errorMessage;
        if (e instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid credentials.";
        } else {
            errorMessage = e != null ? e.getMessage() : "Authentication failed.";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Login Failed")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }
}
