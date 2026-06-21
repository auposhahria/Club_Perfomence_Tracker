package com.example.clubperfomencetracker;

public class Programmer {
    private String name;
    private String cfId;
    private int maxRating;

    public Programmer(String name, String cfId, int maxRating) {
        this.name = name;
        this.cfId = cfId;
        this.maxRating = maxRating;
    }

    public String getName() { return name; }
    public String getCfId() { return cfId; }
    public int getMaxRating() { return maxRating; }
}