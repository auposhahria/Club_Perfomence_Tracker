package com.example.clubperfomencetracker;

import com.google.firebase.database.PropertyName;

public class User {
    public String name;
    public String email;
    public String cfHandle;
    public String rfidTag; 
    public int totalClasses = 0;
    public int attendedClasses = 0;
    public int cfRating = 0;
    public int clubRating = 0;
    
    @PropertyName("isAdmin")
    public boolean isAdmin = false;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String email, String cfHandle) {
        this.name = name;
        this.email = email;
        this.cfHandle = cfHandle;
        this.rfidTag = "";
        this.isAdmin = false;
    }
}
