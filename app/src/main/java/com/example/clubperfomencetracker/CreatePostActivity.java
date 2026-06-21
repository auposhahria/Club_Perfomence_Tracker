package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Post");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextInputEditText etTitle = findViewById(R.id.etPostTitle);
        TextInputEditText etContent = findViewById(R.id.etPostContent);
        Button btnPublish = findViewById(R.id.btnPublish);

        btnPublish.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String author = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Anonymous";
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

            BlogPost newPost = new BlogPost(title, author, content, date);
            String postId = mDatabase.child("blog_posts").push().getKey();
            
            if (postId != null) {
                mDatabase.child("blog_posts").child(postId).setValue(newPost)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Post Published!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Failed to publish post: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}