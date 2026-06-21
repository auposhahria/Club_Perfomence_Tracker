package com.example.clubperfomencetracker;

public class User {
    public String name;
    public String email;
    public String cfHandle;
    public int totalClasses = 0;
    public int attendedClasses = 0;
    public int cfRating = 0;
    public int clubRating = 0;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String cfHandle) {
        this.name = name;
        this.email = email;
        this.cfHandle = cfHandle;
    }
}