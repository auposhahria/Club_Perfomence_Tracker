package com.example.clubperfomencetracker;

public class ClassRecord {
    private String topic;
    private String date;
    private String status; // Present, Absent, Late

    public ClassRecord(String topic, String date, String status) {
        this.topic = topic;
        this.date = date;
        this.status = status;
    }

    public String getTopic() { return topic; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
}