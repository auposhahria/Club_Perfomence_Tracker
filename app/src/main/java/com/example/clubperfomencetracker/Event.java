package com.example.clubperfomencetracker;

public class Event {
    private String name;
    private String type; // e.g., "Team Selection", "Codeforces"
    private int contestId;
    private String date;
    private boolean isUpcoming;

    public Event(String name, String type) {
        this.name = name;
        this.type = type;
        this.isUpcoming = false;
    }

    public Event(String name, String type, int contestId, String date) {
        this.name = name;
        this.type = type;
        this.contestId = contestId;
        this.date = date;
        this.isUpcoming = false;
    }

    public Event(String name, String type, int contestId, String date, boolean isUpcoming) {
        this.name = name;
        this.type = type;
        this.contestId = contestId;
        this.date = date;
        this.isUpcoming = isUpcoming;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getContestId() { return contestId; }
    public String getDate() { return date; }
    public boolean isUpcoming() { return isUpcoming; }
}