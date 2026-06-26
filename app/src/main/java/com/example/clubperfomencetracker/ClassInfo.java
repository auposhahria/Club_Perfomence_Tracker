package com.example.clubperfomencetracker;

public class ClassInfo {
    public String id;
    public String topic;
    public String date;
    public String time;
    public long timestamp;

    public ClassInfo() {
        // Required for Firebase
    }

    public ClassInfo(String id, String topic, String date, String time, long timestamp) {
        this.id = id;
        this.topic = topic;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
    }
}
