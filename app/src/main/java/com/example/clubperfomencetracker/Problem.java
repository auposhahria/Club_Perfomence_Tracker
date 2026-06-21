package com.example.clubperfomencetracker;

public class Problem {
    private String name;
    private String difficulty;
    private boolean isSolved;
    private String url;

    public Problem(String name, String difficulty, boolean isSolved, String url) {
        this.name = name;
        this.difficulty = difficulty;
        this.isSolved = isSolved;
        this.url = url;
    }

    public String getName() { return name; }
    public String getDifficulty() { return difficulty; }
    public boolean isSolved() { return isSolved; }
    public void setSolved(boolean solved) { isSolved = solved; }
    public String getUrl() { return url; }
}