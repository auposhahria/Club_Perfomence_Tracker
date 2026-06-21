package com.example.clubperfomencetracker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<BlogPost> blogList;

    public BlogAdapter(List<BlogPost> blogList) {
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogPost post = blogList.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvAuthorDate.setText("By " + post.getAuthor() + " | " + post.getDate());
        holder.tvPreview.setText(post.getContent());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BlogDetailActivity.class);
            intent.putExtra("BLOG_TITLE", post.getTitle());
            intent.putExtra("BLOG_AUTHOR", post.getAuthor());
            intent.putExtra("BLOG_DATE", post.getDate());
            intent.putExtra("BLOG_CONTENT", post.getContent());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthorDate, tvPreview;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBlogTitle);
            tvAuthorDate = itemView.findViewById(R.id.tvBlogAuthorDate);
            tvPreview = itemView.findViewById(R.id.tvBlogPreview);
        }
    }
}