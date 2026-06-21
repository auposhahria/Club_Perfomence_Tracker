package com.example.clubperfomencetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private TextView tvDebugHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Logging In");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        tvDebugHelp = findViewById(R.id.tvDebugHelp);

        tvDebugHelp.setOnClickListener(v -> showDebugDialog());

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString(); // DO NOT TRIM PASSWORDS

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loadingBar.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
        });

        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void handleLoginError(Exception e) {
        String errorMessage;
        if (e instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email. Please Sign Up first.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Wrong password OR Firebase is blocking the connection (reCAPTCHA/SHA-1 issue).";
            tvDebugHelp.setVisibility(View.VISIBLE);
        } else {
            errorMessage = e != null ? e.getMessage() : "Unknown authentication error.";
            tvDebugHelp.setVisibility(View.VISIBLE);
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Login Problem")
                .setMessage(errorMessage + "\n\nIf you are sure the password is correct, tap the red help text at the bottom.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDebugDialog() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            StringBuilder sb = new StringBuilder();
            sb.append("Package Name: ").append(getPackageName()).append("\n\n");
            
            for (Signature signature : info.signatures) {
                // SHA-1
                MessageDigest md1 = MessageDigest.getInstance("SHA1");
                md1.update(signature.toByteArray());
                String sha1 = bytesToHex(md1.digest());
                sb.append("SHA-1:\n").append(sha1).append("\n\n");

                // SHA-256
                MessageDigest md26 = MessageDigest.getInstance("SHA256");
                md26.update(signature.toByteArray());
                String sha256 = bytesToHex(md26.digest());
                sb.append("SHA-256:\n").append(sha256);
            }
            
            new AlertDialog.Builder(this)
                    .setTitle("App Credentials")
                    .setMessage(sb.toString())
                    .setPositiveButton("Copy All", (dialog, id) -> {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("FirebaseInfo", sb.toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Close", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) sb.append(":");
        }
        return sb.toString();
    }
}