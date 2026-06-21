package com.example.clubperfomencetracker;

public class ClassRecord {
    private String topic;
    private String date;
    private String instructor;
    private boolean wasAttended;

    public ClassRecord(String topic, String date, String instructor, boolean wasAttended) {
        this.topic = topic;
        this.date = date;
        this.instructor = instructor;
        this.wasAttended = wasAttended;
    }

    public String getTopic() { return topic; }
    public String getDate() { return date; }
    public String getInstructor() { return instructor; }
    public boolean wasAttended() { return wasAttended; }
}