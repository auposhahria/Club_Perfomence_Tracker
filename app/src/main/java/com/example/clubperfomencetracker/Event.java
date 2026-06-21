package com.example.clubperfomencetracker;

public class Event {
    private String name;
    private String type; // e.g., "Team Selection", "Codeforces"

    public Event(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}