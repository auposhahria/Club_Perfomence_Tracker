package com.example.clubperfomencetracker;

public class BlogPost {
    public String title;
    public String author;
    public String content;
    public String date;

    public BlogPost() {
        // Default constructor required for calls to DataSnapshot.getValue(BlogPost.class)
    }

    public BlogPost(String title, String author, String content, String date) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getDate() { return date; }
}