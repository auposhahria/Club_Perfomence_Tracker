package com.example.clubperfomencetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BlogFragment extends Fragment {
    private DatabaseReference mDatabase;
    private BlogAdapter adapter;
    private List<BlogPost> blogList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("blog_posts");
        
        RecyclerView rvBlog = view.findViewById(R.id.rvBlog);
        rvBlog.setLayoutManager(new LinearLayoutManager(getContext()));
        
        blogList = new ArrayList<>();
        adapter = new BlogAdapter(blogList);
        rvBlog.setAdapter(adapter);

        // Fetch posts from Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blogList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    BlogPost post = postSnapshot.getValue(BlogPost.class);
                    if (post != null) {
                        blogList.add(0, post); // Add new posts to the top
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load blogs.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CreatePostActivity.class));
        });

        return view;
    }
}