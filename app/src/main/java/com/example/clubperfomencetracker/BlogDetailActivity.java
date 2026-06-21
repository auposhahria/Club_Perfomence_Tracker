package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BlogDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

        String title = getIntent().getStringExtra("BLOG_TITLE");
        String author = getIntent().getStringExtra("BLOG_AUTHOR");
        String date = getIntent().getStringExtra("BLOG_DATE");
        String content = getIntent().getStringExtra("BLOG_CONTENT");

        TextView tvTitle = findViewById(R.id.tvFullTitle);
        TextView tvAuthorDate = findViewById(R.id.tvFullAuthorDate);
        TextView tvContent = findViewById(R.id.tvFullContent);

        tvTitle.setText(title);
        tvAuthorDate.setText("By " + author + " | " + date);
        tvContent.setText(content);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Read Blog");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}